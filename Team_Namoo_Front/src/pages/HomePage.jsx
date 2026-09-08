import { useCallback } from 'react'
import { fetchNews } from '../api/newsApi'
import AiDemoBanner from '../components/AiDemoBanner'
import NewsFeed from '../components/NewsFeed'

function HomePage() {
  const fetchFn = useCallback(() => fetchNews({ display: 12 }), [])

  return (
    <>
      <AiDemoBanner />
      <NewsFeed fetchFn={fetchFn} />
    </>
  )
}

export default HomePage
