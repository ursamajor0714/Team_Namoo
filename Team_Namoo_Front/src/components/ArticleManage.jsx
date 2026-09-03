import { useEffect, useMemo, useState } from 'react'
import NewsModal from './NewsModal'
import { fetchNewsByLeaning } from '../api/newsApi'
import { PARTIES, PARTY_LEANING } from '../constants/parties'

const PER_PARTY = 20

const SEARCH_SCOPES = [
  { value: 'title', label: '글제목' },
  { value: 'content', label: '내용' },
]

/** 상태 3종. 관리자가 선택 기사에 일괄 적용한다(화면 state 에만 반영). */
const STATUS = { NORMAL: '정상', DELETED: '삭제', HIDDEN: '감추기' }

/**
 * 정당(카테고리)별로 API 기사를 받아 게시글 목록 형태로 편다.
 * 상단에 카테고리 이동 버튼 → 그 아래 삭제/복구/감추기 → 목록 → 하단 검색바.
 * 기사에는 백엔드 id/상태가 없어 originalLink 를 키로 쓰고 상태는 목으로 관리한다.
 * 제목을 누르면 원문이 아니라 우리 뉴스 팝업(NewsModal)을 띄운다.
 */
function ArticleManage() {
  const [rows, setRows] = useState([])
  // 상태는 기사(originalLink) 단위로 저장한다. 같은 기사가 여러 정당에 걸쳐 있어도
  // 한 곳에서 바꾸면 나머지 정당에서도 같이 반영된다.
  const [statusByLink, setStatusByLink] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [activeParty, setActiveParty] = useState(PARTIES[0])
  const [selected, setSelected] = useState(() => new Set())
  const [scope, setScope] = useState('title')
  const [term, setTerm] = useState('')
  const [detailArticle, setDetailArticle] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const lists = await Promise.all(
          PARTIES.map((party) =>
            fetchNewsByLeaning({
              leaning: PARTY_LEANING[party],
              partyKeyword: party,
              count: PER_PARTY,
            }).catch(() => []),
          ),
        )
        if (cancelled) {
          return
        }
        const flat = lists.flatMap((list, partyIdx) => {
          const party = PARTIES[partyIdx]
          return list.map((a, i) => ({
            key: `${party}|${a.originalLink}`,
            no: i + 1,
            party,
            article: a,
          }))
        })
        setRows(flat)
      } catch {
        if (!cancelled) {
          setError('기사를 불러오지 못했습니다.')
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  /** 선택된 기사들의 상태를 next 로 바꾸고 선택 해제. originalLink 단위라 타 정당에도 반영된다. */
  function applyStatus(next) {
    if (selected.size === 0) {
      return
    }
    const links = rows
      .filter((r) => selected.has(r.key))
      .map((r) => r.article.originalLink)
    setStatusByLink((prev) => {
      const nextMap = { ...prev }
      for (const link of links) {
        nextMap[link] = next
      }
      return nextMap
    })
    setSelected(new Set())
  }

  /** @param {object} article @returns {string} */
  const statusOf = (article) => statusByLink[article.originalLink] ?? STATUS.NORMAL

  function toggle(key) {
    setSelected((prev) => {
      const nextSet = new Set(prev)
      if (nextSet.has(key)) {
        nextSet.delete(key)
      } else {
        nextSet.add(key)
      }
      return nextSet
    })
  }

  /** 카테고리를 옮기면 선택/검색은 초기화한다. */
  function moveTo(party) {
    setActiveParty(party)
    setSelected(new Set())
    setTerm('')
  }

  const partyCounts = useMemo(() => {
    const map = {}
    for (const r of rows) {
      map[r.party] = (map[r.party] ?? 0) + 1
    }
    return map
  }, [rows])

  const visible = useMemo(() => {
    const q = term.trim().toLowerCase()
    return rows.filter((r) => {
      if (r.party !== activeParty) {
        return false
      }
      if (!q) {
        return true
      }
      const text =
        scope === 'title'
          ? r.article.title
          : r.article.content || r.article.description || ''
      return text.toLowerCase().includes(q)
    })
  }, [rows, activeParty, scope, term])

  if (loading) {
    return <p className="am__status">불러오는 중...</p>
  }
  if (error) {
    return <p className="am__status am__status--error">{error}</p>
  }

  return (
    <section className="am">
      <nav className="am__cats" aria-label="카테고리">
        {PARTIES.map((party) => (
          <button
            key={party}
            type="button"
            className={
              party === activeParty
                ? 'am__cat am__cat--active'
                : 'am__cat'
            }
            onClick={() => moveTo(party)}
          >
            {party}
            <span className="am__cat-count">{partyCounts[party] ?? 0}</span>
          </button>
        ))}
      </nav>

      <div className="am__toolbar">
        <span className="am__selected">{selected.size}개 선택</span>
        <button
          type="button"
          className="am__action"
          onClick={() => applyStatus(STATUS.DELETED)}
        >
          삭제
        </button>
        <button
          type="button"
          className="am__action"
          onClick={() => applyStatus(STATUS.NORMAL)}
        >
          복구
        </button>
        <button
          type="button"
          className="am__action"
          onClick={() => applyStatus(STATUS.HIDDEN)}
        >
          감추기
        </button>
        <span className="am__note">상태 변경은 화면에만 반영되고 저장되지 않습니다.</span>
      </div>

      {visible.length === 0 ? (
        <p className="am-group__empty">표시할 기사가 없습니다.</p>
      ) : (
        <ul className="am-list">
          {visible.map((r) => {
            const st = statusOf(r.article)
            return (
              <li
                key={r.key}
                className={st === STATUS.NORMAL ? 'am-row' : 'am-row am-row--muted'}
              >
                <input
                  type="checkbox"
                  className="am-row__check"
                  checked={selected.has(r.key)}
                  onChange={() => toggle(r.key)}
                  aria-label={`${r.article.title} 선택`}
                />
                <span className="am-row__no">{r.no}</span>
                <button
                  type="button"
                  className="am-row__title"
                  onClick={() => setDetailArticle(r.article)}
                >
                  <span className="am-row__status">({st})</span> {r.article.title}
                </button>
              </li>
            )
          })}
        </ul>
      )}

      <div className="am__search">
        <select
          className="am__search-scope"
          value={scope}
          onChange={(e) => setScope(e.target.value)}
          aria-label="검색 범위"
        >
          {SEARCH_SCOPES.map((s) => (
            <option key={s.value} value={s.value}>
              {s.label}
            </option>
          ))}
        </select>
        <input
          className="am__search-input"
          type="search"
          value={term}
          onChange={(e) => setTerm(e.target.value)}
          placeholder="검색어 입력"
          aria-label="검색어"
        />
      </div>

      <NewsModal
        key={detailArticle?.originalLink}
        article={detailArticle}
        onClose={() => setDetailArticle(null)}
      />
    </section>
  )
}

export default ArticleManage
