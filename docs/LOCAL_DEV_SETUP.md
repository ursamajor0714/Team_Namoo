# 로컬 개발 환경 설치 (Docker Compose)

- 작성일: 2026-09-03
- 목적: 팀원이 **Node / JDK / Gradle 을 호스트에 직접 설치하지 않고**, 폴더에서 `docker compose up`
  한 번으로 개발 환경을 깔끔하게 띄우게 한다.

## 준비물

- Docker Desktop 또는 OrbStack (Compose v2.24 이상 권장 — `env_file` 의 `required: false` 사용)
- Git

## Compose 파일 3개 (용도별)

| 위치 | 명령 | 띄우는 것 |
|---|---|---|
| 프로젝트 루트 | `docker compose up` | 프론트 + 백엔드 전체 |
| `Team_Namoo_Front/` | `docker compose up` | 프론트만 (Vite dev) |
| `Team_Namoo_server/` | `docker compose up` | 백엔드만 (Spring bootRun) |

> ⚠️ 루트 것과 개별 폴더 것은 **포트(8080 / 5173)가 겹친다.** 동시에 띄우지 말 것.
> 분류 서버(`classification-api`)는 어느 compose 에도 없다 — 아래 별도 실행.

---

## 1. 전체 스택 한 번에 (가장 흔한 경우)

```bash
git clone git@github.com:ursamajor0714/Team_Namoo.git
cd Team_Namoo
docker compose up          # 첫 실행은 이미지 pull + npm install + gradle 다운로드로 몇 분 걸림
```

- 프론트: http://localhost:5173
- 백엔드: http://localhost:8080  (Swagger `/swagger-ui.html`, H2 콘솔 `/h2-console`)
- 코드 저장 → 프론트는 HMR 즉시 반영, 백엔드는 devtools 재기동
- 끄기: `Ctrl+C` 후 `docker compose down`

## 2. 프론트만

```bash
cd Team_Namoo_Front
cp .env.example .env        # 필요하면 VITE_API_BASE_URL 수정 (배포 백엔드에 붙이는 등)
docker compose up
```

- 백엔드는 호스트에서 직접 돌리거나(`../Team_Namoo_server` compose), 배포 주소를 `.env` 에 넣는다.

## 3. 백엔드만

```bash
cd Team_Namoo_server
cp .env.example .env        # NAVER 키 / Gmail 앱비번 있으면 채우기 (없어도 기동됨)
docker compose up
```

- DB 는 기본 **H2 인메모리** (재기동하면 데이터 사라짐).
- 실제 키를 IDE 직접 실행에서도 쓰려면 `src/main/resources/application-local.properties`
  (git 미추적) 에 넣는 게 편하다.

## 4. 분류 서버 (classification-api) — 필요할 때만

정당 페이지 성향 태그를 실제로 보려면 호스트에서 따로 띄운다.

```bash
cd classification-api
python3 -m venv .venv && .venv/bin/pip install -r requirements.txt   # 최초 1회
# 모델 파일(model/latest/, ~443MB)은 git 에 없음 → 팀에서 받아 이 경로에 둔다
.venv/bin/uvicorn main:app --port 8000
```

컨테이너 안 백엔드는 `host.docker.internal:8000` 으로 호스트의 이 서버를 본다 (compose 에 설정됨).

---

## 왜 이렇게 바꿨나 (이전 방식의 문제)

- **이전:** `Dockerfile` 로 이미지를 굽고, 프론트는 익명 볼륨으로 `node_modules` 를 얹었다.
  → `docker compose up` (--build 없이) 하면 **npm 패키지가 안 깔리거나 옛 버전으로 남는** 문제.
  → 백엔드는 이미지 빌드 때마다 Gradle 배포판(약 130MB)을 캐시 없이 다시 받아 **느리고 네트워크 취약**.
- **지금:** Dockerfile 없이 공식 베이스 이미지(`node:20-alpine`, `eclipse-temurin:21-jdk`)를 쓰고,
  컨테이너가 **기동할 때마다 `npm install` / `gradlew` 실행**. 캐시(node_modules, ~/.gradle)는
  **named volume** 에 담아 호스트를 더럽히지 않고 두 번째 기동부터 빠르다.
- `Team_Namoo_*/Dockerfile` 과 `.dockerignore` 는 이제 compose 가 안 쓴다. 참고용으로 남겨둠
  (배포는 Docker 를 안 쓰고 EC2 에서 jar/파이썬 직접 실행 — `AWS_DEPLOY_PROGRESS.txt`).

## 자주 겪는 문제

| 증상 | 해결 |
|---|---|
| 포트 이미 사용 중 | 다른 compose 나 예전 컨테이너가 떠 있음. `docker compose ls`, `docker ps` 확인 후 `down` |
| npm/gradle 캐시가 꼬임 | `docker compose down -v` (named volume 삭제) 후 다시 `up` |
| 백엔드가 분류 서버 못 붙음 | 호스트에서 `uvicorn ... --port 8000` 떠 있는지, `curl localhost:8000/health` |
| 뉴스가 빈 배열 | NAVER 키 없음. `Team_Namoo_server/.env` 에 `NAVER_CLIENT_ID/SECRET` 넣고 재기동 |
| 회원가입에서 이메일 인증 실패 | 백엔드 알려진 버그 — `docs/ADMIN_CONSOLE_BACKEND_TODO.md` 5-1 참고 |
