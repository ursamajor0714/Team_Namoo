# 관리자 콘솔 백엔드 진행 상황

- 작업일: 2026-09-05
- 대상 문서: `docs/ADMIN_CONSOLE_BACKEND_TODO.md` (0/1/5-1번 항목 반영)
- 테스트: 로컬 `docker` 프로필(H2)로 기동 후 curl 로 전부 확인, 서버는 종료해둠.

## 0. 관리자 로그인 / 권한 — 완료
- `member/MemberRole.java`(USER/ADMIN/SUPER_ADMIN), `member/MemberStatus.java`(ACTIVE/SUSPENDED/WITHDRAWN) 추가
- `Member`에 role 컬럼, `GET /api/members/me` 응답에 role 포함
- `config/AdminAuthInterceptor.java` + `config/WebMvcConfig.java`: `/api/admin/**` 전체 로그인+ADMIN 이상 게이트 (비로그인 401 / 권한부족 403)
- `PATCH /api/admin/members/{id}/role` — SUPER_ADMIN만 가능, 대상이 SUPER_ADMIN이면 거부
- SUPER_ADMIN 강등/정지 서버에서 거부 (`AdminMemberService.updateStatus/updateRole`)
- 로컬 시드 계정(`config/LocalAdminInitializer.java`) role=SUPER_ADMIN으로 생성

## 1-1. 회원 정보 항목 늘리기 — 완료 (컬럼만, SignupRequest 반영은 보류)
- `Member`에 추가된 컬럼: `status`, `emailVerified`, `supportedParty`(Party FK), `signupChannel`, `zipcode`, `addressBase`, `addressDetail`, `isPlus`, `agreeMarketing`, `createdAt`, `lastAccessAt`
- **보류**: `SignupRequest`는 아직 4개 필드(loginId/password/email/nickname)만 받음 — 프론트가 주소/지지정당 등 전송을 아직 안 켜서 백엔드도 안 건드림. 프론트가 켜면 이어서 작업.

## 1-2. 회원 조회 API — 완료
- `GET /api/admin/members?field=&q=&page=&size=` — field: loginId/nickname/email/supportedParty/signupChannel/status
- `GET /api/admin/members/{id}`
- 응답: `admin/AdminMemberResponse.java`

## 1-3. 회원 수정 API — 완료
- `PATCH /api/admin/members/{id}` — nickname/email/emailVerified/supportedParty/signupChannel/zipcode/addressBase/addressDetail/isPlus/agreeMarketing
- `PATCH /api/admin/members/{id}/status` — `{status: ACTIVE|SUSPENDED|WITHDRAWN}`. 정지 시 `MemberService.login`에서 로그인 자체 거부 (정책 결정: 글쓰기 차단은 게시판 도메인 쪽 별도 작업 필요)
- `PATCH /api/admin/members/{id}/role` — `{role: ADMIN|USER}`

## 1-4. 상단 지표 카드 — 완료 (매출 제외)
- `GET /api/admin/members/stats` — activeCount/inactiveCount/inactivePct/plusCount/plusPct
- `weeklyRevenue`는 결제/구독 테이블이 없어 항상 `0` 반환 (테이블 만들면 이어서)

## 5-1. 이메일 인증 트랜잭션 버그 — 코드 수정 완료, 실발송 미검증
- `EmailVerificationService.sendCode`/`verifyCode`에 `@Transactional` 추가
- 원인: `deleteByEmail()`을 트랜잭션 밖에서 호출 → `TransactionRequiredException` → 재발송/코드확인 500, 가입 자체가 막혀 있었음
- **미검증**: 로컬은 Gmail SMTP 자격증명 미설정이라 실제 메일 발송까지는 못 돌려봄. EC2 배포 후 실제 코드 발송→가입 흐름 한 번 확인 필요.

## 2. 기사관리 (visibility) — 완료 (2026-09-05)
- `CachedNewsArticle` 에 `visibility` ENUM(`NORMAL`/`HIDDEN`/`DELETED`) 컬럼 추가, 기본 `NORMAL`
- 회원용 `GET /api/news`, `GET /api/news/by-leaning` 는 `NewsCacheService.recentPool()` 이
  `visibility = NORMAL` 인 것만 반환하도록 수정 (`CachedNewsArticleRepository`에 조건 추가된
  파생 쿼리 사용)
- `GET /api/admin/articles?party=&scope=title|content&q=` — party는 프론트와 동일한
  정당→성향 매핑(`AdminArticleService.PARTY_LEANING`)으로 필터, 없는 정당이면 400.
  scope=title|content + q 로 텍스트 검색. 관리자는 HIDDEN/DELETED 도 다 보임(필터링 없음)
- `PATCH /api/admin/articles/{id}/visibility`, 일괄 `PATCH /api/admin/articles/visibility`
  body `{ ids:[...], visibility }` — 같은 기사가 여러 정당 탭에 걸쳐 있어도 id 하나만 바꾸면
  전부 반영됨 (기사 자체가 정당별로 중복 저장되지 않으므로 자동으로 해결됨)
- 로컬 H2(docker 프로필)로 기동 후 curl 로 전부 확인: 비로그인 401, HIDDEN 처리 시
  `/api/news` 에서 빠지고 관리자 목록엔 계속 보임, 잘못된 정당명 400, 일괄 NORMAL 복구 후
  회원용에 재노출 — 전부 정상. 서버는 확인 후 종료해둠.
- **보류(🟢 나중, 팀 결정 필요)**: `leaning` 수동 교정 허용 + `leaning_overridden` 플래그
  (자동 재분류가 관리자 교정값을 덮어쓰지 않게), `NewsCacheService.pruneOld()` 가 감춤/삭제
  처리된 기사도 3일 후 같이 지울지 남길지 정책 결정 — 둘 다 아직 안 함

## 아직 안 한 것
- 3번 게시글 관리 (관리자 API — 회원용 게시판 API 자체는 이미 존재)
- 4번 광고관리 + S3 업로드

## 변경/신규 파일
```
신규:
  member/MemberRole.java
  member/MemberStatus.java
  config/AdminAuthInterceptor.java
  config/WebMvcConfig.java
  admin/AdminMemberController.java
  admin/AdminMemberService.java
  admin/AdminMemberResponse.java
  admin/AdminMemberUpdateRequest.java
  admin/AdminMemberStatusRequest.java
  admin/AdminMemberRoleRequest.java
  admin/AdminForbiddenException.java
  news/ArticleVisibility.java
  admin/AdminArticleController.java
  admin/AdminArticleService.java
  admin/AdminArticleResponse.java
  admin/AdminArticleVisibilityRequest.java
  admin/AdminArticleBulkVisibilityRequest.java

수정:
  member/Member.java
  member/MemberRepository.java
  member/MemberResponse.java
  member/MemberService.java
  member/GlobalExceptionHandler.java
  member/SignupRequest.java (supportedParty/signupChannel/zipcode/addressBase/addressDetail/agreeMarketing 추가)
  config/LocalAdminInitializer.java
  email/EmailVerification.java, EmailVerificationRepository.java, EmailVerificationService.java
  news/CachedNewsArticle.java, CachedNewsArticleRepository.java, NewsCacheService.java
```
(전부 `Team_Namoo_server/src/main/java/com/example/team_navigation_server/` 하위)
