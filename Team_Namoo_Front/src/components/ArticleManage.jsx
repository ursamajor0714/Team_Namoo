import { useCallback, useEffect, useState } from 'react'
import { PARTIES } from '../constants/parties'
import {
  adminErrorMessage,
  fetchArticles,
  updateArticleVisibilityBulk,
} from '../api/adminApi'

/** 검색어 입력이 멈춘 뒤 서버를 호출하기까지 기다리는 시간(ms). */
const SEARCH_DEBOUNCE_MS = 300

const SEARCH_SCOPES = [
  { value: 'title', label: '글제목' },
  { value: 'content', label: '내용' },
]

/** 서버 ArticleVisibility → 화면 표기. */
const VISIBILITY_LABEL = { NORMAL: '정상', DELETED: '삭제', HIDDEN: '감추기' }

/**
 * 관리자 - 기사관리 탭.
 * 정당(카테고리)별로 서버(/api/admin/articles)에서 기사를 받아 노출 상태를 바꾼다.
 * 검색(제목/내용)도 서버가 수행한다.
 * 관리자 기사 API 는 본문을 내려주지 않으므로, 제목을 누르면 원문을 새 탭으로 연다.
 */
function ArticleManage() {
  const [articles, setArticles] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [activeParty, setActiveParty] = useState(PARTIES[0])
  const [selected, setSelected] = useState(() => new Set())
  const [scope, setScope] = useState('title')
  const [term, setTerm] = useState('')

  const load = useCallback(async (party, searchScope, searchTerm) => {
    setLoading(true)
    try {
      const q = searchTerm.trim()
      setArticles(await fetchArticles({ party, scope: searchScope, q: q || undefined }))
      setError('')
    } catch (err) {
      setArticles([])
      setError(adminErrorMessage(err, '기사를 불러오지 못했습니다.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => load(activeParty, scope, term), SEARCH_DEBOUNCE_MS)
    return () => clearTimeout(timer)
  }, [load, activeParty, scope, term])

  /** 선택된 기사들의 노출 상태를 한 번에 바꾼다. */
  async function applyVisibility(next) {
    if (selected.size === 0) {
      return
    }
    try {
      await updateArticleVisibilityBulk([...selected], next)
      setSelected(new Set())
      await load(activeParty, scope, term)
    } catch (err) {
      setError(adminErrorMessage(err, '상태를 바꾸지 못했습니다.'))
    }
  }

  function toggle(id) {
    setSelected((prev) => {
      const nextSet = new Set(prev)
      if (nextSet.has(id)) {
        nextSet.delete(id)
      } else {
        nextSet.add(id)
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

  return (
    <section className="am">
      <nav className="am__cats" aria-label="카테고리">
        {PARTIES.map((party) => (
          <button
            key={party}
            type="button"
            className={party === activeParty ? 'am__cat am__cat--active' : 'am__cat'}
            onClick={() => moveTo(party)}
          >
            {party}
            {party === activeParty && <span className="am__cat-count">{articles.length}</span>}
          </button>
        ))}
      </nav>

      <div className="am__toolbar">
        <span className="am__selected">{selected.size}개 선택</span>
        <button type="button" className="am__action" onClick={() => applyVisibility('DELETED')}>
          삭제
        </button>
        <button type="button" className="am__action" onClick={() => applyVisibility('NORMAL')}>
          복구
        </button>
        <button type="button" className="am__action" onClick={() => applyVisibility('HIDDEN')}>
          감추기
        </button>
        <span className="am__note">감춘 기사는 회원 화면에서 보이지 않습니다.</span>
      </div>

      {error && <p className="am__status am__status--error">{error}</p>}

      {loading ? (
        <p className="am__status">불러오는 중...</p>
      ) : articles.length === 0 ? (
        <p className="am-group__empty">표시할 기사가 없습니다.</p>
      ) : (
        <ul className="am-list">
          {articles.map((article, index) => (
            <li
              key={article.id}
              className={article.visibility === 'NORMAL' ? 'am-row' : 'am-row am-row--muted'}
            >
              <input
                type="checkbox"
                className="am-row__check"
                checked={selected.has(article.id)}
                onChange={() => toggle(article.id)}
                aria-label={`${article.title} 선택`}
              />
              <span className="am-row__no">{index + 1}</span>
              <a
                className="am-row__title"
                href={article.originalLink || article.link}
                target="_blank"
                rel="noreferrer"
              >
                <span className="am-row__status">
                  ({VISIBILITY_LABEL[article.visibility] ?? article.visibility})
                </span>{' '}
                {article.title}
              </a>
            </li>
          ))}
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
    </section>
  )
}

export default ArticleManage
