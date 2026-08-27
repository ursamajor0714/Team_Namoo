import { useCallback } from 'react'
import { useParams } from 'react-router-dom'
import { fetchNewsByLeaning } from '../api/newsApi'
import NewsFeed from '../components/NewsFeed'

// 정당별 정치성향 매핑 - 분류 모델이 진보/중립/보수/판단불가 4종류만 반환하기 때문에
// 정당 단위가 아니라 이 성향 단위로 기사를 걸러서 보여준다.
const PARTY_LEANING = {
  더불어민주당: '진보',
  국민의힘: '보수',
  조국혁신당: '진보',
  진보당: '진보',
  개혁신당: '보수',
}

function PartyPage() {
  const { name } = useParams()
  const leaning = PARTY_LEANING[name]

  const fetchFn = useCallback(() => {
    if (!leaning) {
      return Promise.resolve([])
    }
    return fetchNewsByLeaning({ leaning, partyKeyword: name, count: 12 })
  }, [leaning, name])

  return (
    <div className="party-page">
      <h1 className="party-page__title">{name}</h1>
      <NewsFeed
        fetchFn={fetchFn}
        emptyMessage={
          leaning
            ? '이 성향으로 분류된 기사가 아직 없습니다.'
            : '등록되지 않은 정당입니다.'
        }
      />
    </div>
  )
}

export default PartyPage
