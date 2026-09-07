#!/usr/bin/env python3
# Team_Namoo DB ERD 생성기 -> docs/diagram3_erd.html
# 엔티티/컬럼은 origin/server 브랜치 JPA 엔티티 기준.

HDR = 38
ROW = 24
PADT = 8
PADB = 12
W = 1720
H = 1360

def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

class Entity:
    def __init__(self, key, x, y, w, title, sub, rows, note=None, accent="#1e2545"):
        self.key, self.x, self.y, self.w = key, x, y, w
        self.title, self.sub, self.rows, self.note, self.accent = title, sub, rows, note, accent
    @property
    def h(self):
        return HDR + PADT + len(self.rows) * ROW + PADB
    def row_y(self, i):
        return self.y + HDR + PADT + i * ROW + ROW / 2
    def left(self):  return (self.x, self.y + self.h / 2)
    def right(self): return (self.x + self.w, self.y + self.h / 2)
    def top(self):   return (self.x + self.w / 2, self.y)
    def bot(self):   return (self.x + self.w / 2, self.y + self.h)
    def anchor_left(self, i):  return (self.x, self.row_y(i))
    def anchor_right(self, i): return (self.x + self.w, self.row_y(i))
    def svg(self):
        p = []
        p.append(f'<g>')
        p.append(f'<rect x="{self.x}" y="{self.y}" width="{self.w}" height="{self.h}" rx="12" '
                 f'fill="#ffffff" stroke="#cdd3de" stroke-width="1.5"/>')
        p.append(f'<path d="M{self.x} {self.y+16} q0 -16 16 -16 h{self.w-32} q16 0 16 16 v{HDR-16} h-{self.w} z" '
                 f'fill="{self.accent}"/>')
        p.append(f'<text class="ent-t" x="{self.x+16}" y="{self.y+25}">{esc(self.title)}</text>')
        if self.sub:
            p.append(f'<text class="ent-s" x="{self.x+self.w-14}" y="{self.y+25}" text-anchor="end">{esc(self.sub)}</text>')
        for i, (name, typ, tag) in enumerate(self.rows):
            ry = self.y + HDR + PADT + i * ROW
            if i:
                p.append(f'<line x1="{self.x+1}" y1="{ry}" x2="{self.x+self.w-1}" y2="{ry}" stroke="#eef0f4"/>')
            tcls = "col-pk" if tag == "PK" else ("col-fk" if tag == "FK" else "col")
            p.append(f'<text class="{tcls}" x="{self.x+16}" y="{ry+16}">{esc(name)}</text>')
            p.append(f'<text class="col-ty" x="{self.x+self.w-14}" y="{ry+16}" text-anchor="end">{esc(typ)}</text>')
            if tag == "PK":
                p.append(f'<text class="tag" x="{self.x+self.w-70}" y="{ry+16}" text-anchor="end">PK</text>' if False else '')
        p.append('</g>')
        if self.note:
            ny = self.y + self.h + 16
            p.append(f'<text class="note" x="{self.x+4}" y="{ny}">{esc(self.note)}</text>')
        return "\n".join(x for x in p if x)

E = {}
def add(*a, **k):
    e = Entity(*a, **k)
    E[e.key] = e
    return e

add("parties", 250, 90, 250, "parties", "정당",
    [("id", "bigint", "PK"), ("name", "varchar UQ", ""), ("is_active", "boolean", "")])

add("boards", 60, 300, 300, "boards", "정당 5 × 5 = 25",
    [("id", "bigint", "PK"),
     ("party_id", "→ parties", "FK"),
     ("board_index", "int", ""),
     ("name", "varchar", ""),
     ("login_required", "boolean", ""),
     ("allow_anonymous", "boolean", "")],
    note="UNIQUE (party_id, board_index)")

add("posts", 60, 590, 320, "posts", "게시글",
    [("id", "bigint", "PK"),
     ("board_id", "→ boards", "FK"),
     ("author_member_id", "→ members · nullable", "FK"),
     ("author_name", "varchar (익명 표시명)", ""),
     ("title", "varchar(100)", ""),
     ("content", "text", ""),
     ("visibility", "NORMAL / HIDDEN / DELETED", ""),
     ("pinned", "boolean (공지 핀)", ""),
     ("views / likes / dislikes", "int", ""),
     ("created_at / updated_at", "timestamp", "")])

add("comments", 60, 910, 300, "comments", "댓글",
    [("id", "bigint", "PK"),
     ("post_id", "→ posts", "FK"),
     ("author_member_id", "→ members · nullable", "FK"),
     ("author_name", "varchar", ""),
     ("content", "text", ""),
     ("visibility", "NORMAL / HIDDEN / DELETED", ""),
     ("created_at", "timestamp", "")])

