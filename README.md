

Uploading 관리자대시보드.mov…

# Team_Namoo

**정치 뉴스를, 어느 쪽 이야기인지 한눈에.**

🔗 https://main.d11ftaq8rgsma0.amplifyapp.com



https://github.com/user-attachments/assets/03a98b9a-9c97-467f-b94e-e790f3784c2c




---

## 왜 만들었나

정치 정보는 넘쳐나는데, 정작 **그 기사가 어느 편에서 쓴 것인지 알기까지가 오래 걸립니다.**
언론사 성향을 미리 알아야 하고, 여러 매체를 직접 찾아 비교해야 합니다. 관심은 있어도
그만한 시간과 배경지식이 없는 사람에게는 진입 장벽입니다.

Team_Namoo 는 **누구나 쉽고 빠르게 정치 정보를 접할 수 있게** 하려고 시작했습니다.
수집한 기사를 AI 가 **진보 · 중립 · 보수 · 판단불가**로 자동 분류해서, 읽기 전에
성향을 먼저 보여줍니다. 한쪽만 보고 싶은 사람도, 양쪽을 비교하고 싶은 사람도
클릭 한 번으로 골라 볼 수 있습니다.

## 무엇을 할 수 있나

| 기능 | 설명 |
|---|---|
| **뉴스 피드** | 네이버 뉴스를 자동 수집·본문 추출해 성향 태그와 함께 노출 |
| **정당별 페이지** | 정당을 고르면 그 성향으로 분류된 기사만 모아서 |
| **커뮤니티** | 게시판·댓글·추천·신고 — 기사에 대한 의견을 나누는 공간 |
| **회원 / SNS 로그인** | 이메일 인증 가입 + 구글·네이버 OAuth |
| **관리자 콘솔** | 회원·게시글·신고 관리, 광고 등록(S3 업로드)과 노출 기간 설정 |

## 어떻게 동작하나

```
네이버 뉴스 API → [Spring] 수집·본문 추출 → [분류 API] AI 성향 판정 → DB 캐시 → [React] 피드
```

기사 수집·분류는 스케줄러가 백그라운드에서 돌려 캐시에 쌓아 두고, 사용자는 캐시만
읽습니다. 매 요청마다 AI 를 태우지 않아 응답이 빠릅니다.

| 모듈 | 기술 | 역할 |
|---|---|---|
| `Team_Namoo_Front` | React 19 · Vite · zustand · axios | 화면 전체 (피드 · 정당 · 게시판 · 관리자) |
| `Team_Namoo_server` | Spring Boot 4 · Java 21 · MySQL(RDS) | 뉴스 수집, 회원/OAuth, 게시판·신고, 광고, 관리자 API |
| `classification-api` | FastAPI · PyTorch · `klue/bert-base` 파인튜닝 | 기사 제목·본문 → 정치성향 4분류 |

**분류 모델**은 직접 라벨링한 데이터로 재학습해 정확도를 **29% → 68%** 로 끌어올렸습니다.
학습 과정과 실패한 시도까지 [docs/AI_MODEL_DEV_HISTORY.md](docs/AI_MODEL_DEV_HISTORY.md) 에 기록해 두었습니다.

## 배포

AWS 위에 3계층으로 올라가 있습니다.

| | |
|---|---|
| 프론트 | AWS Amplify — https://main.d11ftaq8rgsma0.amplifyapp.com |
| 백엔드 | EC2 + RDS(MySQL), HTTPS — https://15-165-118-162.sslip.io |
| 분류 API | 별도 EC2 (팀원 계정) |

---

## 로컬에서 띄우기

```bash
docker compose up            # 프론트 5173 + 백엔드 8080 (Node/JDK 설치 불필요)

cd classification-api && .venv/bin/uvicorn main:app --port 8000   # 분류 서버는 따로
```

git 에 포함하지 않은 것: `Team_Namoo_server/src/main/resources/application-local.properties`(API 키·DB),
`classification-api/model/latest/`(모델 파일). 설치 상세는 [docs/LOCAL_DEV_SETUP.md](docs/LOCAL_DEV_SETUP.md).

## 문서 · 협업

`server`(백엔드) · `front`(프론트) 브랜치에서 작업하고 **`main` 머지가 곧 배포**입니다.
문서 전체 색인은 [docs/README.md](docs/README.md), 팀 코딩 규칙은 [RULES.txt](RULES.txt).
