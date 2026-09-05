# 관리자 콘솔 — 백엔드 작업 목록 (전체)

- 작성일: 2026-09-03 / 대상: 백엔드 담당
- **이 문서의 목적**: 프론트에서 관리자 콘솔(`/admin`)을 화면과 동작까지 다 만들어 놨는데,
  **데이터가 전부 가짜(mock)거나 브라우저에만 저장(localStorage)** 되는 상태다.
  이걸 진짜로 굴러가게 하려면 백엔드가 뭘 만들어야 하는지를, 화면 기능 하나하나에 대응시켜 정리했다.
- 읽는 법: 각 항목은 **[목적] → [지금 상태] → [할 일]** 순서. 중요도는 앞에 표시.
  - 🔴 **필수** — 이게 없으면 관리자 콘솔이 아예 안 돌거나 보안 구멍이 뚫림
  - 🟡 **중요** — 핵심 기능. 필수 끝나면 바로 이어서
  - 🟢 **나중** — 없어도 서비스는 돌아감
- 기존 `docs/BACKEND_TODO.txt`(회원가입/뉴스/게시판 기본기능) 와 겹치면 `[BACKEND_TODO n번]` 으로 표시.
- **작업을 마친 뒤에는 반드시 맨 아래 "작업 후 전체 점검" 을 실행할 것.**

### 프론트 파일 위치 (참고)

```
Team_Namoo_Front/src/
  pages/AdminPage.jsx              관리자 탭 골격 (회원관리 / 기사관리 / 게시글 관리 / 광고관리)
  constants/admin.js              관리자·슈퍼관리자 판별 (지금은 loginId 하드코딩)
  components/MemberManage.jsx      회원관리    · 목데이터 constants/adminMembers.js
  components/ArticleManage.jsx     기사관리
  components/PostManage.jsx        게시글 관리 · 더미 constants/dummyBoardPosts.js
  components/AdManage.jsx          광고관리
  components/AdRail.jsx            메인/정당 페이지 좌우 광고 칸
  store/adStore.js                광고 상태 (localStorage)
```

---

# 0. 관리자 로그인 / 권한  🔴 필수

**목적:** 지금은 "이 사람이 관리자냐"를 프론트에서 아이디(`dbrmsgh11`)로만 확인한다.
서버는 아무 검사도 안 해서, **관리자가 아니어도 API 를 직접 부르면 다 통한다.** 이걸 서버가 막아야 한다.

**지금 상태:**
- `constants/admin.js` 에서 `user.loginId === 'dbrmsgh11'` 이면 관리자로 취급.
- 슈퍼관리자도 같은 방식 (`isSuperAdmin`).
- `/api/**` 어디에도 권한 체크 없음.

**할 일:**
- [x] 🔴 `members` 에 `role` 컬럼 추가 — `USER` / `ADMIN` / `SUPER_ADMIN` (기본값 `USER`).
- [x] 🔴 `GET /api/members/me` 응답에 `role` 을 실어 보내기.
      → 프론트는 이 값으로 관리자 여부를 판단하게 바꾼다 (아이디 하드코딩 제거).
- [x] 🔴 `/api/admin/**` 모든 경로를 "로그인 + role 이 ADMIN 이상" 일 때만 통과시키기.
      아니면 401(비로그인) / 403(권한 없음). — `config/AdminAuthInterceptor.java`
- [x] 🔴 관리자 임명·해제는 **SUPER_ADMIN 만** 가능하도록 서버에서 한 번 더 검사.
      (프론트 버튼 숨김은 눈속임일 뿐, 실제 방어는 서버가 한다.)
- [x] 🟡 `SUPER_ADMIN` 계정은 강등·정지 불가 — 서버에서도 거부.
- [ ] 🟢 관리자 행동 기록 테이블 `admin_audit_log(id, admin_id, action, target_type, target_id, detail, created_at)`.
      회원 정지 / 글 삭제 / 광고 등록처럼 되돌리기 힘든 행동을 남긴다.

**참고:** 로컬 개발용 슈퍼관리자 자동 생성은 이미 있음 —
`Team_Namoo_server/src/main/java/.../config/LocalAdminInitializer.java` (`@Profile("docker")`).
운영 서버는 RDS 에 `UPDATE members SET role='SUPER_ADMIN' WHERE login_id='dbrmsgh11'` 로 직접 지정.

