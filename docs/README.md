# docs — Team_Namoo 문서 모음

마지막 정리: 2026-09-08

| 파일 | 내용 | 상태 |
|---|---|---|
| [LOCAL_DEV_SETUP.md](LOCAL_DEV_SETUP.md) | 로컬 개발 환경 설치 (docker compose 3개) | 2026-09-03 |
| [BACKEND_TODO.txt](BACKEND_TODO.txt) | 백엔드 작업 명세 (회원가입/뉴스/게시판 기본기능) | 2026-09-01, 일부 완료 |
| [ADMIN_CONSOLE_FRONTEND_PLAN.md](ADMIN_CONSOLE_FRONTEND_PLAN.md) | 관리자 콘솔 프론트 구축안 | 2026-09-03 |
| [ADMIN_CONSOLE_BACKEND_TODO.md](ADMIN_CONSOLE_BACKEND_TODO.md) | 관리자 콘솔을 실서비스로 만들기 위한 백엔드 작업 (S3 업로드 포함) | 2026-09-03 |
| [AWS_DEPLOY_PLAN.txt](AWS_DEPLOY_PLAN.txt) | AWS 배포 계획 (초안) | 2026-09-02 |
| [AWS_DEPLOY_PROGRESS.txt](AWS_DEPLOY_PROGRESS.txt) | AWS 배포 진행 상황 / 운영 / 트러블슈팅 (정본) | 계속 갱신 |
| [2026-09-07_서버브랜치_연동점검.md](2026-09-07_%EC%84%9C%EB%B2%84%EB%B8%8C%EB%9E%9C%EC%B9%98_%EC%97%B0%EB%8F%99%EC%A0%90%EA%B2%80.md) | **9/7 작업 정본** — server→main 배포, 정당 시드·CORS 예비요청·관리자권한 버그 수정, 광고 S3, 관리자 콘솔·게시판·신고·SNS 로그인 연동. 맨 아래 '총정리'에 현재 상태와 남은 일 | 2026-09-07 |

| [AI_MODEL_HANDOFF.md](AI_MODEL_HANDOFF.md) | **정치성향 분류 모델 교체 안내 (실행용)** — 배포 중 모델이 정확도 29%(거의 찍기)라 68% 모델로 교체. 폴더만 갈아 끼우면 되고 서버 코드 수정 불필요 | 2026-09-08 |
| [AI_MODEL_DEV_HISTORY.md](AI_MODEL_DEV_HISTORY.md) | **분류 모델 개발 과정 전체 기록** — 나흘간 8단계, 실패한 시도와 저지른 실수 포함. 재학습·라벨 기준 변경 전에 필독 | 2026-09-08 |

| [2026-09-08_게시글댓글_수정삭제_연동.md](2026-09-08_%EA%B2%8C%EC%8B%9C%EA%B8%80%EB%8C%93%EA%B8%80_%EC%88%98%EC%A0%95%EC%82%AD%EC%A0%9C_%EC%97%B0%EB%8F%99.md) | **9/8 작업** — 서버에만 있고 프론트가 안 붙어 있던 게시글·댓글 수정/삭제를 연동. 응답에 `mine`(작성자 본인) 추가 | 2026-09-08 |
| [2026-09-08_관리자_회원탈퇴.md](2026-09-08_%EA%B4%80%EB%A6%AC%EC%9E%90_%ED%9A%8C%EC%9B%90%ED%83%88%ED%87%B4.md) | **9/8 작업** — 관리자 콘솔에서 회원을 DB 에서 실제 삭제. 참조 데이터(추천·신고·광고·글·댓글) 처리 방식과 막아둔 조건 | 2026-09-08 |

- 팀 코딩 규칙은 레포 루트 `RULES.txt` (도구가 참조해서 여기로 안 옮김).
- **`classification-api` 담당은 AI_MODEL_HANDOFF.md 를 먼저 볼 것** — 지금 배포된 분류 모델이 사실상 작동하지 않고 있습니다.
- 백엔드 담당은 **ADMIN_CONSOLE_BACKEND_TODO.md** 를 우선 볼 것 — 중요도 순 + 작업 후 점검 절차 포함.
