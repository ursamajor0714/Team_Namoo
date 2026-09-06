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

## 3. 게시판 도메인 전체 (`[BACKEND_TODO 5·6·7·8번]`) — 완료 (2026-09-05)
- **구조 결정**: 정당별 완전 분리로 결정 — `boards(id, party_id FK, board_index 1~5, name,
  login_required, allow_anonymous)`, 정당 5 × 게시판 5 = 25행. `BoardSeeder`(CommandLineRunner,
  프로필 무관)가 기동 시 없는 것만 채움(existsBy 가드, parties.name 중복시드 버그 안 따라감).
  게시판 이름은 doc대로 "게시판1~5" 임시 — 프론트 팀 확정 대기.
- `posts`/`comments` 테이블 — doc 스펙 그대로 + **`posts.pinned`(공지) 컬럼 추가**
  (doc 6번의 응답 계약 `{ notices, posts, totalCount }`를 구현하려면 공지 여부를 구분할
  컬럼이 필요한데 스펙엔 없어서 최소 추가함). **주의**: 공지를 지정/해제하는 API는 아직 없음
  (프론트 계획 문서에도 "미구현" 스텁으로만 있어 백엔드 TODO에 아직 항목화 안 됨) — 그래서
  `notices` 배열은 항상 빈 배열로 나감. 필요해지면 `PATCH /api/admin/posts/{id}/pinned` 같은
  걸 추가하면 됨.
- 회원용: `GET/POST /api/parties/{party}/boards/{boardId}/posts`,
  `GET /api/posts/{id}` (조회수 +1), `GET/POST /api/posts/{id}/comments`.
  비로그인 글쓰기/댓글은 `익명`으로 허용(현재 모든 게시판 `allow_anonymous=true`), 로그인
  회원은 닉네임이 작성자로 저장됨(`author_member_id`도 같이 저장 — 3-3 글쓴이 정지 대상 확보).
- 관리자: `GET /api/admin/parties/{party}/boards/{boardId}/posts`,
  `PATCH /api/admin/posts/{id}/visibility`, 일괄 `PATCH /api/admin/posts/visibility`.
  글쓴이 정지는 새 API 안 만들고 기존 `PATCH /api/admin/members/{id}/status` 재사용(3-3).
- `num`(글 번호)은 저장 컬럼이 아니라 조회 시 `총건수 - offset - 페이지내 순번`으로 계산.
- **로컬 H2(docker 프로필)로 실제 기동해서 curl로 전부 확인**: 25개 게시판 전부 200,
  익명/로그인 글쓰기 각각 정상(작성자 이름 다르게 저장), 조회수 누적, 댓글 작성/조회,
  관리자 DELETED 처리 → 회원 목록/상세에서 빠지고 상세는 400, 관리자 목록엔 계속 보임,
  일괄 NORMAL 복구 → 재노출, 없는 정당/게시판 400, 비로그인 관리자 API 401 — 전부 정상.
- **주의**: 작업 시작 시 `board/` 패키지에 2026-09-03자 커밋 안 된 미완성 초안 3개
  (`CommentController.java`, `CommentService.java`, `PostResponse.java`)가 남아있었음 —
  익명 글쓰기·visibility 등을 지원 안 하는 예전 설계라 지금 스펙과 충돌해서 삭제하고 새로 만듦
  (다른 코드에서 참조 없는 것 확인 후 삭제).

## 4. 광고관리 (ads 도메인 + S3 presigned 업로드) — 대부분 완료 (2026-09-07)
- 신규 `ad/` 패키지: `Ad`(page/side/imageUrl/linkUrl/startAt/endAt/createdBy/createdAt), `AdSide`(LEFT/RIGHT),
  `AdRepository`. `board`처럼 party FK 로 안 걸고 doc 스펙대로 `page` 를 그냥 문자열로 둠('main' 또는 정당명).
- 회원용: `GET /api/ads?page=&side=` — 현재 시각 기준 활성 광고 1건, 없으면 204. `start_at`/`end_at` 둘 다
  NULL이면 상시노출("기간 미설정")로, 하나만 NULL이면 그쪽만 무제한으로 취급(doc에 이 케이스 명시가 없어
  판단해서 넣음).
- 관리자: `GET /api/admin/ads?page=`(상태 계산: 노출중/예약/종료/기간미설정 포함), `POST /api/admin/ads`,
  `DELETE /api/admin/ads/{id}`. `POST /api/admin/ads/image/presign` — S3 presigned PUT URL 발급
  (png/jpg/webp 화이트리스트, 2MB 상한, SVG 명시적 거부).
- `build.gradle` 에 AWS SDK v2 (`software.amazon.awssdk:bom:2.29.52` + `s3` — presigner는 별도 아티팩트가
  아니라 `s3` 모듈에 포함돼 있음, 처음에 `s3-presigner` 를 따로 넣었다가 미해결 의존성 에러로 뺐음).
