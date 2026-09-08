/**
 * AI 체험 페이지(/ai) 아래에 붙는 개발 기록.
 * 읽는 사람이 머신러닝을 모른다고 보고, 전문용어 대신 일상어로 적는다.
 * 숫자는 docs/AI_MODEL_DEV_HISTORY.md 와 학습 기록(전체학습결과.json)에서 가져온 실제 값이다.
 */

/**
 * 기술 구성. 이름만 나열하면 모르는 사람에게 의미가 없어서, 각 항목에 한 줄 설명을 붙인다.
 * note 는 그 선택이 무슨 뜻인지 일상어로 풀어 쓴 것이다.
 */
const SPEC = [
  {
    label: '기반 모델',
    value: 'klue/bert-base',
    note: 'BERT 는 구글이 2018년 공개한 언어 이해 모델 구조입니다. 그 구조를 한국어로 학습시켜 공개한 것이 KLUE(한국어 자연어이해 프로젝트)의 klue/bert-base 이고, 이 모델을 밑바탕으로 삼았습니다.',
  },
  {
    label: '학습 방식',
    value: '파인튜닝 (fine-tuning)',
    note: '맨바닥에서 가르치지 않고, 이미 한국어를 읽을 줄 아는 모델 위에 우리 기사 데이터만 얹어 "정치성향 판별"이라는 일만 새로 가르쳤습니다.',
  },
  {
    label: '학습 도구',
    value: 'PyTorch · Hugging Face Transformers',
    note: '모델을 학습시키고 다루는 데 쓰는 표준 도구입니다.',
  },
  {
    label: '학습 장비',
    value: 'Apple M4 · MPS',
    note: 'MPS 는 맥의 그래픽 칩을 학습에 쓰게 해주는 통로입니다. 빌려 쓰던 클라우드 대신 노트북에서 직접 돌렸습니다.',
  },
  {
    label: '학습 설정',
    value: '3회독 · 512토큰 · 학습률 4e-5',
    note: '같은 자료를 세 번 읽히고, 기사 앞부분 512개 단위까지 읽게 했습니다. 학습률은 한 번에 얼마나 크게 배울지를 정하는 값입니다.',
  },
  {
    label: '데이터 불균형 보정',
    value: '라벨별 가중치 적용',
    note: '진보 기사가 보수·중립보다 훨씬 적어서, 수가 적은 쪽을 더 무겁게 세도록 조정했습니다. 그러지 않으면 많은 쪽만 찍는 모델이 됩니다.',
  },
  {
    label: '서빙',
    value: 'FastAPI',
    note: '학습이 끝난 모델을 웹에서 부를 수 있도록 감싼 작은 서버입니다.',
  },
]

/** 맨 위 요약 카드. 굵은 숫자 하나 + 그게 무슨 뜻인지 한 줄. */
const SUMMARY = [
  { value: '68.1%', label: '정확도', sub: '사람이 매긴 정답 116건 기준' },
  { value: '17,696건', label: '학습한 기사', sub: '직접 하나씩 성향을 표시' },
  { value: '20일', label: '작업 기간', sub: '2026.08.20 ~ 09.08' },
  { value: '30% → 68%', label: '개선 폭', sub: '찍기와 다를 바 없던 상태에서' },
]

/**
 * 날짜별 일지. 세 시기로 나뉜다.
 *  - 1차(8/20~9/2): 구글 콜랩으로 부딪혀 보던 시기. 30%대에서 막혔다
 *  - 2차(9/2~9/4): 파인튜닝을 알게 되어 직접 학습 파이프라인을 만든 시기
 *  - 3차(9/5~9/8): 채점 기준부터 다시 세워 68%까지 끌어올린 시기
 * phase 는 화면에서 구간을 갈라 보여주는 데 쓴다.
 */
