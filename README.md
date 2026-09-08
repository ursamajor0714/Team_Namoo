# Team_Namoo

네이버 뉴스를 모아 **정치성향(진보 / 중립 / 보수 / 판단불가)** 으로 분류해 보여주는 뉴스 서비스.
정당별 뉴스 피드, 회원가입·SNS 로그인, 게시판·댓글·신고, 광고 노출, 관리자 콘솔까지 포함한 팀 프로젝트입니다.

| | 주소 |
|---|---|
| 프론트 | https://main.d11ftaq8rgsma0.amplifyapp.com |
| 백엔드 | https://15-165-118-162.sslip.io (EC2:8080) |
| 분류 API | http://43.202.134.21:8000 (팀원 계정 EC2) |

---

## 구성

| 모듈 | 기술 | 역할 | 로컬 포트 |
|---|---|---|---|
| `Team_Namoo_Front` | React 19 + Vite, zustand, axios | 뉴스 피드 · 정당 페이지 · 게시판 · 관리자 콘솔 | 5173 |
| `Team_Namoo_server` | Spring Boot 4 / Java 21 / Gradle, MySQL(RDS) | 뉴스 수집·캐시, 회원/OAuth, 게시판·신고, 광고, 관리자 API | 8080 |
| `classification-api` | FastAPI + PyTorch, `klue/bert-base` 파인튜닝 | 기사 제목·본문 → 정치성향 4분류 | 8000 |

흐름: **네이버 뉴스 API → Spring 수집·본문 추출 → 분류 API 호출 → DB 캐시 → 프론트 피드**

---

## 로컬 실행

```bash
# 프론트 + 백엔드 한 번에 (Node/JDK 설치 불필요)
docker compose up            # → localhost:5173, localhost:8080

# 분류 서버는 호스트에서 따로
cd classification-api && .venv/bin/uvicorn main:app --port 8000
```

자세한 설치는 [docs/LOCAL_DEV_SETUP.md](docs/LOCAL_DEV_SETUP.md).

**git 에 없는 것 (팀에서 따로 받아야 함)**
- `Team_Namoo_server/src/main/resources/application-local.properties` — 네이버/OpenAI 키, DB 정보
- `classification-api/model/latest/` — 학습된 분류 모델 파일

---

## 브랜치

`server`(백엔드 팀원) · `front`(프론트) → **`main`(배포)**. main 머지 = 배포이므로 머지 시점은 항상 확인하고 진행합니다.

---

## 문서

전체 색인은 [docs/README.md](docs/README.md). 먼저 볼 것만 추리면:

- [docs/AI_MODEL_HANDOFF.md](docs/AI_MODEL_HANDOFF.md) — **분류 모델 교체 안내.** 현재 배포된 모델이 정확도 29%(사실상 미작동)라 68% 모델로 교체 필요. 폴더만 갈아 끼우면 되고 서버 코드 수정 없음
- [docs/AWS_DEPLOY_PROGRESS.txt](docs/AWS_DEPLOY_PROGRESS.txt) — 배포 구성·운영·트러블슈팅 정본
- [docs/ADMIN_CONSOLE_BACKEND_TODO.md](docs/ADMIN_CONSOLE_BACKEND_TODO.md) — 백엔드 남은 작업 (중요도 순)
- [RULES.txt](RULES.txt) — 팀 코딩 규칙. 이 레포에서 코딩 전 필독
