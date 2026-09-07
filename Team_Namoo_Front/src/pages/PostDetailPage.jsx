import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Modal from '../components/Modal'
import {
  boardErrorMessage,
  createComment,
  fetchBoardPosts,
  fetchComments,
  fetchPost,
  reportComment,
  reportPost,
  votePost,
} from '../api/boardApi'

/** 서버가 주는 ISO 시각 → 'YYYY-MM-DD HH:mm' */
function fullDate(value) {
  if (!value) {
    return '—'
  }
  return String(value).slice(0, 16).replace('T', ' ')
}

/**
 * 신고 사유 입력 모달. 게시글과 댓글이 같은 폼을 쓴다.
 * @param {{ label: string, onSubmit: (reason: string) => Promise<void>, onClose: () => void }} props
 */
function ReportModal({ label, onSubmit, onClose }) {
  const [reason, setReason] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function submit() {
    if (!reason.trim()) {
      setError('신고 사유를 입력해주세요.')
      return
    }
    setBusy(true)
    setError('')
    try {
      await onSubmit(reason.trim())
    } catch (err) {
      setError(boardErrorMessage(err, '신고하지 못했습니다.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title={`${label} 신고`} onClose={onClose}>
      <textarea
        className="board-write__textarea"
        value={reason}
        onChange={(event) => setReason(event.target.value)}
        rows={4}
        placeholder="신고 사유를 입력하세요"
      />
      {error && <p className="post-detail__notice">{error}</p>}
      <div className="modal__actions">
        <button type="button" className="btn btn--ghost" onClick={onClose} disabled={busy}>
          취소
        </button>
        <button type="button" className="btn btn--primary" onClick={submit} disabled={busy}>
          {busy ? '신고 중...' : '신고'}
        </button>
      </div>
    </Modal>
  )
}

/**
 * 게시글 상세.
 * 글·댓글·추천은 서버와 주고받는다. 신고는 사유를 받아 POST 한다.
 * 스크랩/수정/삭제는 아직 백엔드 API 가 없어 안내만 띄운다.
 */
function PostDetailPage() {
  const { name, boardId, postId } = useParams()
  const boardPath = `/party/${encodeURIComponent(name)}/board/${boardId}`

  const [post, setPost] = useState(null)
  const [comments, setComments] = useState([])
  const [siblings, setSiblings] = useState([])
  const [loadError, setLoadError] = useState('')
  const [loading, setLoading] = useState(true)

  const [myVote, setMyVote] = useState(null)
  const [notice, setNotice] = useState('')
  const [commentDraft, setCommentDraft] = useState('')
  const [reportTarget, setReportTarget] = useState(null) // { type: 'POST'|'COMMENT', id, label }

  useEffect(() => {
    let cancelled = false
    Promise.all([
      fetchPost(postId),
      fetchComments(postId),
      fetchBoardPosts(name, boardId).catch(() => ({ posts: [] })),
    ])
      .then(([detail, commentList, board]) => {
        if (cancelled) {
          return
        }
        setPost(detail)
        setComments(commentList ?? [])
        setSiblings(board.posts ?? [])
        setLoadError('')
        setLoading(false)
      })
      .catch((err) => {
        if (cancelled) {
          return
        }
        setLoadError(boardErrorMessage(err, '글을 불러오지 못했습니다.'))
        setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [postId, name, boardId])

  if (loading) {
    return <p className="post-detail__missing">불러오는 중...</p>
  }

  if (!post) {
    return (
      <>
        <h1 className="party-page__title">
          <Link to={boardPath} className="board-home-link">
            {name} · 게시판{boardId}
          </Link>
        </h1>
        <p className="post-detail__missing">
          {loadError || '글을 찾을 수 없습니다. 삭제되었거나 감춰진 글일 수 있습니다.'}
        </p>
        <div className="board-toolbar">
          <Link to={boardPath} className="navbar__btn">
            목록
          </Link>
        </div>
      </>
    )
  }

  async function handleVote(type) {
    try {
      const result = await votePost(postId, type)
      setPost((prev) => ({ ...prev, likes: result.likes, dislikes: result.dislikes }))
      setMyVote(result.myVote ?? null)
      setNotice('')
    } catch (err) {
      setNotice(boardErrorMessage(err, '추천하지 못했습니다.'))
    }
  }

  const showUnavailable = (label) => {
    setNotice(`'${label}' 기능은 백엔드 API 가 아직 없어 동작하지 않습니다.`)
  }

  async function handleCommentSubmit(event) {
    event.preventDefault()
    if (!commentDraft.trim()) {
      return
    }
    try {
      await createComment(postId, commentDraft.trim())
      setComments(await fetchComments(postId))
      setCommentDraft('')
      setNotice('')
    } catch (err) {
      setNotice(boardErrorMessage(err, '댓글을 등록하지 못했습니다.'))
    }
  }

  async function submitReport(reason) {
    if (reportTarget.type === 'POST') {
      await reportPost(reportTarget.id, reason)
    } else {
      await reportComment(reportTarget.id, reason)
    }
    setReportTarget(null)
    setNotice('신고가 접수되었습니다.')
  }

  // 목록은 최신순이라 앞쪽이 다음글(최신), 뒤쪽이 이전글(과거).
  const currentIndex = siblings.findIndex((p) => String(p.id) === String(postId))
  const nextPost = currentIndex > 0 ? siblings[currentIndex - 1] : null
  const prevPost =
    currentIndex >= 0 && currentIndex < siblings.length - 1 ? siblings[currentIndex + 1] : null

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
          <span>{fullDate(post.createdAt)}</span>
          <span>조회 {post.views}</span>
          <span>추천 {post.likes}</span>
          <span>댓글 {comments.length}</span>
        </div>

        <div className="post-detail__content">
          {String(post.content ?? '').split('\n').map((line, i) => (
            <p key={i}>{line || ' '}</p>
          ))}
        </div>

        <div className="post-detail__vote">
          <button
            type="button"
            className={`post-detail__vote-btn${myVote === 'LIKE' ? ' post-detail__vote-btn--active' : ''}`}
            onClick={() => handleVote('LIKE')}
          >
            ▲ 추천 {post.likes}
          </button>
          <button
            type="button"
            className={`post-detail__vote-btn${myVote === 'DISLIKE' ? ' post-detail__vote-btn--active' : ''}`}
            onClick={() => handleVote('DISLIKE')}
          >
            ▼ 비추천 {post.dislikes}
          </button>
        </div>

        <div className="post-detail__actions">
          <button type="button" className="navbar__btn" onClick={() => showUnavailable('스크랩')}>
            스크랩
          </button>
          <button
            type="button"
            className="navbar__btn"
            onClick={() => setReportTarget({ type: 'POST', id: postId, label: '게시글' })}
          >
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
          <Link to={`${boardPath}/post/${nextPost.id}`} className="post-detail__sibling">
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
          <Link to={`${boardPath}/post/${prevPost.id}`} className="post-detail__sibling">
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
                <span className="post-comments__date">{fullDate(c.createdAt)}</span>
                <button
                  type="button"
                  className="post-comments__report"
                  onClick={() => setReportTarget({ type: 'COMMENT', id: c.id, label: '댓글' })}
                >
                  신고
                </button>
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

      {reportTarget && (
        <ReportModal
          label={reportTarget.label}
          onSubmit={submitReport}
          onClose={() => setReportTarget(null)}
        />
      )}
    </>
  )
}

export default PostDetailPage
