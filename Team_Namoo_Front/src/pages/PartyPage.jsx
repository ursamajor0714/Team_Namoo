import { useCallback } from 'react'
import { useParams } from 'react-router-dom'
import { fetchNewsByLeaning } from '../api/newsApi'
import NewsFeed from '../components/NewsFeed'
import { PARTY_LEANING } from '../constants/parties'

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
    <>
      <h1 className="party-page__title">{name}</h1>
      <NewsFeed
        fetchFn={fetchFn}
        emptyMessage={
          leaning
            ? '이 성향으로 분류된 기사가 아직 없습니다.'
            : '등록되지 않은 정당입니다.'
        }
      />
    </>
  )
}

export default PartyPage
