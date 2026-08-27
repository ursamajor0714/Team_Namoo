import { useEffect, useState } from 'react'
import NewsCard from './NewsCard'
import NewsModal from './NewsModal'

function NewsFeed({ fetchFn, emptyMessage = '표시할 뉴스가 없습니다.' }) {
  const [articles, setArticles] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [selectedArticle, setSelectedArticle] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function loadNews() {
      setLoading(true)
      setError(null)
      try {
        const data = await fetchFn()
        if (!cancelled) {
          setArticles(data)
        }
      } catch {
        if (!cancelled) {
          setError('뉴스를 불러오지 못했습니다.')
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    loadNews()
    return () => {
      cancelled = true
    }
  }, [fetchFn])

  return (
    <>
      <main className="news-list">
        {loading && <p className="news-status">불러오는 중...</p>}
        {error && <p className="news-status news-status--error">{error}</p>}
        {!loading && !error && articles.length === 0 && (
          <p className="news-status">{emptyMessage}</p>
        )}
        {articles.map((article) => (
          <NewsCard
            key={article.originalLink}
            article={article}
            onClick={() => setSelectedArticle(article)}
          />
        ))}
      </main>

      <NewsModal
        article={selectedArticle}
        onClose={() => setSelectedArticle(null)}
      />
    </>
  )
}

export default NewsFeed
