# Team_Namoo_Front

정치 뉴스 피드 + 정당별 페이지 프론트엔드. React 19 + Vite.

## 개발

```bash
npm install
npm run dev      # http://localhost:5173
```

백엔드(`Team_Namoo_server`)가 `http://localhost:8080` 에 떠 있어야 뉴스 API가 동작한다.
API 주소는 `src/api/newsApi.js` 상단 `API_BASE_URL` 에 하드코딩되어 있다.

## 스크립트

- `npm run dev` — 개발 서버 (HMR)
- `npm run build` — 프로덕션 번들 (`dist/`)
- `npm run preview` — 빌드 결과 미리보기
- `npm run lint` — ESLint

## 구조

- `src/pages/` — 라우트 단위 페이지 (`HomePage`, `PartyPage`)
- `src/components/` — `Navbar`, `NewsFeed`, `NewsCard`, `NewsModal`
- `src/api/newsApi.js` — 백엔드 호출 (axios)

## Docker

레포 루트 `docker-compose.yml` 로 백엔드와 함께 기동한다.

```bash
docker compose up frontend
```