add("post_votes", 470, 1080, 300, "post_votes", "추천 / 비추천",
    [("id", "bigint", "PK"),
     ("post_id", "→ posts", "FK"),
     ("member_id", "→ members", "FK"),
     ("type", "LIKE / DISLIKE", "")],
    note="UNIQUE (post_id, member_id) — 1인 1표")

add("members", 640, 300, 380, "members", "회원",
    [("id", "bigint", "PK"),
     ("login_id", "varchar UQ", ""),
     ("password", "varchar (BCrypt)", ""),
     ("email", "varchar UQ", ""),
     ("nickname", "varchar UQ", ""),
     ("role", "USER / ADMIN / SUPER_ADMIN", ""),
     ("status", "ACTIVE / SUSPENDED / WITHDRAWN", ""),
     ("email_verified", "boolean", ""),
     ("supported_party_id", "→ parties · nullable", "FK"),
     ("signup_channel", "varchar", ""),
     ("zipcode / address_base / address_detail", "varchar", ""),
     ("is_plus / agree_marketing", "boolean", ""),
     ("created_at / last_access_at", "timestamp", "")])

add("email_verifications", 1090, 90, 310, "email_verifications", "이메일 인증",
    [("id", "bigint", "PK"),
     ("email", "varchar UQ", ""),
     ("code", "varchar(6)", ""),
     ("expires_at", "timestamp", ""),
     ("verified", "boolean", "")],
    note="members.email 과 값으로만 연결 (FK 없음)")

add("ads", 1090, 400, 320, "ads", "배너 광고",
    [("id", "bigint", "PK"),
     ("page", "varchar (main / 정당명)", ""),
     ("side", "LEFT / RIGHT", ""),
     ("image_url", "varchar (S3)", ""),
     ("link_url", "varchar", ""),
     ("start_at / end_at", "timestamp", ""),
     ("created_by", "→ members", "FK"),
     ("created_at", "timestamp", "")])

add("reports", 1090, 720, 340, "reports", "신고",
    [("id", "bigint", "PK"),
     ("target_type", "POST / COMMENT", ""),
     ("target_id", "bigint (다형성 · FK 없음)", ""),
     ("reporter_member_id", "→ members", "FK"),
     ("reason", "varchar(500)", ""),
     ("status", "PENDING / RESOLVED / REJECTED", ""),
     ("created_at", "timestamp", "")],
    note="UNIQUE (target_type, target_id, reporter_member_id) — 중복 신고 방지")

add("cached_news_articles", 1470, 300, 400, "cached_news_articles", "뉴스 캐시 · 독립 테이블",
    [("id", "bigint", "PK"),
     ("original_link", "varchar(2000) UQ", ""),
     ("title", "varchar", ""),
     ("link", "varchar(2000)", ""),
     ("description / pub_date", "varchar", ""),
     ("content / content_html / summary", "text", ""),
     ("image_url", "varchar(2000)", ""),
     ("leaning", "진보 / 중립 / 보수 / 판단불가 · null", ""),
     ("collected_at", "timestamp", ""),
     ("visibility", "NORMAL / HIDDEN", "")],
    note="스케줄러가 30분마다 채우고 collected_at 3일 경과분 자동 삭제 · 회원/게시판과 관계 없음",
    accent="#6c3fd4")

# canvas 넓힘 (cached_news_articles 오른쪽)
W = 1900

def edge(a, b, label, dashed=False, style="ortho"):
    (x1, y1), (x2, y2) = a, b
    dash = ' stroke-dasharray="6 5"' if dashed else ''
    col = "#8a90a0" if dashed else "#454b5a"
    mid_x = (x1 + x2) / 2
    if style == "h":
        d = f"M{x1} {y1} C {mid_x} {y1}, {mid_x} {y2}, {x2} {y2}"
    elif style == "v":
        mid_y = (y1 + y2) / 2
        d = f"M{x1} {y1} C {x1} {mid_y}, {x2} {mid_y}, {x2} {y2}"
    else:
        d = f"M{x1} {y1} L {x2} {y2}"
    lx, ly = (x1 + x2) / 2, (y1 + y2) / 2
    return (f'<path d="{d}" fill="none" stroke="{col}" stroke-width="2"{dash} marker-end="url(#era)"/>'
            f'<rect x="{lx-26}" y="{ly-12}" width="52" height="20" rx="5" fill="#ffffff" opacity="0.92"/>'
            f'<text class="elbl" x="{lx}" y="{ly+3}" text-anchor="middle">{esc(label)}</text>')