---

# 1. 회원관리

프론트: `MemberManage.jsx`. **목록은 눈으로만 보고, 수정은 "상세" 팝업에서만** 하도록 만들어 놨다.
목데이터는 `constants/adminMembers.js` 의 회원 16명.

## 1-1. 회원 정보 항목 늘리기  🔴 필수  `[BACKEND_TODO 10번]`

**목적:** 관리자 화면이 보여주는 회원 정보(지지정당, 가입경로, 주소, 상태, Plus 등)를
**지금 `members` 테이블은 하나도 저장하지 않는다.** 저장할 칸부터 만들어야 한다.

**목록 한 줄에 뜨는 정보 (이게 곧 필요한 컬럼):**

```
아이디        dbrmsgh11
가입일        2026-08-24
닉네임        관리자
이메일        admin@teamnamoo.local
지지정당      더불어민주당
가입경로      검색
상태          정상          (정상 / 정지 / 탈퇴)
구분          Plus, 슈퍼관리자   (배지)
[상세] 버튼 → 아래 1-2 의 상세 팝업
```

**할 일 — `members` 에 추가할 컬럼:**
- [x] 🔴 `role` ENUM(USER/ADMIN/SUPER_ADMIN) — 위 0번
- [x] 🔴 `status` ENUM(ACTIVE/SUSPENDED/WITHDRAWN) 기본 ACTIVE — 정상/정지/탈퇴
- [x] 🔴 `email_verified` BOOLEAN 기본 false
- [x] 🔴 `supported_party_id` FK → `parties(id)` — 지지정당
- [x] 🔴 `created_at` (가입일) — 없으면 추가
- [x] 🟡 `signup_channel` VARCHAR — 인스타그램/페이스북/커뮤니티/검색
- [x] 🟡 `is_plus` BOOLEAN 기본 false
- [x] 🟡 `last_access_at` TIMESTAMP — 로그인할 때마다 갱신 (아래 지표 카드에서 씀)
- [x] 🟡 `agree_marketing` BOOLEAN (`terms_agreed_at` 은 보류 — 안 씀)
- [x] 🟡 주소: `members` 에 `zipcode`/`address_base`/`address_detail` 컬럼 3개로.
- [ ] 🟡 회원가입(`SignupRequest`)에서 위 항목들 받기. (컬럼은 준비됐지만 SignupRequest는 아직
      4개 필드만 받음 — 프론트가 전송 켜기 전에 이어서 작업 필요)
      프론트 `SignupPage.jsx` 는 이미 폼에서 주소·지지정당·가입경로·마케팅동의를 입력받고
      **전송만 막아둔 상태**(코드 주석에 표시됨). 백엔드 준비되면 프론트가 전송 켠다.

## 1-2. 회원 조회 API  🔴 필수

**목적:** 관리자 목록·검색·상세를 채우려면 회원 데이터를 내려주는 API 가 필요하다.

**할 일:**
- [x] 🔴 `GET /api/admin/members?q=&field=&page=&size=`
  - `field` = `loginId | nickname | email | supportedParty | signupChannel | status`
    (프론트 검색 분류 드롭다운과 1:1)
  - 각 회원: `id, loginId, nickname, email, emailVerified, supportedParty, signupChannel,
    zipcode, addressBase, addressDetail, joinedAt, lastAccessAt, status, isPlus, agreeMarketing, role`
  - 최신 가입순, 페이지네이션. (Spring `Page` 응답 — `content`/`totalElements` 등 표준 필드)
- [x] 🔴 `GET /api/admin/members/{id}` — 상세 팝업용 (같은 항목).

## 1-3. 회원 수정 API  🔴 필수

**목적:** 상세 팝업에서 "저장"을 누르면 실제로 반영돼야 한다. 지금은 화면에서만 바뀐다.

**할 일:**
- [x] 🔴 `PATCH /api/admin/members/{id}` — 상세 팝업 "저장"
  - 바꿀 수 있는 것: `nickname, email, emailVerified, supportedParty, signupChannel,
    zipcode, addressBase, addressDetail, isPlus, agreeMarketing`
  - `loginId / joinedAt / lastAccessAt / role / status` 는 이 API 로 못 바꾼다 (아래 전용 API).
