import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { boardErrorMessage, createPost } from '../api/boardApi'

/**
 * 글쓰기. 서버에 글을 만들고(POST) 성공하면 그 글의 상세로 이동한다.
 * 지지정당 게시판이 아니거나 비로그인이면 서버가 한글 메시지로 거절한다.
 */
function BoardWritePage() {
  const { name, boardId } = useParams()
  const navigate = useNavigate()

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const boardPath = `/party/${encodeURIComponent(name)}/board/${boardId}`

  async function handleSubmit(event) {
    event.preventDefault()
    if (!title.trim() || !content.trim() || busy) {
      return
    }
    setBusy(true)
    setError('')
    try {
      const created = await createPost(name, boardId, {
        title: title.trim(),
        content: content.trim(),
      })
      navigate(`${boardPath}/post/${created.id}`)
    } catch (err) {
      setError(boardErrorMessage(err, '글을 등록하지 못했습니다.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <h1 className="party-page__title">
        {name} · 게시판{boardId} 글쓰기
      </h1>
      <form className="board-write" onSubmit={handleSubmit}>
        <div className="board-write__row">
          <label className="board-write__label" htmlFor="board-write-title">
            제목
          </label>
          <input
            id="board-write-title"
            className="board-write__input"
            type="text"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            maxLength={100}
            required
          />
        </div>
        <div className="board-write__row">
          <label className="board-write__label" htmlFor="board-write-content">
            내용
          </label>
          <textarea
            id="board-write-content"
            className="board-write__textarea"
            value={content}
            onChange={(event) => setContent(event.target.value)}
            rows={12}
            required
          />
        </div>
        {error && <p className="post-detail__notice">{error}</p>}
        <div className="board-write__actions">
          <button
            type="button"
            className="navbar__btn"
            onClick={() => navigate(boardPath)}
            disabled={busy}
          >
            취소
          </button>
          <button type="submit" className="navbar__btn navbar__btn--primary" disabled={busy}>
            {busy ? '등록 중...' : '등록'}
          </button>
        </div>
      </form>
    </>
  )
}

export default BoardWritePage
