import { useEffect, useState } from 'react'
import { fetchActiveAd } from '../api/adApi'

/**
 * 메인/정당 페이지 좌우에 세로로 긴 광고 칸.
 * 지금 노출할 광고를 서버(GET /api/ads)에 물어본다. 없거나(204) 조회에 실패하면 기본 자리표시자.
 * 화면 폭에 따라 크기가 달라진다(App.css 의 .ad-rail 미디어쿼리).
 * @param {{ page: string, side: 'left'|'right' }} props
 */
function AdRail({ page, side }) {
  const [ad, setAd] = useState(null)

  useEffect(() => {
    // 페이지를 빠르게 옮기면 이전 응답이 늦게 도착해 덮어쓸 수 있어 취소 플래그를 둔다.
    let cancelled = false
    fetchActiveAd(page, side.toUpperCase())
      .then((data) => {
        if (!cancelled) {
          setAd(data)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setAd(null)
        }
      })
    return () => {
      cancelled = true
    }
  }, [page, side])

  const body = ad?.imageUrl ? (
    <img className="ad-rail__img" src={ad.imageUrl} alt="광고" />
  ) : (
    <div className="ad-rail__placeholder">광고 영역</div>
  )

  return (
    <aside
      className={`ad-rail ad-rail--${side}`}
      aria-label={side === 'left' ? '왼쪽 광고' : '오른쪽 광고'}
    >
      {ad?.linkUrl ? (
        <a
          className="ad-rail__link"
          href={ad.linkUrl}
          target="_blank"
          rel="noreferrer"
        >
          {body}
        </a>
      ) : (
        body
      )}
    </aside>
  )
}

export default AdRail