- [x] 🔴 `PATCH /api/admin/members/{id}/status`  body `{ status: "SUSPENDED" | "ACTIVE" }`
  - 상세 팝업의 **정지 / 정지 해제** 버튼.
  - 정책 결정: 정지된 회원은 **로그인 자체를 거부**함(`MemberService.login`). 글쓰기·댓글 차단은
    게시판 도메인(3번) 쪽에서 별도 처리 필요.
- [x] 🔴 `PATCH /api/admin/members/{id}/role`  body `{ role: "ADMIN" | "USER" }` — **SUPER_ADMIN 만**
  - 상세 팝업 맨 아래 **관리자 임명 / 관리자 해제** 버튼.
  - 대상이 SUPER_ADMIN 이면 거부.

## 1-4. 상단 지표 카드  🟡 중요

**목적:** 회원관리 첫 줄의 카드 4개(활성 회원 / 장기 미접속 / Plus 회원 / 금주 매출)를
지금은 프론트가 목 배열로 계산한다. 실제 숫자를 내려줘야 한다.

**할 일:**
- [x] 🟡 `GET /api/admin/members/stats`
  - `activeCount` = 상태 ACTIVE 수
  - `inactiveCount`, `inactivePct` = `last_access_at` 이 30일보다 오래된 회원 수 + 비율
  - `plusCount`, `plusPct` = Plus 회원 수 + 비율
  - `weeklyRevenue` = 결제 테이블이 없어 임시로 항상 `0` 반환 (아래 결제 테이블 만들면 이어서)
- [ ] 🟢 **금주 매출**은 결제/구독 기록 없이는 못 낸다. 최소한 테이블만이라도:
  - `payments(id, member_id, amount, kind, paid_at)` 또는
    `subscriptions(id, member_id, plan, price, started_at, expires_at, status)`
  - 금주 매출 = 이번 주(월~일) `paid_at` 범위 `SUM(amount)`.
  - 실제 PG(결제대행) 연동은 큰 별도 작업. 우선 수기 입력이라도 가능하게.

---

# 2. 기사관리

프론트: `ArticleManage.jsx`. 정당별로 `/api/news/by-leaning` 를 불러 목록을 만들고,
삭제/감추기/복구 상태는 **화면에서만** 관리한다 (새로고침하면 사라짐).

## 2-1. 기사에 "노출 상태" 추가  🟡 중요

**목적:** 관리자가 부적절한 기사를 **감추거나 삭제 표시**할 수 있어야 하고,
**"삭제"라고 해도 실제로 지우지 않고 회원에게만 안 보이게** 해야 한다 (관리자는 계속 봄).

**지금 상태:** `CachedNewsArticle` 테이블은 있음 (`id`, `originalLink`(고유), `leaning` 등).
노출 상태 개념이 없음.

**할 일:**
- [x] 🟡 `CachedNewsArticle` 에 `visibility` ENUM(`NORMAL` / `HIDDEN` / `DELETED`) 추가, 기본 `NORMAL`.
- [x] 🟡 회원용 API (`GET /api/news`, `GET /api/news/by-leaning`) 는 `visibility = NORMAL` 만 반환.
- [ ] 🟢 `leaning` 수동 교정 허용 + `leaning_overridden` 플래그 —
      자동 재분류가 관리자가 고친 값을 덮어쓰지 않도록.
- [ ] 🟢 3일 지난 기사를 지우는 `NewsCacheService.pruneOld()` 가 삭제·감춤 처리된 것도
      같이 지울지 / 남길지 정책 결정.

## 2-2. 기사관리 API  🟡 중요

**할 일:**
- [x] 🟡 `GET /api/admin/articles?party=&scope=title|content&q=`
  - `party` 별로 조회 (프론트 카테고리 버튼). 서버는 정당→성향 매핑으로 필터
    (`Team_Namoo_Front/src/constants/parties.js` 의 `PARTY_LEANING` 과 동일:
     민주·혁신·진보당→진보 / 국힘·개혁신당→보수).
  - 각 기사에 `id`, `visibility`, `title`, `link`, `originalLink`, `leaning`.
  - 관리자는 감춤·삭제된 것도 다 보이고, 프론트가 제목 앞에 `(정상)/(삭제)/(감춤)` 을 붙인다.
