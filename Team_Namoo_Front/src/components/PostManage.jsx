import { useMemo, useState } from 'react'
import Modal from './Modal'
import { PARTIES } from '../constants/parties'
import { buildDummyPosts } from '../constants/dummyBoardPosts'

const BOARD_IDS = [1, 2, 3, 4, 5]
const boardName = (id) => `게시판${id}`

/** 상태 3종. '삭제'도 실제 삭제가 아니라 회원 비공개 처리다(관리자에겐 계속 보임). */
const STATUS = { NORMAL: '정상', DELETED: '삭제', HIDDEN: '감춤' }

/**
 * 관리자 - 게시글 관리 탭.
 * 좌측 트리(정당 → 게시판) 에서 게시판을 고르면 그 게시판 글이 열린다.
 * 게시판 글은 더미(dummyBoardPosts) 다. 삭제/감추기/복구는 화면 state 에만 반영되고,
 * '삭제'여도 실제로 지워지지 않고 회원 화면에서만 비공개된다는 개념이다.
 */
function PostManage() {
  const [openParty, setOpenParty] = useState(PARTIES[0])
  const [board, setBoard] = useState(null) // { party, boardId }
  const [statusByPost, setStatusByPost] = useState({}) // `${party}|${boardId}|${postId}` -> status
  const [checked, setChecked] = useState(() => new Set())
  const [suspended, setSuspended] = useState(() => new Set()) // 정지된 글쓴이 이름
  const [authorModal, setAuthorModal] = useState(null) // 클릭한 글쓴이 이름

  const posts = useMemo(
    () => (board ? buildDummyPosts(board.boardId) : []),
    [board],
  )

  const postKey = (post) => `${board.party}|${board.boardId}|${post.id}`
  const statusOf = (post) => statusByPost[postKey(post)] ?? STATUS.NORMAL

  function selectBoard(party, boardId) {
    setBoard({ party, boardId })
    setChecked(new Set())
  }

  function toggle(post) {
    setChecked((prev) => {
      const next = new Set(prev)
      const k = postKey(post)
      if (next.has(k)) {
        next.delete(k)
      } else {
        next.add(k)
      }
      return next
    })
  }

  function apply(nextStatus) {
    if (checked.size === 0) {
      return
    }
    setStatusByPost((prev) => {
      const next = { ...prev }
      for (const k of checked) {
        next[k] = nextStatus
      }
      return next
    })
    setChecked(new Set())
  }

  /** 글쓴이 정지/해제 토글. */
  function toggleSuspend(author) {
    setSuspended((prev) => {
      const next = new Set(prev)
      if (next.has(author)) {
        next.delete(author)
      } else {
        next.add(author)
      }
      return next
    })
  }

  return (
    <section className="pm">
      <nav className="pm__tree" aria-label="게시판 트리">
        {PARTIES.map((party) => {
          const open = openParty === party
          return (
            <div key={party} className="pm-tree__party">
              <button
                type="button"
                className="pm-tree__party-btn"
                aria-expanded={open}
                onClick={() => setOpenParty(open ? null : party)}
              >
                <span className="pm-tree__caret">{open ? '▾' : '▸'}</span>
                {party}
              </button>
              {open && (
                <ul className="pm-tree__boards">
                  {BOARD_IDS.map((id) => {
                    const active =
                      board && board.party === party && board.boardId === id
                    return (
                      <li key={id}>
                        <button
                          type="button"
                          className={
                            active
                              ? 'pm-tree__board pm-tree__board--active'
                              : 'pm-tree__board'
                          }
                          onClick={() => selectBoard(party, id)}
                        >
                          {boardName(id)}
                        </button>
                      </li>
                    )
                  })}
                </ul>
              )}
            </div>
          )
        })}
      </nav>

      <div className="pm__main">
        {!board ? (
          <p className="pm__hint">좌측에서 정당 → 게시판을 선택하세요.</p>
        ) : (
          <>
            <h2 className="pm__title">
              {board.party} · {boardName(board.boardId)}
            </h2>

            <div className="pm__toolbar">
              <span className="pm__selected">{checked.size}개 선택</span>
              <button
                type="button"
                className="pm__action"
                onClick={() => apply(STATUS.DELETED)}
              >
                삭제
              </button>
              <button
                type="button"
                className="pm__action"
                onClick={() => apply(STATUS.HIDDEN)}
              >
                감추기
              </button>
              <button
                type="button"
                className="pm__action"
                onClick={() => apply(STATUS.NORMAL)}
              >
                복구
              </button>
              <span className="pm__note">
                삭제해도 실제로 지워지지 않고 회원 화면에서만 비공개됩니다.
              </span>
            </div>

            <table className="pm-list">
              <thead>
                <tr>
                  <th className="pm-list__col-check" />
                  <th className="pm-list__col-num">번호</th>
                  <th className="pm-list__col-title">제목</th>
                  <th className="pm-list__col-author">글쓴이</th>
                  <th className="pm-list__col-date">작성일</th>
                </tr>
              </thead>
              <tbody>
                {posts.map((post) => {
                  const st = statusOf(post)
                  return (
                    <tr
                      key={post.id}
                      className={
                        st === STATUS.NORMAL ? 'pm-row' : 'pm-row pm-row--muted'
                      }
                    >
                      <td className="pm-list__col-check">
                        <input
                          type="checkbox"
                          checked={checked.has(postKey(post))}
                          onChange={() => toggle(post)}
                          aria-label={`${post.title} 선택`}
                        />
                      </td>
                      <td className="pm-list__col-num">{post.num}</td>
                      <td className="pm-list__col-title">
                        <span className="pm-status">({st})</span> {post.title}
                      </td>
                      <td className="pm-list__col-author">
                        <button
                          type="button"
                          className={
                            suspended.has(post.author)
                              ? 'pm-author pm-author--suspended'
                              : 'pm-author'
                          }
                          onClick={() => setAuthorModal(post.author)}
                        >
                          {post.author}
                          {suspended.has(post.author) && ' (정지)'}
                        </button>
                      </td>
                      <td className="pm-list__col-date">{post.date}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </>
        )}
      </div>

      {authorModal && (
        <Modal title={`글쓴이 · ${authorModal}`} onClose={() => setAuthorModal(null)}>
          <p className="modal__text">
            현재 상태:{' '}
            <strong>{suspended.has(authorModal) ? '정지' : '정상'}</strong>
            <br />
            <span className="pm__note">목 데이터입니다. 저장되지 않습니다.</span>
          </p>
          <div className="modal__actions">
            <button
              type="button"
              className="btn btn--ghost"
              onClick={() => setAuthorModal(null)}
            >
              닫기
            </button>
            <button
              type="button"
              className="btn btn--primary"
              onClick={() => toggleSuspend(authorModal)}
            >
              {suspended.has(authorModal) ? '정지 해제' : '정지'}
            </button>
          </div>
        </Modal>
      )}
    </section>
  )
}

export default PostManage
