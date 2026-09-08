import { useState } from 'react'
import AiDevLog from '../components/AiDevLog'
import { classifyArticleWithConfidence } from '../api/newsApi'

/** 모델이 내는 네 가지 판정. 값은 App.css 의 ai-demo__verdict--* 와 짝을 이룬다. */
const LEANING_KEY = {
  진보: 'progressive',
  중립: 'neutral',
  보수: 'conservative',
  판단불가: 'undetermined',
}

/** 붙여넣을 수 있는 최대 길이. 모델은 앞부분 512토큰만 보므로 이보다 길 필요가 없다. */
const MAX_LENGTH = 10000

/** 버튼 한 번으로 채워지는 예시. 기사를 직접 찾아오지 않아도 바로 눌러볼 수 있게 둔다. */
const SAMPLES = [
  {
    label: '경제 정책',
    text: '정부가 법인세 인하와 규제 완화를 골자로 한 투자 활성화 대책을 발표했다. 기업의 투자 여력을 늘려 일자리를 만들겠다는 취지다. 재계는 즉각 환영 입장을 냈다.',
  },
  {
    label: '복지 정책',
    text: '야당은 기초연금 확대와 공공임대주택 공급 확대를 담은 법안을 발의했다. 소득 격차를 줄이려면 재정의 역할을 키워야 한다고 강조했다.',
  },
  {
    label: '정치와 무관',
    text: '해남 고다산성 발굴 조사에서 왕(王)자가 새겨진 청동 인장과 금동 허리띠 장식이 출토됐다. 삼국시대 지방 통치 체계를 보여주는 유물로 평가된다.',
  },
]

/**
 * AI 체험 페이지 (/ai).
 * 기사 본문을 붙여넣거나 기사 링크를 넣고 버튼을 누르면, 학습한 분류 모델이 성향을 판정한다.
 * 입력한 글과 주소는 판정에만 쓰고 저장하지 않는다.
 */
function AiDemoPage() {
  const [mode, setMode] = useState('text') // 'text' | 'url'
  const [text, setText] = useState('')
  const [url, setUrl] = useState('')
  const [result, setResult] = useState(null) // { leaning, confidence, title }
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function run() {
    const payload = mode === 'url' ? { url: url.trim() } : { content: text.trim() }
    const filled = mode === 'url' ? payload.url : payload.content
    if (!filled) {
      setError(mode === 'url' ? '기사 주소를 넣어주세요.' : '기사 내용을 붙여넣어 주세요.')
      return
    }

    setBusy(true)
    setError('')
    setResult(null)
    try {
      const data = await classifyArticleWithConfidence(payload)
      if (!data.leaning) {
        setError(data.message || '분류 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.')
        return
      }
      setResult(data)
    } catch {
      setError('분류에 실패했습니다. 잠시 후 다시 시도해주세요.')
    } finally {
      setBusy(false)
    }
  }

  function applySample(sample) {
    setMode('text')
    setText(sample.text)
    setResult(null)
    setError('')
  }

  function switchMode(next) {
    setMode(next)
    setResult(null)
    setError('')
  }

  return (
    <section className="ai-demo">
      <h1 className="ai-demo__title">AI 정치성향 분류 체험</h1>
      <p className="ai-demo__lead">
        직접 라벨링한 기사 17,696건으로 학습시킨 모델입니다.
      </p>
      <p className="ai-demo__lead">
        기사를 넣고 버튼을 누르면 <strong>진보 · 중립 · 보수 · 판단불가</strong> 중 하나로 판정합니다.
      </p>
      <p className="ai-demo__lead ai-demo__lead--muted">입력한 내용은 저장하지 않습니다.</p>

      <div className="ai-demo__tabs" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={mode === 'text'}
          className={`ai-demo__tab${mode === 'text' ? ' ai-demo__tab--on' : ''}`}
          onClick={() => switchMode('text')}
        >
          본문 붙여넣기
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={mode === 'url'}
          className={`ai-demo__tab${mode === 'url' ? ' ai-demo__tab--on' : ''}`}
          onClick={() => switchMode('url')}
        >
          기사 링크로
        </button>
      </div>

      {mode === 'text' ? (
        <>
          <div className="ai-demo__samples">
            <span className="ai-demo__samples-label">예시 넣기</span>
            {SAMPLES.map((sample) => (
              <button
                key={sample.label}
                type="button"
                className="navbar__btn"
                onClick={() => applySample(sample)}
                disabled={busy}
              >
                {sample.label}
              </button>
            ))}
          </div>

          <textarea
            className="ai-demo__input"
            value={text}
            onChange={(event) => setText(event.target.value)}
            maxLength={MAX_LENGTH}
            rows={12}
            placeholder="기사 본문을 붙여넣으세요."
            aria-label="기사 본문"
          />
          <div className="ai-demo__count">
            {text.length.toLocaleString()} / {MAX_LENGTH.toLocaleString()}자
          </div>
        </>
      ) : (
        <>
          <input
            type="url"
            className="ai-demo__url"
            value={url}
            onChange={(event) => setUrl(event.target.value)}
            placeholder="https://n.news.naver.com/... 기사 주소를 붙여넣으세요"
            aria-label="기사 주소"
          />
          <p className="ai-demo__hint">
            주소를 넣으면 서버가 그 기사를 읽어와 본문을 뽑은 뒤 판정합니다. 언론사에 따라 본문을
            못 읽는 경우가 있는데, 그때는 본문 붙여넣기를 써주세요.
          </p>
        </>
      )}

      <button type="button" className="btn btn--primary ai-demo__run" onClick={run} disabled={busy}>
        {busy ? 'AI가 읽는 중...' : 'AI 판단 가동'}
      </button>

      {error && <p className="ai-demo__error">{error}</p>}

      {result && (
        <div
          className={`ai-demo__verdict ai-demo__verdict--${LEANING_KEY[result.leaning] ?? 'undetermined'}`}
        >
          {result.title && <p className="ai-demo__verdict-source">읽은 기사 · {result.title}</p>}
          <p className="ai-demo__verdict-text">
            AI는 이 기사를 <strong>{result.leaning}</strong> 성향으로 판단했습니다.
          </p>
          <p className="ai-demo__verdict-sub">확신도 {result.confidence}%</p>
        </div>
      )}

      <AiDevLog />
    </section>
  )
}

export default AiDemoPage