- [x] 🟡 `PATCH /api/admin/articles/{id}/visibility`  body `{ visibility: "DELETED"|"HIDDEN"|"NORMAL" }`
  - **삭제 / 감추기 / 복구** 버튼.
  - **중복 기사 연동 문제 자동 해결**: 같은 기사가 여러 정당 목록에 나와도 서버에선 기사 1건(id 1개)
    이므로, id 로 상태를 바꾸면 모든 정당 목록에 한 번에 반영된다.
  - 일괄: `PATCH /api/admin/articles/visibility` body `{ ids:[...], visibility }`.

---

# 3. 게시글 관리

프론트: `PostManage.jsx`. 좌측 트리(정당 → 게시판)에서 게시판을 고르면 그 게시판 글이 열린다.
**게시판·게시글·댓글 기능이 백엔드에 아직 하나도 없다.** `[BACKEND_TODO 5·6·7·8번 = P0]`
프론트는 `constants/dummyBoardPosts.js` 의 가짜 글 15개로만 돌아간다.

## 3-1. 게시판 도메인 만들기  🔴 필수  `[BACKEND_TODO 5·6·7·8번]`

**목적:** 게시글 관리는 물론이고, 서비스의 커뮤니티 기능 자체가 이거 없이는 전부 가짜다.

**할 일:**
- [x] 🔴 `boards(id, party_id, board_index 1~5, name, login_required BOOLEAN, allow_anonymous BOOLEAN)`
  - 정당 5 × 게시판 5 = 25행. 게시판 이름은 아직 "게시판1~5" 임시 — 프론트 팀과 확정 필요.
- [x] 🔴 `posts(id, board_id, author_member_id NULL, author_name, title, content,
      visibility ENUM(NORMAL/HIDDEN/DELETED) 기본 NORMAL, views, likes, dislikes, created_at, updated_at)`
- [x] 🔴 `comments(id, post_id, author_member_id NULL, author_name, content, visibility, created_at)`
- [x] 🔴 회원용 목록/작성/상세/댓글 API — `[BACKEND_TODO 6·7·8번]` 참고.

## 3-2. 게시글 관리 API  🟡 중요

**할 일:**
- [x] 🟡 `GET /api/admin/parties/{party}/boards/{boardId}/posts?page=&size=`
  - 프론트 트리에서 정당 → 게시판 선택 시 호출.
  - 각 글: `id, num, title, author, date, visibility` (+ views/likes).
  - 관리자는 감춤·삭제 글도 다 보임.
- [x] 🟡 `PATCH /api/admin/posts/{id}/visibility`  body `{ visibility: "DELETED"|"HIDDEN"|"NORMAL" }`
  - **삭제 / 감추기 / 복구** 버튼. "삭제해도 실제로 안 지워지고 회원 화면에서만 비공개" — 화면 문구 그대로.
  - 일괄: `PATCH /api/admin/posts/visibility` body `{ ids:[...], visibility }`.
- [x] 🟡 회원용 게시글 조회는 `visibility = NORMAL` 만.

## 3-3. 글쓴이 정지  🟡 중요

**목적:** 게시판 글 목록에서 글쓴이 이름을 누르면 뜨는 팝업에서 그 사람을 정지/해제할 수 있어야 한다.

**할 일:**
- [x] 🟡 `posts.author_member_id` 가 실제 회원과 연결돼야 함 (지금 더미는 이름 문자열만 있음).
- [x] 🟡 글쓴이 팝업의 **정지 / 정지 해제** = 1-3 의 `PATCH /api/admin/members/{id}/status` 를 그대로 재사용 (별도 API 불필요, 기존 것 그대로 씀).
- [ ] 🟢 비로그인 작성 글(`author_member_id` 없음)은 정지 대상이 없음 — IP 차단 등은 별도 정책.

---

# 4. 광고관리

프론트: `AdManage.jsx` + `AdRail.jsx` + `store/adStore.js`.
**광고 기능 전체가 백엔드에 없다.** 지금은 브라우저 `localStorage`(키 `teamnamoo_ads`)에만 저장되고,
같은 브라우저의 메인/정당 페이지에서만 광고가 보인다. 다른 사람·다른 기기엔 안 보인다.

## 4-1. 광고 도메인 만들기  🟡 중요