edges = []
# parties 1:N boards
edges.append(edge(E["parties"].anchor_left(0), (E["boards"].x+E["boards"].w/2, E["boards"].y), "1 : N", style="v"))
# parties 0:N members.supported_party_id
edges.append(edge(E["parties"].anchor_right(0), (E["members"].x, E["members"].y+HDR+PADT+8*ROW+ROW/2), "0 : N", dashed=True, style="h"))
# boards 1:N posts
edges.append(edge(E["boards"].bot(), E["posts"].top(), "1 : N", style="v"))
# posts 1:N comments
edges.append(edge(E["posts"].bot(), E["comments"].top(), "1 : N", style="v"))
# posts 1:N post_votes
edges.append(edge(E["posts"].anchor_right(1), E["post_votes"].anchor_left(1), "1 : N", style="h"))
# members 0:N posts.author
edges.append(edge(E["members"].anchor_left(0), E["posts"].anchor_right(2), "0 : N", dashed=True, style="h"))
# members 0:N comments.author
edges.append(edge((E["members"].x, E["members"].y+E["members"].h-40), E["comments"].anchor_right(2), "0 : N", dashed=True, style="h"))
# members 1:N post_votes.member
edges.append(edge(E["members"].bot(), E["post_votes"].anchor_right(2), "1 : N", style="v"))
# members 1:N reports.reporter
edges.append(edge(E["members"].anchor_right(0), E["reports"].anchor_left(3), "1 : N", style="h"))
# members 1:N ads.created_by
edges.append(edge(E["members"].anchor_right(2), E["ads"].anchor_left(6), "1 : N", style="h"))
# email_verifications .. members (value match)
edges.append(edge(E["email_verifications"].anchor_left(1), E["members"].anchor_right(3), "email", dashed=True, style="h"))
# reports .. posts / comments (polymorphic)
edges.append(edge((E["reports"].x, E["reports"].y+HDR+PADT+2*ROW+ROW/2), (E["posts"].x+E["posts"].w, E["posts"].y+E["posts"].h-14), "target", dashed=True, style="h"))

svg = []
svg.append(f'<svg width="{W}" height="{H}" viewBox="0 0 {W} {H}" xmlns="http://www.w3.org/2000/svg">')
svg.append(f'<rect width="{W}" height="{H}" fill="#ffffff"/>')
svg.append('<text class="kick" x="60" y="46">DATA MODEL</text>')
svg.append('<text class="title" x="60" y="82">DB 스키마 (ERD) — 10개 테이블</text>')
svg.append('<rect x="60" y="98" width="120" height="5" rx="2.5" fill="#23b39d"/>')
svg.append('''<defs><marker id="era" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0 L10 5 L0 10 z" fill="#454b5a"/></marker></defs>''')
svg.extend(edges)  # edges under boxes
for e in E.values():
    svg.append(e.svg())
# legend
lx, ly = 60, 1325
svg.append(f'<text class="note" x="{lx}" y="{ly}" style="font-size:13px">실선 = JPA @ManyToOne 외래키    ·····  = 애플리케이션 레벨 연관(외래키 없음)    ·    <tspan fill="#7a4de0">보라 테이블</tspan> = 뉴스 파이프라인이 채우는 독립 캐시</text>')
svg.append('</svg>')

html = f'''<!doctype html>
<meta charset="utf-8">
<style>
  html,body{{margin:0;padding:0;background:#fff}}
  svg{{display:block}}
  text{{font-family:"Apple SD Gothic Neo","Noto Sans KR",-apple-system,sans-serif;fill:#1f2430}}
  .title{{font-size:32px;font-weight:800;letter-spacing:-.5px}}
  .kick{{font-size:14px;font-weight:700;fill:#23b39d;letter-spacing:2px}}
  .ent-t{{font-size:16px;font-weight:800;fill:#fff}}
  .ent-s{{font-size:11.5px;font-weight:600;fill:#aab4cf}}
  .col{{font-size:12.5px;font-weight:500;fill:#2b3040}}
  .col-pk{{font-size:12.5px;font-weight:800;fill:#0f766e}}
  .col-fk{{font-size:12.5px;font-weight:700;fill:#7a4de0}}
  .col-ty{{font-size:11.5px;font-weight:500;fill:#8b93a3}}
  .note{{font-size:12px;font-weight:600;fill:#7a8290}}
  .elbl{{font-size:11px;font-weight:800;fill:#454b5a}}
</style>
{"".join(svg)}
'''

import pathlib
out = pathlib.Path(__file__).parent / "diagram3_erd.html"
out.write_text(html, encoding="utf-8")
print("wrote", out)