const TIMELINE = [
  {
    phase: '1차 · 콜랩으로 부딪히던 시기',
    date: '8월 20일',
    title: 'AI를 직접 학습시킬 수 있다는 걸 알게 됐다',
    detail:
      '구글 콜랩은 브라우저에서 남의 고성능 컴퓨터를 빌려 쓰는 서비스입니다. 여기서 처음으로 "내가 AI를 학습시킬 수 있겠다"는 감을 잡았습니다. 검색해 가며 필요한 개념을 하나씩 익혔습니다.',
  },
  {
    phase: '1차 · 콜랩으로 부딪히던 시기',
    date: '8월 21~24일',
    title: '첫 번째 학습 — 돌아가긴 했지만 30%를 못 넘겼다',
    detail:
      '학습을 끝내고 기사를 넣으면 답이 나오기는 했습니다. 그런데 정확도가 30% 아래에서 올라가지 않았습니다. 네 개 중 하나를 고르는 문제라 찍어도 25%는 맞습니다. 빌려 쓰는 환경과 자원이 부족하다고 판단해 처음부터 다시 돌리기로 했습니다.',
  },
  {
    phase: '1차 · 콜랩으로 부딪히던 시기',
    date: '8월 25일 ~ 9월 2일',
    title: '두 번째 학습 — 38%까지 올렸지만 거기서 멈췄다',
    detail:
      '설정을 바꿔가며 다시 학습시켜 35%, 최고 38%까지 올렸습니다. 하지만 더는 올라가지 않았습니다. 무료로 쓸 수 있는 시간과 성능에 한계가 있었고, 무엇을 고쳐야 오르는지도 알기 어려웠습니다.',
  },
  {
    phase: '2차 · 직접 가르치기로 바꾼 시기',
    date: '9월 2일',
    title: '파인튜닝을 알게 됐다 — 접근 자체를 바꿨다',
    detail:
      '이미 한국어를 읽을 줄 아는 공개 모델을 가져와, 그 위에 우리 기사 데이터만 얹어 가르치는 방식입니다. 맨바닥에서 시작하지 않으니 훨씬 적은 자료로도 배울 수 있습니다. 이 방식으로 학습 과정을 직접 만들기 시작했습니다.',
  },
  {
    phase: '2차 · 직접 가르치기로 바꾼 시기',
    date: '9월 4일',
    title: '직접 만든 AI를 처음으로 돌렸다',
    detail:
      '남의 환경에 맡기지 않고, 내 손으로 자료를 넣고 학습시켜 결과를 받아본 첫날입니다. 여기서부터는 무엇을 바꾸면 무엇이 달라지는지 직접 확인할 수 있게 됐습니다.',
  },
  {
    phase: '3차 · 기준부터 다시 세운 시기',
    date: '9월 5일',
    title: '배포까지 해놓은 AI가 사실상 찍고 있었다',
    detail:
      '사람이 매긴 정답으로 제대로 채점해 보니 찍는 것과 다를 바 없었습니다. 나중에 정답을 116건으로 늘려 다시 재보니 100건 중 29건이었습니다. 네 개 중 하나를 고르는 문제라 찍어도 25건은 맞습니다. 학습 환경을 내 노트북(M4 맥)으로 옮기고 처음부터 다시 시작했습니다.',
  },
  {
    phase: '3차 · 기준부터 다시 세운 시기',
    date: '9월 6일',
    title: '문제는 AI가 아니라 사람이 만든 정답표였다',
    detail:
      'AI는 사람이 미리 표시해둔 정답을 보고 배웁니다. 그런데 그 정답이 들쭉날쭉했습니다. 같은 성격의 기사인데 어떤 건 진보, 어떤 건 중립으로 표시돼 있었습니다. 기준을 통일했더니 기사를 한 건도 더 넣지 않고 정확도가 40%에서 60%로 올랐습니다.',
  },
  {
    phase: '3차 · 기준부터 다시 세운 시기',
    date: '9월 6~7일',
    title: '기사 2만 건을 모으고 1만 7천 건에 성향을 표시했다',
    detail:
      '표시 도중 잘못 분류한 89건을 한 덩어리로 발견해 되돌렸습니다. 여당과 야당이 함께 비판한 사건을 한쪽 진영으로 몰아놨던 것이 원인이었습니다.',
  },
  {
    phase: '3차 · 기준부터 다시 세운 시기',
    date: '9월 7일',
    title: '설정을 이것저것 바꿔봤지만 대부분 헛수고였다',
    detail:
      '학습 속도, 반복 횟수 등을 여러 조합으로 시험했습니다. 효과가 있었던 건 하나뿐 — 기사를 앞부분만 짧게 읽히지 말고 더 길게(512자) 읽히는 것이었습니다.',
  },
  {
    phase: '3차 · 기준부터 다시 세운 시기',
    date: '9월 7~8일',
    title: '채점 기준과 공부 기준이 20배 어긋나 있었다',
    detail:
      '시험은 서술형으로 보는데 공부는 객관식으로 하고 있던 셈입니다. 채점할 때 중요하게 보는 항목과 AI가 학습할 때 중요하게 여기던 항목이 서로 달랐습니다. 이걸 맞춘 것이 이번 작업에서 가장 큰 개선이었습니다.',
  },
  {
    phase: '3차 · 기준부터 다시 세운 시기',
    date: '9월 8일',
    title: '전체 1만 7천 건을 한 번에 학습시켜 68.1% 달성',
    detail:
      '그전까지는 데이터를 조각내 나눠 학습시키고 있었는데, 그 방식 자체가 잘못된 설계였음을 확인하고 통째로 다시 학습시켰습니다.',
  },
]