**목적:** 관리자가 등록한 광고가 실제로 모든 방문자에게, 예약한 날짜·시간에 맞춰 보여야 한다.

**할 일:**
- [ ] 🟡 `ads` 테이블
  ```
  id, page (VARCHAR: 'main' 또는 정당명),  side (ENUM LEFT/RIGHT),
  image_url (S3 URL, NULL 이면 기본이미지),  link_url (NULL 가능),
  start_at, end_at (TIMESTAMP, NULL 가능),  created_by (FK members), created_at
  (선택: advertiser, weight, daily_impression_cap, impressions, clicks)
  ```

## 4-2. 광고 조회 / 등록 API  🟡 중요

**할 일:**
- [ ] 🟡 `GET /api/ads?page=&side=` — **공개 API**.
  - 지금 시각이 `start_at ~ end_at` 안에 드는 광고 중, `start_at` 이 가장 이른 것 1건.
  - 없으면 빈 응답 → 프론트가 기본 이미지 표시.
  - **프론트 `store/adStore.js` 의 `pickActiveAd()` 로직을 그대로 서버로 옮기면 된다.**
  - **스케줄러 불필요**: "조회하는 순간의 시각으로 거른다" 만 하면
    예약한 다음 광고가 시간 되면 자동으로 뜨는 동작이 그냥 된다.
- [ ] 🟡 `GET /api/admin/ads?page=` — 관리자용 목록(= 화면의 "추가 이력").
  - `created_at` 최신순. 각 항목에 상태(`노출 중`/`예약`/`종료`/`기간 미설정`) 계산해서 같이.
- [ ] 🟡 `POST /api/admin/ads` body `{ page, side, imageUrl, linkUrl, startAt, endAt }` — "추가" 버튼.
- [ ] 🟡 `DELETE /api/admin/ads/{id}` — 이력의 "삭제" 버튼.
- [ ] 🟢 `POST /api/ads/{id}/impression`, `POST /api/ads/{id}/click` — 노출·클릭 수 집계.

## 4-3. 광고 이미지 업로드 — 반드시 S3  🔴 필수 · ⚠️ 강조

> ## ⚠️ 이미지 업로드는 무조건 S3 서버로. 꼭! 꼭! 꼭!
>
> **목적:** 광고 이미지를 안전하고 빠르게, 서버 부담 없이 서빙하려면 오브젝트 스토리지(S3)가 정답이다.
>
> **절대 하면 안 되는 것:**
> - ❌ 이미지를 **DB 에 저장** (BLOB / base64 컬럼) — 절대 금지
> - ❌ 이미지를 **WAS(스프링 서버) 로컬 디스크에 저장** — 절대 금지
> - ❌ 지금 프론트처럼 **base64 문자열로 들고 다니기** — 이건 순전히 임시 목이다
>
> **지금 상태:** 프론트가 `FileReader` 로 이미지를 base64 로 바꿔서 `localStorage` 에 넣고 있음. 임시임.

**할 일:**
- [ ] 🔴 **S3 버킷 생성** (예: `teamnamoo-ad-assets`, 서울 리전 `ap-northeast-2`).
      퍼블릭 읽기는 버킷 직접 공개가 아니라 **CloudFront 경유**로.
- [ ] 🔴 **업로드는 presigned URL 방식 권장** (서버 트래픽 최소):
  1. 프론트가 `POST /api/admin/ads/image/presign` body `{ contentType, size }` 호출
  2. 서버가 검증(확장자 `png/jpg/webp` 만, content-type 화이트리스트, 용량 상한 예: 2MB) 후
     S3 `PutObject` 용 presigned URL + 최종 오브젝트 key 반환
  3. 프론트가 그 URL 로 **S3 에 직접 PUT** (스프링 서버 안 거침)
  4. 프론트가 `POST /api/admin/ads` 할 때 `imageUrl` = 업로드된 CloudFront URL
- [ ] 🔴 presigned 가 부담되면 최소한 `POST /api/admin/ads/image` (multipart) 로 받아서
      **즉시 S3 로 스트리밍 업로드** → S3/CloudFront URL 반환. **서버 디스크에 임시파일도 남기지 말 것.**
