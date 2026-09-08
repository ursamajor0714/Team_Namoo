#!/usr/bin/env python3
# Team_Namoo AI 뉴스 성향 분석 흐름도 -> docs/AI분석흐름도.svg
#
# 실제 코드 경로를 따라 그린다.
#   수집  NewsCacheRefreshScheduler(30분) -> NewsCollectionService.collect()
#         -> NaverNewsApiClient -> NonPoliticalOutletFilter -> ArticleTextExtractor
#   분류  ClassificationModelClient -> FastAPI /predict (klue/bert-base 파인튜닝)
#   저장  CachedNewsArticle (leaning 컬럼, 3일 보존)
#   조회  NewsController /api/news, /api/news/by-leaning, /api/news/classify

W, H = 1680, 1240
BG = "#ffffff"
INK = "#1e2545"
MUTED = "#6b7280"
LINE = "#9aa3b2"

COLORS = {
    "collect": ("#eef2ff", "#c7d2fe", "#3730a3"),
    "ai":      ("#f5f0ff", "#ddd0fb", "#6d28d9"),
    "store":   ("#ecfdf5", "#bbf7d0", "#047857"),
    "serve":   ("#fff7ed", "#fed7aa", "#c2410c"),
    "user":    ("#f8fafc", "#e2e8f0", "#334155"),
}

def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

out = []
def add(s): out.append(s)

