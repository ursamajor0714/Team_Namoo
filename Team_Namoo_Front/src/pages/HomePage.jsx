import { useCallback } from 'react'
import { fetchNews } from '../api/newsApi'
import NewsFeed from '../components/NewsFeed'

function HomePage() {
  const fetchFn = useCallback(() => fetchNews({ display: 12 }), [])

  return <NewsFeed fetchFn={fetchFn} />
}

export default HomePage