/** 겪은 문제. 실패도 그대로 적는다 - 감추는 것보다 무엇을 배웠는지가 중요하다. */
const TROUBLES = [
  {
    title: '빌려 쓰는 컴퓨터로는 벽이 있었다',
    detail:
      '처음에는 구글이 무료로 빌려주는 컴퓨터에서 학습시켰습니다. 쓸 수 있는 시간과 성능에 제한이 있어 한 번 돌리는 데 오래 걸렸고, 무엇을 고쳐야 정확도가 오르는지 확인하기가 어려웠습니다. 두 번 시도해 38%에서 멈췄습니다.',
  },
  {
    title: '틀린 문제만 다시 풀렸더니 오히려 망가졌다',
    detail:
      '틀린 기사만 모아 다시 학습시키면 좋아질 줄 알았는데 정확도가 54%에서 20%로 떨어졌습니다. 틀린 것만 보면 원래 알던 것까지 잊어버립니다. 맞은 문제를 반드시 섞어야 했습니다.',
  },
  {
    title: '기사를 길게 읽히려다 39분을 날렸다',
    detail:
      '읽는 길이를 2배로 늘리면 필요한 메모리는 4배가 됩니다. 그걸 모르고 돌렸다가 중간에 멈췄습니다.',
  },
  {
    title: '만들어놓고 연결하지 않은 코드가 있었다',
    detail:
      '점수를 계산하는 함수를 만들어두고 정작 그 함수를 아무도 부르지 않고 있었습니다. 머릿속에서 이름을 대충 부르면 코드도 그렇게 굳습니다.',
  },
]

/**
 * 라벨별 성적. 정답 116건으로 채점한 실제 값이다(전체학습결과.json).
 *  found   = 재현율. 실제 그 성향인 기사 중 몇 %를 찾아냈나
 *  correct = 정밀도. 그 성향이라고 답했을 때 몇 %가 맞았나
 * 둘을 나란히 놔야 "못 잡는 것"과 "헛짚는 것"이 구분된다.
 */
const LABEL_SCORES = [
  { label: '진보', found: 66, correct: 75, detail: '32건 중 21건' },
  { label: '중립', found: 65, correct: 52, detail: '26건 중 17건' },
  { label: '보수', found: 47, correct: 75, detail: '32건 중 15건' },
  { label: '판단불가', found: 100, correct: 74, detail: '26건 중 26건' },
]

