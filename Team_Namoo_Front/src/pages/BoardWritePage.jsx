import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

function BoardWritePage() {
  const { name, boardId } = useParams()
  const navigate = useNavigate()
  const user = useAuthStore((state) => state.user)

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')

  const boardPath = `/party/${encodeURIComponent(name)}/board/${boardId}`

  const handleSubmit = (event) => {
    event.preventDefault()
    if (!title.trim() || !content.trim()) {
      return
    }

    navigate(boardPath, {
      state: {
        newPost: {
          title: title.trim(),
          author: user?.nickname ?? '익명',
          content: content.trim(),
        },
      },
    })
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
        <div className="board-write__actions">
          <button
            type="button"
            className="navbar__btn"
            onClick={() => navigate(boardPath)}
          >
            취소
          </button>
          <button type="submit" className="navbar__btn navbar__btn--primary">
            등록
          </button>
        </div>
      </form>
    </>
  )
}

export default BoardWritePage
