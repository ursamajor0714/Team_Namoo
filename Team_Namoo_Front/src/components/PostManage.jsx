import { useCallback, useEffect, useState } from 'react'
import Modal from './Modal'
import { PARTIES } from '../constants/parties'
import { STATUS_LABEL } from '../constants/adminMembers'
import {
  adminErrorMessage,
  fetchMembers,
  fetchPosts,
  updateMemberStatus,
  updatePostPinned,
  updatePostVisibilityBulk,
} from '../api/adminApi'

const BOARD_IDS = [1, 2, 3, 4, 5]
const boardName = (id) => `게시판${id}`

/** 서버 PostVisibility → 화면 표기. '삭제'도 실제 삭제가 아니라 회원 비공개 처리다. */
const VISIBILITY_LABEL = { NORMAL: '정상', DELETED: '삭제', HIDDEN: '감춤' }

/** 서버가 주는 ISO 시각 → 'YYYY-MM-DD' */
const day = (v) => (v ? String(v).slice(0, 10) : '—')

/**
 * 글쓴이 상태 모달. 게시글 API 는 글쓴이 닉네임만 주므로 닉네임으로 회원을 찾아 상태를 바꾼다.
 * @param {{ author: string, onClose: () => void }} props
 */
function AuthorModal({ author, onClose }) {
  const [member, setMember] = useState(null)
  const [message, setMessage] = useState('회원 정보를 불러오는 중...')
  const [busy, setBusy] = useState(false)
  /** 상태를 바꾼 뒤 다시 읽어오기 위한 카운터. */
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    fetchMembers({ field: 'nickname', q: author })
      .then((page) => {
        if (cancelled) {
          return
        }
        const found = (page.content ?? []).find((m) => m.nickname === author) ?? null
        setMember(found)
        setMessage(found ? '' : '이 닉네임의 회원을 찾지 못했습니다.')
      })
      .catch((error) => {
        if (cancelled) {
          return
        }
        setMember(null)
        setMessage(adminErrorMessage(error, '회원 정보를 불러오지 못했습니다.'))
      })
    return () => {
      cancelled = true
    }
  }, [author, reloadKey])

  async function toggleSuspend() {
    if (!member) {
      return
    }
    setBusy(true)
    try {
      await updateMemberStatus(member.id, member.status === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED')
      setReloadKey((n) => n + 1)
    } catch (error) {
      setMessage(adminErrorMessage(error, '상태를 바꾸지 못했습니다.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal title={`글쓴이 · ${author}`} onClose={onClose}>
      <p className="modal__text">
        현재 상태: <strong>{member ? STATUS_LABEL[member.status] : '—'}</strong>
        {message && (
          <>
            <br />
            <span className="pm__note">{message}</span>
          </>
        )}
      </p>
      <div className="modal__actions">
        <button type="button" className="btn btn--ghost" onClick={onClose}>
          닫기
        </button>
        <button
          type="button"
          className="btn btn--primary"
          onClick={toggleSuspend}
          disabled={busy || !member}
        >
          {member?.status === 'SUSPENDED' ? '정지 해제' : '정지'}
        </button>
      </div>
    </Modal>
  )
}

/**
 * 관리자 - 게시글 관리 탭.
 * 좌측 트리(정당 → 게시판)에서 게시판을 고르면 서버에서 그 게시판 글을 받아온다.
 * 삭제/감추기/복구는 노출 상태 변경이고, 실제로 글이 지워지지는 않는다.
 */
function PostManage() {
  const [openParty, setOpenParty] = useState(PARTIES[0])
  const [board, setBoard] = useState(null) // { party, boardId }
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [checked, setChecked] = useState(() => new Set())
  const [authorModal, setAuthorModal] = useState(null)

  const load = useCallback(async (target) => {
    if (!target) {
      return
    }
    setLoading(true)
    try {
      setPosts(await fetchPosts(target.party, target.boardId))
      setError('')
    } catch (err) {
      setPosts([])
      setError(adminErrorMessage(err, '게시글을 불러오지 못했습니다.'))
    } finally {
      setLoading(false)
    }
  }, [])

  function selectBoard(party, boardId) {
    const target = { party, boardId }
    setBoard(target)
    setChecked(new Set())
    load(target)
  }

  function toggle(id) {
    setChecked((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  async function apply(nextVisibility) {
    if (checked.size === 0) {
      return
    }
    try {
      await updatePostVisibilityBulk([...checked], nextVisibility)
      setChecked(new Set())
      await load(board)
    } catch (err) {
      setError(adminErrorMessage(err, '상태를 바꾸지 못했습니다.'))
    }
  }

  /** 선택한 글을 공지로 지정하거나 해제한다. */
  async function applyPinned(pinned) {
    if (checked.size === 0) {
      return
    }
    try {
      await Promise.all([...checked].map((id) => updatePostPinned(id, pinned)))
      setChecked(new Set())
      await load(board)
    } catch (err) {
      setError(adminErrorMessage(err, '공지 설정을 바꾸지 못했습니다.'))
    }
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
                    const active = board && board.party === party && board.boardId === id
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
              <button type="button" className="pm__action" onClick={() => apply('DELETED')}>
                삭제
              </button>
              <button type="button" className="pm__action" onClick={() => apply('HIDDEN')}>
                감추기
              </button>
              <button type="button" className="pm__action" onClick={() => apply('NORMAL')}>
                복구
              </button>
              <button type="button" className="pm__action" onClick={() => applyPinned(true)}>
                공지 지정
              </button>
              <button type="button" className="pm__action" onClick={() => applyPinned(false)}>
                공지 해제
              </button>
              <span className="pm__note">
                삭제해도 실제로 지워지지 않고 회원 화면에서만 비공개됩니다.
              </span>
            </div>

            {error && <p className="pm__note">{error}</p>}

            {loading ? (
              <p className="pm__hint">불러오는 중...</p>
            ) : posts.length === 0 ? (
              <p className="pm__hint">이 게시판에는 글이 없습니다.</p>
            ) : (
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
                  {posts.map((post) => (
                    <tr
                      key={post.id}
                      className={
                        post.visibility === 'NORMAL' ? 'pm-row' : 'pm-row pm-row--muted'
                      }
                    >
                      <td className="pm-list__col-check">
                        <input
                          type="checkbox"
                          checked={checked.has(post.id)}
                          onChange={() => toggle(post.id)}
                          aria-label={`${post.title} 선택`}
                        />
                      </td>
                      <td className="pm-list__col-num">{post.num}</td>
                      <td className="pm-list__col-title">
                        <span className="pm-status">
                          ({VISIBILITY_LABEL[post.visibility] ?? post.visibility})
                        </span>{' '}
                        {post.pinned && '[공지] '}
                        {post.title}
                      </td>
                      <td className="pm-list__col-author">
                        <button
                          type="button"
                          className="pm-author"
                          onClick={() => setAuthorModal(post.author)}
                        >
                          {post.author}
                        </button>
                      </td>
                      <td className="pm-list__col-date">{day(post.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </>
        )}
      </div>

      {authorModal && (
        <AuthorModal author={authorModal} onClose={() => setAuthorModal(null)} />
      )}
    </section>
  )
}

export default PostManage