- [ ] 🔴 오브젝트 key 는 서버가 UUID 로 새로 부여: `ads/{page}/{side}/{uuid}.{ext}`.
      업로드된 원본 파일명은 신뢰하지 않는다.
- [ ] 🔴 파일 검증: content-type + 매직바이트 확인. **SVG 는 XSS 위험 → 허용 안 함.**
- [ ] 🟡 광고 삭제 시 S3 오브젝트도 정리 (또는 S3 lifecycle rule 로 미참조 오브젝트 자동 만료).
- [ ] 🟡 권장 규격 160×600 (스카이스크래퍼). 업로드 시 리사이즈·최적화 (선택).
- [ ] 🔴 IAM: 스프링 서버(EC2) 인스턴스 롤에 **해당 버킷의 `PutObject`/`DeleteObject` 만** 부여.
      루트/개인 액세스 키를 코드나 env 에 넣지 말 것.
- [ ] 🟡 설정값: `AWS_S3_BUCKET`, `AWS_REGION`, CloudFront 도메인을 EC2 env 로.
      자격증명은 인스턴스 롤로 (키 하드코딩 금지).
>
> **한 줄 요약: 이미지 = S3. DB 저장 금지. 서버 디스크 저장 금지. presigned 로 서버 안 거치게. 꼭.**

---

# 5. 이미 알려진 것 / 이월 버그

## 5-1. 이메일 인증이 지금 깨져 있음  🔴 필수 — ✅ 해결됨 (2026-09-05)

**목적:** 회원가입이 이메일 인증을 요구하는데, **인증 코드 검증이 100% 실패해서 아무도 가입 못 한다.**

**증상:** `EmailVerificationService.sendCode()` / `verifyCode()` 가
`emailVerificationRepository.deleteByEmail()` 를 트랜잭션 없이 호출 →
`TransactionRequiredException: ... cannot reliably process 'remove' call` → 500.
첫 코드 발송만 우연히 되고, 재발송·코드 확인은 전부 500. `MemberService.signup()` 이
`isVerified` 를 검사하므로 배포 사이트에서 가입 자체가 막혀 있음.

**할 일:**
- [x] 🔴 `EmailVerificationService` 의 `sendCode`, `verifyCode` 에 `@Transactional` 붙이기.
      (`import org.springframework.transaction.annotation.Transactional`)
- [ ] 🔴 고친 뒤 실제로 코드 발송 → 입력 → 가입까지 한 번 돌려서 확인.
      (로컬은 SMTP 자격증명 미설정이라 메일 발송 자체는 못 돌려봄 — EC2 배포 후 실제 Gmail
      SMTP 로 확인 필요. 트랜잭션 버그 자체는 코드 리뷰로 확정.)

## 5-2. 기타 이월  🟡 중요 / 🟢 나중  `[BACKEND_TODO]`

- [ ] 🟡 `parties.name` 에 UNIQUE 제약 — 없으면 `data.sql` 시드가 재시작마다 중복 INSERT.
- [ ] 🟢 actuator 헬스체크 엔드포인트 추가 (지금은 `curl /api/news` 로 대체).
- [ ] 🟢 `GlobalExceptionHandler` 를 `member` 패키지에서 공통 위치로 이동.
- [ ] 🟢 실패 응답을 평문 문자열 → JSON `{ message, field? }` 로 통일.
- [ ] 🟡 `BACKEND_TODO.txt` 의 나머지 P0/P1 (게시판·검증 등) — 위 3번과 통합해 진행.

---

# 작업 우선순위 (요약)

| 순서 | 무엇을 | 왜 |
|---|---|---|
| 1 | **0번** 관리자 권한 + **5-1** 이메일 인증 버그 | 이거 없으면 관리자 콘솔 무의미 / 가입 불가 |
| 2 | **1-1~1-3** 회원 스키마 + 조회·수정·정지·임명 API | 회원관리가 콘솔의 중심 |
| 3 | **2번** 기사 노출상태(visibility) | 도메인이 이미 있어서 제일 빨리 됨 |
| 4 | **4번** 광고 도메인 + **S3 업로드** | 지금 localStorage 라 실사용 불가 |
| 5 | **3번** 게시판 도메인 전체 | 제일 큰 덩어리. `[BACKEND_TODO]` 와 통합 |
| 6 | **1-4** 지표/결제, 감사 로그, 광고 집계 | 있으면 좋은 것 |