function AiDevLog() {
  return (
    <section className="devlog">
      <h2 className="devlog__title">이 AI는 이렇게 만들어졌습니다</h2>
      <p className="devlog__lead">
        기성 서비스를 가져다 쓴 것이 아니라, 기사를 직접 모으고 하나씩 성향을 표시해 학습시킨
        모델입니다.
      </p>
      <p className="devlog__lead">
        처음 만든 것이 사실상 찍고 있다는 걸 알고 다시 만들기까지, 그 과정을 그대로 적었습니다.
      </p>

      <div className="devlog__summary">
        {SUMMARY.map((card) => (
          <article key={card.label} className="devlog-card">
            <strong className="devlog-card__value">{card.value}</strong>
            <span className="devlog-card__label">{card.label}</span>
            <span className="devlog-card__sub">{card.sub}</span>
          </article>
        ))}
      </div>

      <h3 className="devlog__heading">무엇으로 만들었나</h3>
      <dl className="devlog__spec">
        {SPEC.map((item) => (
          <div key={item.label} className="devlog-spec">
            <dt className="devlog-spec__label">{item.label}</dt>
            <dd className="devlog-spec__body">
              <strong className="devlog-spec__value">{item.value}</strong>
              <span className="devlog-spec__note">{item.note}</span>
            </dd>
          </div>
        ))}
      </dl>

      <h3 className="devlog__heading">날짜별 기록</h3>
      <ol className="devlog__timeline">
        {TIMELINE.map((step, i) => (
          <li key={step.date} className="devlog-step">
            {/* 구간이 바뀌는 첫 항목에만 구간 이름을 얹는다. */}
            {step.phase !== TIMELINE[i - 1]?.phase && (
              <span className="devlog-step__phase">{step.phase}</span>
            )}
            <span className="devlog-step__date">{step.date}</span>
            <div className="devlog-step__body">
              <strong className="devlog-step__title">{step.title}</strong>
              <p className="devlog-step__detail">{step.detail}</p>
            </div>
          </li>
        ))}
      </ol>

      <h3 className="devlog__heading">겪은 문제</h3>
      <div className="devlog__troubles">
        {TROUBLES.map((trouble) => (
          <article key={trouble.title} className="devlog-trouble">
            <strong className="devlog-trouble__title">{trouble.title}</strong>
            <p className="devlog-trouble__detail">{trouble.detail}</p>
          </article>
        ))}
      </div>

      <h3 className="devlog__heading">지금 잘하는 것과 못하는 것</h3>
      <p className="devlog__note">
        사람이 매긴 정답 116건으로 채점한 결과입니다.
      </p>
      <div className="devlog__table-wrap">
        <table className="devlog-table">
          <thead>
            <tr>
              <th scope="col">성향</th>
              <th scope="col">찾아낸 비율</th>
              <th scope="col">맞춘 비율</th>
            </tr>
          </thead>
          <tbody>
            {LABEL_SCORES.map((row) => (
              <tr key={row.label}>
                <th scope="row">{row.label}</th>
                <td>
                  <strong>{row.found}%</strong>
                  <span className="devlog-table__sub">{row.detail}</span>
                </td>
                <td>
                  <strong>{row.correct}%</strong>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="devlog__note">
        <strong>찾아낸 비율</strong>은 실제 그 성향인 기사 중 몇 %를 골라냈는가,
        <strong> 맞춘 비율</strong>은 그 성향이라고 답했을 때 몇 %가 맞았는가입니다.
      </p>
      <p className="devlog__note">
        보수는 찾아내는 비율이 47%로 가장 낮지만, 보수라고 답했을 때는 75%가 맞습니다.
        헛짚는 것이 아니라 애매하면 중립으로 넘겨서 놓치는 쪽입니다. 반대로 정치와 무관한 기사는
        26건을 하나도 빠뜨리지 않고 걸러냅니다.
      </p>

      <p className="devlog__foot">
        가장 크게 배운 것은 <strong>데이터를 더 넣는 것보다 정답 기준을 일관되게 만드는 쪽이
        효과가 컸다</strong>는 점입니다. 기사를 한 건도 늘리지 않고 기준만 맞춰서 20%p가 올랐습니다.
      </p>
      <p className="devlog__foot devlog__foot--sub">
        빌린 컴퓨터에서 38%에 막혀 있던 것을, 접근을 바꾸고 기준을 다시 세워 68%까지 올렸습니다.
        중간에 실패한 시도도 지우지 않고 남겼습니다.
      </p>
    </section>
  )
}

export default AiDevLog
