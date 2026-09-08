#!/usr/bin/env python3
# Team_Namoo 기술 스택 -> docs/기술스택.svg
# 항목은 build.gradle / package.json / requirements.txt 의 실제 의존성 기준.

W, H = 1640, 1080
BG = "#ffffff"
INK = "#1e2545"
MUTED = "#6b7280"

def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

out = []
def add(s): out.append(s)

# (제목, 부제, 강조색, 옅은배경, 테두리, 주력항목[(이름,설명)], 보조항목[문자열])
COLUMNS = [
    ("프론트엔드", "화면 · 사용자 조작", "#1d4ed8", "#eff6ff", "#bfdbfe",
     [("React 19", "화면을 구성하는 UI 라이브러리"),
      ("Vite 8", "개발 서버 · 번들러"),
      ("JavaScript (ES2023)", "언어")],
     ["react-router-dom 7 — 페이지 이동",
      "zustand 5 — 로그인 상태 보관",
      "axios — 서버 통신",
      "framer-motion — 애니메이션",
      "ESLint — 코드 검사"]),

    ("백엔드", "API · 인증 · 뉴스 수집", "#047857", "#ecfdf5", "#a7f3d0",
     [("Spring Boot 4", "웹 서버 프레임워크"),
      ("Java 21", "언어"),
      ("Gradle", "빌드 도구")],
     ["Spring Data JPA — DB 연동",
      "Spring Security Crypto — 비밀번호 암호화",
      "jsoup · readability4j — 기사 본문 추출",
      "OkHttp — 외부 API 호출",
      "Spring Mail — 이메일 인증",
      "AWS SDK (S3) — 광고 이미지 업로드",
      "SpringDoc — API 문서 자동화"]),

    ("데이터베이스", "저장", "#b45309", "#fffbeb", "#fde68a",
     [("MySQL 8", "운영 DB (AWS RDS)"),
      ("H2", "로컬 개발용 인메모리 DB"),
      ("Hibernate", "JPA 구현체")],
     ["테이블 10개 — 회원 · 게시판 · 댓글 ·",
      "추천 · 신고 · 광고 · 정당 · 뉴스 캐시 등",
      "세션 기반 인증 (JSESSIONID)"]),

    ("AI", "정치성향 분류", "#6d28d9", "#f5f0ff", "#ddd0fb",
     [("klue/bert-base", "한국어 BERT를 파인튜닝"),
      ("PyTorch", "학습 · 추론 프레임워크"),
      ("FastAPI", "모델을 서빙하는 API 서버")],
     ["Hugging Face Transformers — 모델 로딩",
      "Apple M4 · MPS — 학습 장비",
      "uvicorn — 실행 서버",
      "학습 17,696건 · 정확도 68.1%"]),
]

CW, GAP, X0, Y0 = 368, 24, 60, 200

add(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}" '
    f'font-family="-apple-system,BlinkMacSystemFont,\'Apple SD Gothic Neo\',sans-serif">')
add(f'<rect width="{W}" height="{H}" fill="{BG}"/>')

add(f'<text x="60" y="58" font-size="12" font-weight="700" fill="{MUTED}" letter-spacing="0.14em">TECH STACK</text>')
add(f'<text x="60" y="92" font-size="22" font-weight="700" fill="{INK}">Team_Namoo 기술 스택</text>')
add(f'<text x="60" y="118" font-size="12.5" fill="{MUTED}">뉴스를 모아 AI가 정치성향을 판별해 보여주는 서비스. 프론트 · 백엔드 · DB · AI 네 갈래로 나뉜다.</text>')
add(f'<rect x="60" y="132" width="120" height="3" rx="2" fill="#10b981"/>')

for i, (title, sub, accent, fill, stroke, mains, subs) in enumerate(COLUMNS):
    x = X0 + i * (CW + GAP)
    h = 600
    add(f'<rect x="{x}" y="{Y0}" width="{CW}" height="{h}" rx="12" fill="{fill}" stroke="{stroke}" stroke-width="1.5"/>')
    add(f'<rect x="{x}" y="{Y0}" width="{CW}" height="4" rx="2" fill="{accent}"/>')
    add(f'<text x="{x+22}" y="{Y0+44}" font-size="17" font-weight="700" fill="{accent}">{esc(title)}</text>')
    add(f'<text x="{x+22}" y="{Y0+66}" font-size="11.5" fill="{MUTED}">{esc(sub)}</text>')

    y = Y0 + 96
    for name, desc in mains:
        add(f'<rect x="{x+18}" y="{y}" width="{CW-36}" height="52" rx="8" fill="#ffffff" stroke="{stroke}"/>')
        add(f'<text x="{x+32}" y="{y+22}" font-size="14" font-weight="700" fill="{INK}">{esc(name)}</text>')
        add(f'<text x="{x+32}" y="{y+40}" font-size="11.5" fill="{MUTED}">{esc(desc)}</text>')
        y += 60

    y += 10
    add(f'<line x1="{x+18}" y1="{y}" x2="{x+CW-18}" y2="{y}" stroke="{stroke}" stroke-width="1.2"/>')
    y += 26
    add(f'<text x="{x+22}" y="{y}" font-size="11" font-weight="700" fill="{accent}" letter-spacing="0.05em">함께 쓴 것</text>')
    y += 24
    for s in subs:
        add(f'<text x="{x+22}" y="{y}" font-size="11.8" fill="{INK}">· {esc(s)}</text>')
        y += 21

# ── 하단: 그 외 (공통 도구 / 인프라)
BY = 840
add(f'<rect x="60" y="{BY}" width="{W-120}" height="180" rx="12" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1.5"/>')
add(f'<rect x="60" y="{BY}" width="{W-120}" height="4" rx="2" fill="#334155"/>')
add(f'<text x="82" y="{BY+40}" font-size="16" font-weight="700" fill="{INK}">그 외 — 공통 도구 · 인프라</text>')
add(f'<text x="82" y="{BY+61}" font-size="11.5" fill="{MUTED}">네 갈래 전부에서 함께 쓰는 것들</text>')

GROUPS = [
    ("협업 · 버전관리", ["Git", "GitHub", "main 푸쉬 → Amplify 자동 빌드·배포"]),
    ("개발 환경", ["VS Code", "IntelliJ IDEA", "Docker · OrbStack"]),
    ("배포 · 운영", ["AWS EC2 · RDS · S3", "AWS Amplify (프론트)", "Caddy (HTTPS) · systemd"]),
    ("외부 API", ["네이버 뉴스 검색 API", "다음 우편번호 (주소 검색)", "구글 · 네이버 OAuth 로그인"]),
]
gx = 92
gw = (W - 120 - 64) // 4
for gi, (gtitle, items) in enumerate(GROUPS):
    x = gx + gi * gw
    add(f'<text x="{x}" y="{BY+94}" font-size="12" font-weight="700" fill="#334155">{esc(gtitle)}</text>')
    yy = BY + 118
    for it in items:
        add(f'<text x="{x}" y="{yy}" font-size="11.8" fill="{INK}">· {esc(it)}</text>')
        yy += 21

add('</svg>')

import pathlib
pathlib.Path("기술스택.svg").write_text("\n".join(out))
print("wrote 기술스택.svg")
