import { useEffect, useState } from 'react'
import { adminErrorMessage, fetchReports, updateReportStatus } from '../api/adminApi'

/** 서버 ReportStatus → 화면 표기. */
const STATUS_LABEL = { PENDING: '대기', RESOLVED: '처리완료', REJECTED: '반려' }

/** 서버 ReportTargetType → 화면 표기. */
const TARGET_LABEL = { POST: '게시글', COMMENT: '댓글' }

const STATUS_FILTERS = [
  { value: '', label: '전체 상태' },
  { value: 'PENDING', label: '대기' },
  { value: 'RESOLVED', label: '처리완료' },
  { value: 'REJECTED', label: '반려' },
]

const TARGET_FILTERS = [
  { value: '', label: '전체 대상' },
  { value: 'POST', label: '게시글' },
  { value: 'COMMENT', label: '댓글' },
]

/** 서버가 주는 ISO 시각 → 'YYYY-MM-DD HH:mm' */
const at = (v) => (v ? String(v).slice(0, 16).replace('T', ' ') : '—')

/**
 * 관리자 - 신고관리 탭.
 * 회원이 올린 신고를 상태/대상으로 걸러 보고, 대기 중인 건을 처리완료 또는 반려로 넘긴다.
 */
function ReportManage() {
  const [reports, setReports] = useState([])
  const [total, setTotal] = useState(0)
  const [status, setStatus] = useState('PENDING')
  const [targetType, setTargetType] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  /** 처리/반려 후 목록을 다시 읽기 위한 카운터. */
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    fetchReports({
      status: status || undefined,
      targetType: targetType || undefined,
    })
      .then((page) => {
        if (cancelled) {
          return
        }
        setReports(page.content ?? [])
        setTotal(page.totalElements ?? 0)
        setError('')
        setLoading(false)
      })
      .catch((err) => {
        if (cancelled) {
          return
        }
        setReports([])
        setTotal(0)
        setError(adminErrorMessage(err, '신고 목록을 불러오지 못했습니다.'))
        setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [status, targetType, reloadKey])

  async function decide(id, nextStatus) {
    try {
      await updateReportStatus(id, nextStatus)
      setReloadKey((n) => n + 1)
    } catch (err) {
      setError(adminErrorMessage(err, '신고를 처리하지 못했습니다.'))
    }
  }

  return (
    <section className="rm">
      <div className="rm__filters">
        <select
          className="mm__search-field"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          aria-label="처리 상태"
        >
          {STATUS_FILTERS.map((f) => (
            <option key={f.value} value={f.value}>
              {f.label}
            </option>
          ))}
        </select>
        <select
          className="mm__search-field"
          value={targetType}
          onChange={(e) => setTargetType(e.target.value)}
          aria-label="신고 대상"
        >
          {TARGET_FILTERS.map((f) => (
            <option key={f.value} value={f.value}>
              {f.label}
            </option>
          ))}
        </select>
        <span className="mm__count">{total}건</span>
      </div>

      <div className="mm__list">
        <div className="rm-list__inner">
          <div className="rm-list__head">
            <span>대상</span>
            <span>내용</span>
            <span>작성자</span>
            <span>사유</span>
            <span>신고자</span>
            <span>접수일시</span>
            <span>상태</span>
            <span />
          </div>

          {error ? (
            <p className="mm__empty">{error}</p>
          ) : loading ? (
            <p className="mm__empty">불러오는 중...</p>
          ) : reports.length === 0 ? (
            <p className="mm__empty">조건에 맞는 신고가 없습니다.</p>
          ) : (
            reports.map((report) => (
              <article key={report.id} className="rm-row">
                <span className="mm-cell">{TARGET_LABEL[report.targetType] ?? report.targetType}</span>
                <span className="mm-cell" title={report.targetPreview ?? ''}>
                  {report.targetPreview ?? '—'}
                </span>
                <span className="mm-cell">{report.targetAuthor ?? '—'}</span>
                <span className="mm-cell" title={report.reason ?? ''}>
                  {report.reason ?? '—'}
                </span>
                <span className="mm-cell">{report.reporterNickname ?? '—'}</span>
                <span className="mm-cell">{at(report.createdAt)}</span>
                <span className="mm-cell">{STATUS_LABEL[report.status] ?? report.status}</span>
                <span className="rm-row__actions">
                  {report.status === 'PENDING' ? (
                    <>
                      <button
                        type="button"
                        className="btn btn--primary"
                        onClick={() => decide(report.id, 'RESOLVED')}
                      >
                        처리
                      </button>
                      <button
                        type="button"
                        className="btn btn--ghost"
                        onClick={() => decide(report.id, 'REJECTED')}
                      >
                        반려
                      </button>
                    </>
                  ) : (
                    <button
                      type="button"
                      className="btn btn--ghost"
                      onClick={() => decide(report.id, 'PENDING')}
                    >
                      되돌리기
                    </button>
                  )}
                </span>
              </article>
            ))
          )}
        </div>
      </div>
    </section>
  )
}

export default ReportManage