- 사용자가 AWS 콘솔에서 S3 버킷 `teamnamoo-ad-assets`(서울) 생성 완료 — 퍼블릭 액세스 전부 차단,
  ACL 비활성화 기본값 그대로. `aws.s3.bucket`/`aws.s3.region`/`aws.s3.cloudfront-domain` 를
  `application.properties` 에 env var로 추가, 로컬 버킷명은 `application-local.properties` 에 직접 넣음
  (CloudFront 는 아직 안 붙여서 비워둠 — 비어있으면 S3 버킷 URL을 그대로 imageUrl로 반환).
- **로컬 docker 프로필로 실제 기동 후 curl로 확인**: 비로그인 401, 광고 등록→공개 API 노출→삭제 시 사라짐,
  예약/노출중/종료/기간미설정 상태 계산 전부 정상, 종료일시<시작일시 400, presign의 SVG 거부/2MB초과
  거부 400 — 전부 정상.
- **미검증**: presign이 실제로 S3 PUT까지 되는지는 확인 못 함 — 로컬에 AWS 자격증명(액세스키)이 없어서
  presign 호출이 `SdkClientException`(자격증명 없음) 500으로 남. 코드/버킷 설정 문제가 아니라 로컬 키
  부재가 원인인 것까지는 확인함. EC2 배포 시 인스턴스 롤로 해결 예정이나, 배포는 사용자가 나중으로
  미룸 — 로컬에서 진짜 업로드까지 보려면 IAM 액세스키를 임시로 하나 만들어야 함(아직 안 함).
- **doc 대비 의도적으로 다르게 한 것**: 오브젝트 key를 `ads/{page}/{side}/{uuid}.{ext}` 가 아니라
  `ads/{uuid}.{ext}` 로 단순화. presign 요청 계약이 `{ contentType, size }` 뿐이라 그 시점엔 어느
  page/side 인지 알 방법이 없음(광고 등록 전에 이미지부터 올리는 흐름이라).
- **doc 항목 중 구조적으로 못 하는 것**: 파일 매직바이트 검증 — presigned 방식은 서버가 실제 파일
  바이트를 안 보고 프론트가 S3로 직접 PUT하므로 content-type 화이트리스트 이상은 서버가 검증할 수
  없음. 필요해지면 4-3 문서의 multipart 대안으로 바꿔야 함(현재는 안 만듦).
- **안 한 것(🟡/🟢, 문서에도 우선순위 낮음)**: 광고 삭제 시 S3 오브젝트 정리, 이미지 리사이즈,
  노출/클릭수 집계(`/impression`, `/click`).
- **프론트 연동 시 주의**: `Team_Namoo_Front/src/store/adStore.js` 의 `Ad` 타입은 `side` 를
  `'left'|'right'` 소문자로 쓰는데, 백엔드 `AdSide` enum은 JSON 응답에 `"LEFT"`/`"RIGHT"` 대문자로
  나간다(요청 파싱은 대소문자 안 가림). 프론트가 이 API로 갈아끼울 때 응답값 그대로 비교하는 곳이 있으면
  소문자로 맞추거나 프론트에서 `.toLowerCase()` 필요. 그 외 필드명(`startAt`/`endAt`)과 null 처리
  의미는 `pickActiveAd()`/`adScheduleLabel()` 과 동일하게 맞춰뒀음.
- **아직 필요**: IAM — EC2 인스턴스 롤에 버킷 PutObject 권한 부여는 EC2 콘솔 작업이라 배포 시점에
  같이 해야 함 (배포 자체를 나중으로 미룸).

## 아직 안 한 것
- 공지(pinned) 지정/해제 API — 위 3번 참고, 필요해지면 추가
- 광고 S3 실업로드 검증, S3 오브젝트 정리, 노출/클릭 집계, EC2 IAM 롤 (위 4번 참고)

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
  board/Board.java, BoardRepository.java, BoardSeeder.java
  board/Post.java, PostRepository.java, PostVisibility.java
  board/PostService.java, PostController.java
  board/PostCreateRequest.java, PostSummaryResponse.java, PostListResponse.java, PostDetailResponse.java
  board/Comment.java, CommentRepository.java, CommentCreateRequest.java, CommentResponse.java
  admin/AdminPostResponse.java, AdminPostService.java, AdminPostController.java
  admin/AdminPostVisibilityRequest.java, AdminPostBulkVisibilityRequest.java
  ad/Ad.java, AdSide.java, AdRepository.java, AdService.java, AdController.java, AdPublicResponse.java
  admin/AdminAdController.java, AdminAdService.java, AdminAdResponse.java, AdminAdCreateRequest.java
  admin/AdminAdImagePresignRequest.java, AdminAdImagePresignResponse.java
  config/S3Config.java

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
  build.gradle (AWS SDK v2 s3 추가)
  application.properties, application-local.properties (aws.s3.* 추가)
```
(전부 `Team_Namoo_server/src/main/java/com/example/team_navigation_server/` 하위, build.gradle/properties는 `Team_Namoo_server/` 바로 밑 또는 `src/main/resources/`)
