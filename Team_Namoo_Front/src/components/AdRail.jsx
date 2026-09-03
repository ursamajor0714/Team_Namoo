import { useAdStore, pickActiveAd, nowLocalIso } from '../store/adStore'

/**
 * 메인/정당 페이지 좌우에 세로로 긴 광고(160×600) 칸.
 * 관리자(광고관리)가 등록한 광고를 localStorage 로 읽어, (page, side) 슬롯에서
 * 지금 시각이 노출 기간에 드는 광고를 띄운다. 없으면 기본 자리표시자.
 * @param {{ page: string, side: 'left'|'right' }} props
 */
function AdRail({ page, side }) {
  const ads = useAdStore((state) => state.ads)
  const ad = pickActiveAd(ads, page, side, nowLocalIso())

  const body = ad?.image ? (
    <img className="ad-rail__img" src={ad.image} alt="광고" />
  ) : (
    <div className="ad-rail__placeholder">
      광고 영역
      <br />
      160 × 600
    </div>
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
