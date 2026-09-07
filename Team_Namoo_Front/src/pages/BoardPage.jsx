import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { boardErrorMessage, fetchBoardPosts } from '../api/boardApi'

const SEARCH_SCOPES = [
  { value: 'title', label: '제목' },
  { value: 'author', label: '글쓴이' },
]

const TABS = [
  { value: 'all', label: '전체' },
  { value: 'notice', label: '공지' },
  { value: 'best', label: '베스트' },
]

const BEST_MIN_LIKES = 30

/** 서버가 주는 ISO 시각 → 'MM.DD' */
function shortDate(value) {
  if (!value) {
    return '—'
  }
  return String(value).slice(5, 10).replace('-', '.')
}

/**
 * 정당별 게시판 목록.
 * 글·공지는 서버(GET /api/parties/{party}/boards/{id}/posts)에서 받아온다.
 * 검색과 베스트(추천 30 이상)는 받아온 목록 안에서 추린다.
 */
function BoardPage() {
  const { name, boardId } = useParams()

  const [notices, setNotices] = useState([])
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [scope, setScope] = useState('title')
  const [keyword, setKeyword] = useState('')
  const [query, setQuery] = useState('')
  const [tab, setTab] = useState('all')

  useEffect(() => {
    let cancelled = false
    fetchBoardPosts(name, boardId)
      .then((data) => {
        if (cancelled) {
          return
        }
        setNotices(data.notices ?? [])
        setPosts(data.posts ?? [])
        setError('')
        setLoading(false)
      })
      .catch((err) => {
        if (cancelled) {
          return
        }
        setNotices([])
        setPosts([])
        setError(boardErrorMessage(err, '게시글을 불러오지 못했습니다.'))
        setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [name, boardId])

  const bestPosts = posts
    .filter((post) => post.likes >= BEST_MIN_LIKES)
    .sort((a, b) => b.likes - a.likes)

  const baseList = tab === 'notice' ? notices : tab === 'best' ? bestPosts : posts
  const visiblePosts = query
    ? baseList.filter((post) => String(post[scope] ?? '').includes(query))
    : baseList
  const showPinnedNotices = tab === 'all' && !query

  const emptyMessage = error
    ? error
    : loading
      ? '불러오는 중...'
      : query
        ? '검색 결과가 없습니다.'
        : tab === 'best'
          ? `추천 ${BEST_MIN_LIKES} 이상인 글이 없습니다.`
          : tab === 'notice'
            ? '공지사항이 없습니다.'
            : '게시글이 없습니다.'

  const handleGoHome = () => {
    setTab('all')
    setQuery('')
    setKeyword('')
  }

  const handleSearchSubmit = (event) => {
    event.preventDefault()
    setQuery(keyword.trim())
  }

  const renderPostRow = (post, isNotice) => (
    <tr key={post.id} className={`board-list__row${isNotice ? ' board-list__row--notice' : ''}`}>
      <td className="board-list__col-num">{isNotice ? '공지' : post.num}</td>
      <td className="board-list__col-title">
        <Link
          to={`/party/${encodeURIComponent(name)}/board/${boardId}/post/${post.id}`}
          className="board-list__title-link"
        >
          {post.title}
        </Link>
        {post.commentCount > 0 && (
          <span className="board-list__comment-count">[{post.commentCount}]</span>
        )}
      </td>
      <td className="board-list__col-author">{post.author}</td>
      <td className="board-list__col-date">{shortDate(post.createdAt)}</td>
      <td className="board-list__col-views">{post.views}</td>
      <td className="board-list__col-likes">{post.likes}</td>
    </tr>
  )

  return (
    <>
      <h1 className="party-page__title">
        <button type="button" className="board-home-link" onClick={handleGoHome}>
          {name} · 게시판{boardId}
        </button>
      </h1>

      <div className="board-tabs" role="tablist">
        {TABS.map((t) => (
          <button
            key={t.value}
            type="button"
            role="tab"
            aria-selected={tab === t.value}
            className={`board-tabs__tab${tab === t.value ? ' board-tabs__tab--active' : ''}`}
            onClick={() => setTab(t.value)}
          >
            {t.label}
          </button>
        ))}
      </div>

      <table className="board-list">
        <thead>
          <tr>
            <th className="board-list__col-num">번호</th>
            <th className="board-list__col-title">제목</th>
            <th className="board-list__col-author">글쓴이</th>
            <th className="board-list__col-date">작성일</th>
            <th className="board-list__col-views">조회</th>
            <th className="board-list__col-likes">추천</th>
          </tr>
        </thead>
        <tbody>
          {showPinnedNotices && notices.map((post) => renderPostRow(post, true))}
          {visiblePosts.length === 0 ? (
            <tr className="board-list__row">
              <td className="board-list__col-empty" colSpan={6}>
                {emptyMessage}
              </td>
            </tr>
          ) : (
            visiblePosts.map((post) => renderPostRow(post, tab === 'notice'))
          )}
        </tbody>
      </table>

      <div className="board-toolbar">
        <form className="board-search" onSubmit={handleSearchSubmit}>
          <select
            className="board-search__scope"
            value={scope}
            onChange={(event) => setScope(event.target.value)}
            aria-label="검색 범위"
          >
            {SEARCH_SCOPES.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          <input
            className="board-search__input"
            type="text"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="검색어를 입력하세요"
          />
          <button type="submit" className="navbar__btn">
            검색
          </button>
        </form>
        <Link
          to={`/party/${encodeURIComponent(name)}/board/${boardId}/write`}
          className="navbar__btn navbar__btn--primary"
        >
          글쓰기
        </Link>
      </div>
    </>
  )
}

export default BoardPage