---

# ✅ 작업 후 전체 점검 (반드시 실행)

각 모듈을 끝낼 때마다, 그리고 전부 끝난 뒤 아래를 처음부터 끝까지 한 번씩 돌려볼 것.

## 빌드 / 기동
- [ ] `cd Team_Namoo_server && ./gradlew clean build` 통과 (테스트 포함)
- [ ] 로컬 도커 스택 기동: `cd .. && docker compose up -d --build` → `docker compose logs backend` 에 에러 없음
- [ ] `curl localhost:8080/api/news` 200 + 데이터

## 권한 (0번)
- [ ] 비로그인으로 `GET /api/admin/members` → 401
- [ ] 일반 회원 로그인 쿠키로 `/api/admin/**` → 403
- [ ] `dbrmsgh11`(SUPER_ADMIN) 로그인 → `/api/admin/**` 통과, `GET /api/members/me` 에 `role` 포함
- [ ] 일반 ADMIN 계정으로 `PATCH /api/admin/members/{id}/role` → 403 (슈퍼만 가능)

## 이메일 인증 (5-1)
- [ ] 새 이메일로 코드 발송 → 재발송 → 코드 입력 → 검증 200 (500 안 남)
- [ ] 그 이메일로 회원가입 완료까지 성공

## 회원관리 (1번)
- [ ] 배포 프론트 `/admin` → 회원관리: 목록에 실제 회원이 뜨고, 검색 분류(아이디/닉네임/…)가 동작
- [ ] 상세 팝업에서 닉네임·지지정당 등 수정 → 저장 → 새로고침해도 유지
- [ ] 정지 → 그 회원 로그인/글쓰기 차단되는지, 정지 해제 → 복구
- [ ] 슈퍼관리자로 다른 회원 "관리자 임명" → 그 회원이 `/admin` 접근 가능해짐 → "관리자 해제" → 다시 막힘
- [ ] 지표 카드 숫자가 실제 회원 수와 맞는지

## 기사관리 (2번)
- [ ] 기사 "감추기" / "삭제" → 회원용 `/api/news`·`/api/news/by-leaning` 에서 사라짐
- [ ] 관리자 목록에서는 계속 보이고 제목 앞 라벨이 맞음
- [ ] 같은 기사가 여러 정당 탭에 있을 때, 한 곳에서 바꾸면 나머지도 같이 바뀜
- [ ] "복구" → 회원용에도 다시 나옴

## 게시글 관리 (3번)
- [ ] 트리에서 정당 → 게시판 선택 → 실제 글 목록 로드
- [ ] 글 "삭제" → 회원 게시판에서 안 보임, 관리자 목록엔 보임 → "복구"
- [ ] 글쓴이 팝업에서 정지 → 그 회원 글쓰기 차단

## 광고관리 (4번)
- [ ] 광고 등록(이미지 업로드 포함) → **이미지가 S3 에 올라갔는지 버킷에서 직접 확인**
- [ ] DB `ads.image_url` 이 S3/CloudFront URL 인지 (base64 아님)
- [ ] 등록한 광고가 다른 브라우저(시크릿 창)의 해당 페이지에 뜨는지
- [ ] 시작일이 미래인 광고는 안 뜨고, 기간이 지난 광고도 안 뜸
- [ ] 기간을 이어서 2건 등록 → 첫 광고 기간 끝나면 두 번째가 자동으로 뜸
- [ ] 광고 삭제 → 화면에서 사라지고 S3 오브젝트도 정리됨
- [ ] SVG·초대형 파일 업로드 시도 → 서버가 거부

## 마무리
- [ ] 새 API 전부 `/api/admin/**` 권한 게이트 안에 들어가 있는지 다시 확인
- [ ] 응답에 비밀번호 해시·내부 에러 스택 등 민감정보가 안 새는지
- [ ] `BACKEND_TODO.txt` / 이 문서의 완료 항목에 체크
- [ ] 프론트 담당에게 "목/localStorage → 실제 API 로 교체" 넘길 목록 전달
      (회원: `constants/adminMembers.js`, 광고: `store/adStore.js`, 게시글: `constants/dummyBoardPosts.js`,
       권한: `constants/admin.js` 의 하드코딩)
