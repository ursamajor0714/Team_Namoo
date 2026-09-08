#!/usr/bin/env python3
# Team_Namoo 팀원 소개 -> docs/팀원소개.svg

W, H = 1640, 880
BG, INK, MUTED = "#ffffff", "#1e2545", "#6b7280"

def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

out = []
def add(s): out.append(s)

MEMBERS = [
    {"name": "유근호", "role": "팀장", "initial": "유",
     "accent": "#1d4ed8", "fill": "#eff6ff", "stroke": "#bfdbfe",
     "parts": [("프론트엔드", "화면 전체 · 관리자 콘솔 · AI 체험 페이지"),
               ("기획 및 구성", "서비스 방향 설정 · 화면 설계 · 문서화"),
               ("AI 학습", "기사 라벨링 · 모델 학습 · 성능 개선")]},
    {"name": "박준이", "role": "팀원", "initial": "박",
     "accent": "#047857", "fill": "#ecfdf5", "stroke": "#a7f3d0",
     "parts": [("백엔드", "API · 인증 · 게시판 · 뉴스 수집 · 관리자 기능"),
               ("AI 튜닝", "분류 모델 서빙 · 운영 환경 조정")]},
]

add(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}" '
    f'font-family="-apple-system,BlinkMacSystemFont,\'Apple SD Gothic Neo\',sans-serif">')
add(f'<rect width="{W}" height="{H}" fill="{BG}"/>')

add(f'<text x="60" y="58" font-size="12" font-weight="700" fill="{MUTED}" letter-spacing="0.14em">TEAM</text>')
add(f'<text x="60" y="92" font-size="22" font-weight="700" fill="{INK}">팀원 소개</text>')
add(f'<text x="60" y="118" font-size="12.5" fill="{MUTED}">2인 팀 · 프론트엔드와 백엔드를 나누고 AI는 학습과 서빙으로 갈라 맡았다.</text>')
add(f'<rect x="60" y="132" width="120" height="3" rx="2" fill="#10b981"/>')

CW, GAP, X0, Y0 = 760, 40, 60, 190
for i, m in enumerate(MEMBERS):
    x = X0 + i * (CW + GAP)
    # 카드 높이는 담당 파트 개수에 맞춘다. 고정하면 항목이 적은 쪽에 빈 공간이 크게 남는다.
    CH = 128 + len(m["parts"]) * 74 + 22
    add(f'<rect x="{x}" y="{Y0}" width="{CW}" height="{CH}" rx="14" fill="{m["fill"]}" stroke="{m["stroke"]}" stroke-width="1.5"/>')
    add(f'<rect x="{x}" y="{Y0}" width="{CW}" height="5" rx="2.5" fill="{m["accent"]}"/>')

    # 이름 원
    cx, cy, r = x + 74, Y0 + 82, 34
    add(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{m["accent"]}"/>')
    add(f'<text x="{cx}" y="{cy+11}" font-size="30" font-weight="700" fill="#ffffff" text-anchor="middle">{esc(m["initial"])}</text>')

    add(f'<text x="{x+126}" y="{Y0+72}" font-size="24" font-weight="700" fill="{INK}">{esc(m["name"])}</text>')
    rw = len(m["role"]) * 13 + 22
    add(f'<rect x="{x+126+len(m["name"])*25+12}" y="{Y0+52}" width="{rw}" height="26" rx="13" fill="{m["accent"]}"/>')
    add(f'<text x="{x+126+len(m["name"])*25+12+rw/2}" y="{Y0+70}" font-size="13" font-weight="700" fill="#ffffff" text-anchor="middle">{esc(m["role"])}</text>')
    add(f'<text x="{x+126}" y="{Y0+98}" font-size="12.5" fill="{MUTED}">담당 파트</text>')

    y = Y0 + 128
    for title, desc in m["parts"]:
        add(f'<rect x="{x+28}" y="{y}" width="{CW-56}" height="62" rx="9" fill="#ffffff" stroke="{m["stroke"]}"/>')
        add(f'<rect x="{x+28}" y="{y+14}" width="4" height="34" rx="2" fill="{m["accent"]}"/>')
        add(f'<text x="{x+48}" y="{y+27}" font-size="15" font-weight="700" fill="{INK}">{esc(title)}</text>')
        add(f'<text x="{x+48}" y="{y+48}" font-size="12" fill="{MUTED}">{esc(desc)}</text>')
        y += 74

# 개발 목표
GY = 580
add(f'<rect x="60" y="{GY}" width="{W-120}" height="184" rx="14" fill="#f8fafc" stroke="#e2e8f0" stroke-width="1.5"/>')
add(f'<rect x="60" y="{GY}" width="{W-120}" height="5" rx="2.5" fill="#334155"/>')
add(f'<text x="88" y="{GY+46}" font-size="17" font-weight="700" fill="{INK}">개발 목표</text>')
add(f'<text x="88" y="{GY+90}" font-size="17" fill="{INK}">누구나 쉽게 정치에 접근하고, 활발히 정보를 공유할 수 있게 하기 위해</text>')
add(f'<text x="88" y="{GY+118}" font-size="17" fill="{INK}">이 웹사이트를 개발했습니다.</text>')
add(f'<text x="88" y="{GY+154}" font-size="12.5" fill="{MUTED}">기사의 정치성향을 AI가 먼저 알려주고, 정당별로 모아 보고, 게시판에서 의견을 나눌 수 있게 했다.</text>')

add('</svg>')

import pathlib
pathlib.Path("팀원소개.svg").write_text("\n".join(out))
print("wrote 팀원소개.svg")
