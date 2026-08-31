import { useEffect, useState } from 'react'
import { classifyArticle } from '../api/newsApi'

function NewsModal({ article, onClose }) {
  const [leaning, setLeaning] = useState(null)

  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        onClose()
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [onClose])

  useEffect(() => {
    if (!article) {
      return
    }
    let cancelled = false
    classifyArticle({ title: article.title, content: article.content })
      .then((result) => {
        if (!cancelled) {
          setLeaning(result)
        }
      })
      .catch(() => {
        // 분류 서버 미기동 등 - 태그 없이 조용히 넘어간다.
      })
    return () => {
      cancelled = true
    }
  }, [article])

  if (!article) {
    return null
  }

  return (
    <div className="news-modal__backdrop" onClick={onClose}>
      <div
        className="news-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="news-modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <button
          type="button"
          className="news-modal__close"
          aria-label="닫기"
          onClick={onClose}
        >
          ×
        </button>
        <p className="news-modal__date">{article.pubDate}</p>
        <h2 id="news-modal-title" className="news-modal__title">
          {article.title}
        </h2>
        {article.contentHtml ? (
          <div
            className="news-modal__content news-modal__content--html"
            dangerouslySetInnerHTML={{ __html: article.contentHtml }}
          />
        ) : (
          <p className="news-modal__content">
            {article.content || article.description}
          </p>
        )}
        <a
          className="news-modal__link"
          href={article.originalLink}
          target="_blank"
          rel="noreferrer"
        >
          원문 보기
        </a>
        {leaning && <p className="news-modal__tag">#{leaning}</p>}
      </div>
    </div>
  )
}

export default NewsModal
