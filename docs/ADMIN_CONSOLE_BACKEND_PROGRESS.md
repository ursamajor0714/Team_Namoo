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

## 아직 안 한 것
- 2번 기사관리 (visibility)
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

수정:
  member/Member.java
  member/MemberRepository.java
  member/MemberResponse.java
  member/MemberService.java
  member/GlobalExceptionHandler.java
  config/LocalAdminInitializer.java
  email/EmailVerificationService.java
```
(전부 `Team_Namoo_server/src/main/java/com/example/team_navigation_server/` 하위)
