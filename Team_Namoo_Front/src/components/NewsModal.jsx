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

  // contentHtml 안에 이미 본문 이미지가 있으면 대표이미지(imageUrl)를 또 보여주지 않는다 -
  // 같은 사진이 위아래로 두 번 나오는 걸 막기 위함. contentHtml이 비어 있거나
  // 이미지가 없을 때만(즉 본문에 사진이 안 나올 때만) 대표이미지로 대체해서 보여준다.
  const contentHasImage = article.contentHtml?.includes('<img')
  const showHeroImage = article.imageUrl && !contentHasImage

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
        {showHeroImage && (
          <img
            className="news-modal__hero"
            src={article.imageUrl}
            alt=""
            onError={(event) => {
              event.currentTarget.style.display = 'none'
            }}
          />
        )}
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