def box(x, y, w, h, kind, title, lines, code=None, rx=10):
    fill, stroke, accent = COLORS[kind]
    add(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="1.5"/>')
    add(f'<text x="{x+16}" y="{y+27}" font-size="15" font-weight="700" fill="{accent}">{esc(title)}</text>')
    ty = y + 50
    for ln in lines:
        add(f'<text x="{x+16}" y="{ty}" font-size="12.5" fill="{INK}">{esc(ln)}</text>')
        ty += 19
    if code:
        add(f'<text x="{x+16}" y="{y+h-13}" font-size="11" font-family="ui-monospace,Menlo,monospace" fill="{MUTED}">{esc(code)}</text>')

def arrow(x1, y1, x2, y2, label=None, dashed=False, curve=0):
    d = f"M {x1} {y1} " + (f"Q {(x1+x2)/2+curve} {(y1+y2)/2} {x2} {y2}" if curve else f"L {x2} {y2}")
    dash = ' stroke-dasharray="6 5"' if dashed else ''
    add(f'<path d="{d}" fill="none" stroke="{LINE}" stroke-width="2"{dash} marker-end="url(#ah)"/>')
    if label:
        mx, my = (x1 + x2) / 2 + (curve * 0.5), (y1 + y2) / 2
        tw = len(label) * 6.6 + 12
        add(f'<rect x="{mx-tw/2}" y="{my-11}" width="{tw}" height="19" rx="4" fill="{BG}"/>')
        add(f'<text x="{mx}" y="{my+3}" font-size="11.5" fill="{MUTED}" text-anchor="middle">{esc(label)}</text>')

def band(x, y, w, h, text, color):
    add(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="8" fill="none" stroke="{color}" stroke-width="1.2" stroke-dasharray="7 6" opacity="0.75"/>')
    add(f'<text x="{x+12}" y="{y-8}" font-size="12" font-weight="700" fill="{color}" letter-spacing="0.06em">{esc(text)}</text>')

add(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}" font-family="-apple-system,BlinkMacSystemFont,\'Apple SD Gothic Neo\',sans-serif">')
add(f'<rect width="{W}" height="{H}" fill="{BG}"/>')
add('<defs><marker id="ah" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">'
    f'<path d="M 0 0 L 10 5 L 0 10 z" fill="{LINE}"/></marker></defs>')

add(f'<text x="60" y="52" font-size="12" font-weight="700" fill="{MUTED}" letter-spacing="0.14em">AI PIPELINE</text>')
add(f'<text x="60" y="84" font-size="21" font-weight="700" fill="{INK}">뉴스 정치성향 분석 흐름</text>')
add(f'<text x="60" y="108" font-size="12.5" fill="{MUTED}">기사를 모아 AI가 진보 · 중립 · 보수 · 판단불가로 나누고, 결과를 캐시에 쌓아 화면에 내보낸다.</text>')
add(f'<rect x="60" y="120" width="120" height="3" rx="2" fill="#10b981"/>')

# 1) 수집
band(60, 175, 480, 552, "1 · 수집 (30분마다 자동)", "#3730a3")
box(80, 200, 440, 112, "collect", "스케줄러가 깨운다", ["30분마다 백그라운드에서 실행된다.", "사용자 요청과 무관하게 미리 채워둔다."], "NewsCacheRefreshScheduler")
arrow(300, 312, 300, 344)
box(80, 344, 440, 92, "collect", "네이버 뉴스 API 로 '정치' 검색", ["최신순으로 기사 목록을 받아온다."], "NaverNewsApiClient")
arrow(300, 436, 300, 468)
box(80, 468, 440, 112, "collect", "연예·스포츠 언론사 걸러내기", ["정치와 무관한 매체는 여기서 버린다.", "AI 를 태우기 전에 줄여 비용을 아낀다."], "NonPoliticalOutletFilter.isBlocked()")
arrow(300, 580, 300, 612)
box(80, 612, 440, 92, "collect", "기사 본문 추출", ["언론사 페이지를 열어 광고·네비를 걷어내고 본문만."], "ArticleTextExtractor")

# 2) 분류
band(600, 175, 480, 552, "2 · AI 분류", "#6d28d9")
box(620, 200, 440, 112, "ai", "제목 + 본문을 한 덩어리로", ["title \\n body 로 붙여 512 토큰까지 자른다.", "앞부분만 읽어도 논조는 대개 드러난다."], "main.py / predict()")
arrow(840, 312, 840, 344)
box(620, 344, 440, 132, "ai", "klue/bert-base 파인튜닝 모델", ["구글 BERT 구조를 한국어로 학습한 공개 모델에,", "직접 라벨링한 기사 17,696건을 얹어 학습.", "4개 값의 확률을 내놓는다."], "FastAPI  POST /predict")
arrow(840, 476, 840, 508)
box(620, 508, 440, 112, "ai", "가장 높은 확률 = 판정", ["진보 · 중립 · 보수 · 판단불가 중 하나", "그 확률이 곧 '확신도'"], '{"정치성향":"중립","확신도":0.58}')
add(f'<text x="620" y="654" font-size="11.5" fill="{MUTED}">분류 서버가 죽어 있어도 수집은 계속된다 — leaning 을 비워 두고 저장한다 (classifySafely).</text>')

# 3) 저장
band(1140, 175, 480, 220, "3 · 저장", "#047857")
box(1160, 200, 440, 170, "store", "cached_news_articles", ["기사 원문 + leaning(성향) + collected_at 을 저장.",
                                                            "같은 링크는 다시 분류하지 않는다.",
                                                            "3일 지난 것은 자동 삭제.",
                                                            "→ 사용자는 캐시만 읽으므로 응답이 즉시 온다."], "CachedNewsArticle")

arrow(520, 655, 620, 400, "본문", curve=40)
arrow(1060, 560, 1160, 320, "판정 결과", curve=40)

# 4) 사용
band(1140, 468, 480, 404, "4 · 화면에 내보내기", "#c2410c")
box(1160, 492, 440, 92, "serve", "메인 뉴스 피드", ["캐시에서 최신순으로 꺼내 성향 태그와 함께."], "GET /api/news")
box(1160, 600, 440, 112, "serve", "정당별 페이지", ["1순위 제목에 정당명이 들어간 기사,", "2순위 그 정당 성향으로 분류된 기사."], "GET /api/news/by-leaning")
box(1160, 728, 440, 120, "serve", "AI 체험 페이지 (/ai)", ["사용자가 붙여넣은 기사나 링크를 즉시 분류.", "이것만 캐시를 거치지 않고 바로 모델을 부른다."], "POST /api/news/classify")

arrow(1380, 370, 1380, 492)
arrow(1160, 788, 1080, 590, "직접 호출", dashed=True, curve=-40)

# 하단 설명
add(f'<rect x="60" y="920" width="1560" height="118" rx="10" fill="#f8fafc" stroke="#e2e8f0"/>')
add(f'<text x="84" y="952" font-size="13" font-weight="700" fill="{INK}">왜 이렇게 나눴나</text>')
notes = [
    "미리 모아두는 이유 — 사용자가 들어올 때마다 크롤링하고 AI 를 돌리면 수십 초가 걸린다. 30분마다 미리 채워 두고 화면은 캐시만 읽는다.",
    "AI 앞에 필터를 두는 이유 — 연예·스포츠 기사는 어차피 '판단불가'다. 모델에 태우기 전에 버려서 시간과 자원을 아낀다.",
    "체험 페이지만 예외인 이유 — 사용자가 방금 넣은 글은 캐시에 있을 리 없으므로 그 자리에서 모델을 부른다.",
]
ny = 978
for n in notes:
    add(f'<text x="84" y="{ny}" font-size="12" fill="{MUTED}">· {esc(n)}</text>')
    ny += 21

add(f'<text x="60" y="1090" font-size="11.5" fill="{MUTED}">모델 정확도 68.1% (사람이 매긴 정답 116건 기준) · 학습 데이터 17,696건 · 4분류</text>')
add('</svg>')

import pathlib
pathlib.Path("AI분석흐름도.svg").write_text("\n".join(out))
print("wrote AI분석흐름도.svg")
