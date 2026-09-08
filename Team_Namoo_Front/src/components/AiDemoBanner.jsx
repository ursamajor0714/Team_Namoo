import { Link } from 'react-router-dom'

/**
 * 메인 화면 상단 배너. 목록의 성향 태그가 어디서 나온 것인지 알려주고
 * 직접 넣어볼 수 있는 /ai 로 보낸다.
 */
function AiDemoBanner() {
  return (
    <Link to="/ai" className="ai-banner">
      <div className="ai-banner__text">
        <strong className="ai-banner__title">이 기사들의 성향은 AI가 판별했습니다</strong>
        <span className="ai-banner__sub">
          기사를 직접 넣어보세요. 어떻게 만든 AI인지도 함께 볼 수 있습니다.
        </span>
      </div>
      <span className="ai-banner__cta">직접 해보기 →</span>
    </Link>
  )
}

export default AiDemoBanner
