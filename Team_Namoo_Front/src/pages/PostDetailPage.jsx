import { useMemo, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { NOTICE_POSTS, buildDummyComments, buildDummyPosts } from '../constants/dummyBoardPosts'
import { useAuthStore } from '../store/authStore'

function findPost(boardId, postId, statePost) {
  if (statePost) {
    return statePost
  }
  const notice = NOTICE_POSTS.find((post) => post.id === postId)
  if (notice) {
    return notice
  }
  return buildDummyPosts(boardId).find((post) => post.id === postId) ?? null
}

function PostDetailPage() {
  const { name, boardId, postId } = useParams()
  const location = useLocation()
  const user = useAuthStore((state) => state.user)

  const boardPath = `/party/${encodeURIComponent(name)}/board/${boardId}`
  const post = useMemo(
    () => findPost(boardId, postId, location.state?.post),
    [boardId, postId, location.state],
  )

  const [likes, setLikes] = useState(post?.likes ?? 0)
  const [dislikes, setDislikes] = useState(0)
  const [voted, setVoted] = useState(null)
  const [notice, setNotice] = useState('')
  const [comments, setComments] = useState(() => (post ? buildDummyComments(post.id) : []))
  const [commentDraft, setCommentDraft] = useState('')

  if (!post) {
    return (
      <>
        <h1 className="party-page__title">
          <Link to={boardPath} className="board-home-link">
            {name} · 게시판{boardId}
          </Link>
        </h1>
        <p className="post-detail__missing">
          글을 찾을 수 없습니다. 새로고침하면 사라지는 임시 글이거나 삭제된 글일 수 있습니다.
        </p>
        <div className="board-toolbar">
          <Link to={boardPath} className="navbar__btn">
            목록
          </Link>
        </div>
      </>
    )
  }

  const handleVote = (type) => {
    if (voted === type) {
      return
    }
    if (type === 'like') {
      setLikes((n) => n + 1)
      if (voted === 'dislike') {
        setDislikes((n) => n - 1)
      }
    } else {
      setDislikes((n) => n + 1)
      if (voted === 'like') {
        setLikes((n) => n - 1)
      }
    }
    setVoted(type)
  }

  const showUnavailable = (label) => {
    setNotice(`'${label}' 기능은 백엔드 연동 전이라 아직 동작하지 않습니다.`)
  }

  const handleCommentSubmit = (event) => {
    event.preventDefault()
    if (!commentDraft.trim()) {
      return
    }
    setComments((prev) => [
      ...prev,
      {
        id: `local-c-${Date.now()}`,
        author: user?.nickname ?? '익명',
        date: '방금 전',
        content: commentDraft.trim(),
      },
    ])
    setCommentDraft('')
  }

  // buildDummyPosts 는 최신(num 큼) 순으로 정렬돼 있으므로, 앞쪽이 다음글(최신), 뒤쪽이 이전글(과거).
  const siblingPosts = buildDummyPosts(boardId)
  const currentIndex = siblingPosts.findIndex((p) => p.id === post.id)
  const nextPost = currentIndex > 0 ? siblingPosts[currentIndex - 1] : null
  const prevPost =
    currentIndex >= 0 && currentIndex < siblingPosts.length - 1
      ? siblingPosts[currentIndex + 1]
      : null

  return (
    <>
      <h1 className="party-page__title">
        <Link to={boardPath} className="board-home-link">
          {name} · 게시판{boardId}
        </Link>
      </h1>

      <article className="post-detail">
        <h2 className="post-detail__title">{post.title}</h2>
        <div className="post-detail__meta">
          <span>{post.author}</span>
          <span>{post.date}</span>
          <span>조회 {post.views}</span>
          <span>추천 {likes}</span>
          <span>댓글 {comments.length}</span>
        </div>

        <div className="post-detail__content">
          {post.content.split('\n').map((line, i) => (
            <p key={i}>{line || ' '}</p>
          ))}
        </div>

        <div className="post-detail__vote">
          <button
            type="button"
            className={`post-detail__vote-btn${voted === 'like' ? ' post-detail__vote-btn--active' : ''}`}
            onClick={() => handleVote('like')}
          >
            ▲ 추천 {likes}
          </button>
          <button
            type="button"
            className={`post-detail__vote-btn${voted === 'dislike' ? ' post-detail__vote-btn--active' : ''}`}
            onClick={() => handleVote('dislike')}
          >
            ▼ 비추천 {dislikes}
          </button>
        </div>

        <div className="post-detail__actions">
          <button type="button" className="navbar__btn" onClick={() => showUnavailable('스크랩')}>
            스크랩
          </button>
          <button type="button" className="navbar__btn" onClick={() => showUnavailable('신고')}>
            신고
          </button>
          <button type="button" className="navbar__btn" onClick={() => showUnavailable('수정')}>
            수정
          </button>
          <button type="button" className="navbar__btn" onClick={() => showUnavailable('삭제')}>
            삭제
          </button>
        </div>
        {notice && <p className="post-detail__notice">{notice}</p>}
      </article>

      <nav className="post-detail__siblings" aria-label="이전글 다음글">
        {nextPost ? (
          <Link to={`${boardPath}/post/${nextPost.id}`} state={{ post: nextPost }} className="post-detail__sibling">
            <span className="post-detail__sibling-label">다음글</span>
            <span className="post-detail__sibling-title">{nextPost.title}</span>
          </Link>
        ) : (
          <div className="post-detail__sibling post-detail__sibling--disabled">
            <span className="post-detail__sibling-label">다음글</span>
            <span className="post-detail__sibling-title">없음</span>
          </div>
        )}
        {prevPost ? (
          <Link to={`${boardPath}/post/${prevPost.id}`} state={{ post: prevPost }} className="post-detail__sibling">
            <span className="post-detail__sibling-label">이전글</span>
            <span className="post-detail__sibling-title">{prevPost.title}</span>
          </Link>
        ) : (
          <div className="post-detail__sibling post-detail__sibling--disabled">
            <span className="post-detail__sibling-label">이전글</span>
            <span className="post-detail__sibling-title">없음</span>
          </div>
        )}
      </nav>

      <section className="post-comments">
        <h3 className="post-comments__title">댓글 {comments.length}</h3>
        <ul className="post-comments__list">
          {comments.map((c) => (
            <li key={c.id} className="post-comments__item">
              <div className="post-comments__item-head">
                <span className="post-comments__author">{c.author}</span>
                <span className="post-comments__date">{c.date}</span>
              </div>
              <p className="post-comments__body">{c.content}</p>
            </li>
          ))}
        </ul>
        <form className="post-comments__form" onSubmit={handleCommentSubmit}>
          <textarea
            className="board-write__textarea"
            value={commentDraft}
            onChange={(event) => setCommentDraft(event.target.value)}
            rows={3}
            placeholder="댓글을 입력하세요"
          />
          <button type="submit" className="navbar__btn navbar__btn--primary">
            등록
          </button>
        </form>
      </section>

      <div className="board-toolbar">
        <Link to={boardPath} className="navbar__btn">
          목록
        </Link>
      </div>
    </>
  )
}

export default PostDetailPage
