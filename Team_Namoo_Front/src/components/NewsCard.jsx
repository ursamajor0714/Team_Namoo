function NewsCard({ article, onClick }) {
  return (
    <button type="button" className="news-card" onClick={onClick}>
      {article.imageUrl && (
        <img
          className="news-card__thumb"
          src={article.imageUrl}
          alt=""
          onError={(event) => {
            event.currentTarget.style.display = 'none'
          }}
        />
      )}
      <p className="news-card__date">{article.pubDate}</p>
      <h3 className="news-card__title">{article.title}</h3>
      <p className="news-card__desc">{article.description}</p>
    </button>
  )
}

export default NewsCard
