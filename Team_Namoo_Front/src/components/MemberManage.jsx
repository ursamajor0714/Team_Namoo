import { useCallback, useEffect, useState } from 'react'
import Modal from './Modal'
import { useAuthStore } from '../store/authStore'
import { isSuperAdmin } from '../constants/admin'
import { PARTIES } from '../constants/parties'
import {
  ROLE_LABEL,
  SEARCH_FIELDS,
  SIGNUP_CHANNELS,
  STATUS_LABEL,
  STATUS_VALUE,
} from '../constants/adminMembers'
import {
  adminErrorMessage,
  fetchMemberStats,
  fetchMembers,
  updateMember,
  updateMemberRole,
  updateMemberStatus,
} from '../api/adminApi'

/** 검색어 입력이 멈춘 뒤 서버를 호출하기까지 기다리는 시간(ms). 타자마다 요청하지 않기 위한 것. */
const SEARCH_DEBOUNCE_MS = 300

/** 숫자를 원 단위 콤마로. */
function won(n) {
  return `${Number(n ?? 0).toLocaleString('ko-KR')}원`
}

/** 서버가 주는 '2026-09-05T09:00:00' → '2026-09-05' */
const day = (v) => (v ? v.slice(0, 10) : '—')

/**
 * 회원 상세 + 수정 모달. 가입 정보를 쭉 보여주고 이 안에서만 수정한다.
 * 맨 아래 관리자 임명/해제는 슈퍼 어드민에게만 보인다.
 * status/role 은 서버 enum 값을 그대로 들고 있고 표시할 때만 한글로 바꾼다.
 * @param {{ member: object, canManageRole: boolean, saving: boolean, error: string,
 *           onSave: (draft: object) => void, onClose: () => void }} props
 */
function MemberDetailModal({ member, canManageRole, saving, error, onSave, onClose }) {
  const [draft, setDraft] = useState(member)

  const set = (patch) => setDraft((prev) => ({ ...prev, ...patch }))

  return (
    <Modal title={`회원 상세 · ${member.loginId}`} onClose={onClose}>
      <div className="mm-detail">
        <label className="mm-detail__row">
          <span>아이디</span>
          <input type="text" value={draft.loginId} readOnly />
        </label>
        <label className="mm-detail__row">
          <span>권한</span>
          <input type="text" value={ROLE_LABEL[draft.role] ?? draft.role} readOnly />
        </label>
        <label className="mm-detail__row">
          <span>닉네임</span>
          <input
            type="text"
            value={draft.nickname ?? ''}
            onChange={(e) => set({ nickname: e.target.value })}
          />
        </label>
        <label className="mm-detail__row">
          <span>이메일</span>
          <input
            type="text"
            value={draft.email ?? ''}
            onChange={(e) => set({ email: e.target.value })}
          />
        </label>
        <label className="mm-detail__row mm-detail__row--check">
          <input
            type="checkbox"
            checked={Boolean(draft.emailVerified)}
            onChange={(e) => set({ emailVerified: e.target.checked })}
          />
          <span>이메일 인증됨</span>
        </label>
        <label className="mm-detail__row">
          <span>지지정당</span>
          <select
            value={draft.supportedParty ?? ''}
            onChange={(e) => set({ supportedParty: e.target.value })}
          >
            {PARTIES.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
        </label>
        <label className="mm-detail__row">
          <span>가입경로</span>
          <select
            value={draft.signupChannel ?? ''}
            onChange={(e) => set({ signupChannel: e.target.value })}
          >
            {SIGNUP_CHANNELS.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </label>
        <label className="mm-detail__row">
          <span>우편번호</span>
          <input
            type="text"
            value={draft.zipcode ?? ''}
            onChange={(e) => set({ zipcode: e.target.value })}
          />
        </label>
        <label className="mm-detail__row">
          <span>기본주소</span>
          <input
            type="text"
            value={draft.addressBase ?? ''}
            onChange={(e) => set({ addressBase: e.target.value })}
          />
        </label>
        <label className="mm-detail__row">
          <span>상세주소</span>
          <input
            type="text"
            value={draft.addressDetail ?? ''}
            onChange={(e) => set({ addressDetail: e.target.value })}
          />
        </label>
        <label className="mm-detail__row">
          <span>가입일</span>
          <input type="text" value={day(draft.joinedAt)} readOnly />
        </label>
        <label className="mm-detail__row">
          <span>최근접속</span>
          <input type="text" value={day(draft.lastAccessAt)} readOnly />
        </label>
        <label className="mm-detail__row mm-detail__row--check">
          <input
            type="checkbox"
            checked={Boolean(draft.isPlus)}
            onChange={(e) => set({ isPlus: e.target.checked })}
          />
          <span>Plus 회원</span>
        </label>
        <label className="mm-detail__row mm-detail__row--check">
          <input
            type="checkbox"
            checked={Boolean(draft.agreeMarketing)}
            onChange={(e) => set({ agreeMarketing: e.target.checked })}
          />
          <span>마케팅 수신 동의</span>
        </label>

        <div className="mm-detail__section">
          <span className="mm-detail__section-label">계정 상태</span>
          <span className="mm-detail__section-value">{STATUS_LABEL[draft.status]}</span>
          {draft.role === 'SUPER_ADMIN' ? (
            <span className="mm-detail__section-value">슈퍼 관리자 · 정지 불가</span>
          ) : draft.status === 'SUSPENDED' ? (
            <button
              type="button"
              className="btn btn--primary"
              onClick={() => set({ status: 'ACTIVE' })}
            >
              정지 해제
            </button>
          ) : (
            <button
              type="button"
              className="btn btn--ghost"
              onClick={() => set({ status: 'SUSPENDED' })}
            >
              정지
            </button>
          )}
        </div>

        {canManageRole && (
          <div className="mm-detail__section">
            <span className="mm-detail__section-label">관리자 권한</span>
            {draft.role === 'SUPER_ADMIN' ? (
              <span className="mm-detail__section-value">슈퍼 관리자 · 변경 불가</span>
            ) : draft.role === 'ADMIN' ? (
              <button
                type="button"
                className="btn btn--ghost"
                onClick={() => set({ role: 'USER' })}
              >
                관리자 해제
              </button>
            ) : (
              <button
                type="button"
                className="btn btn--primary"
                onClick={() => set({ role: 'ADMIN' })}
              >
                관리자 임명
              </button>
            )}
          </div>
        )}
      </div>

      {error && <p className="mm__note">{error}</p>}

      <div className="modal__actions">
        <button type="button" className="btn btn--ghost" onClick={onClose} disabled={saving}>
          취소
        </button>
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => onSave(draft)}
          disabled={saving}
        >
          {saving ? '저장 중...' : '저장'}
        </button>
      </div>
    </Modal>
  )
}

/**
 * 관리자 - 회원관리 탭.
 * 목록·검색·통계는 서버(/api/admin/members)에서 받아오고, 수정은 상세 모달에서만 한다.
 * 검색은 서버가 수행하므로 입력이 멈춘 뒤에만 호출한다.
 */
function MemberManage() {
  const currentUser = useAuthStore((state) => state.user)
  const canManageRole = isSuperAdmin(currentUser)

  const [members, setMembers] = useState([])
  const [total, setTotal] = useState(0)
  const [stats, setStats] = useState(null)
  const [listErr, setListErr] = useState('')
  const [loading, setLoading] = useState(true)

  const [searchField, setSearchField] = useState(SEARCH_FIELDS[0].key)
  const [searchTerm, setSearchTerm] = useState('')

  const [detailId, setDetailId] = useState(null)
  const [saving, setSaving] = useState(false)
  const [saveErr, setSaveErr] = useState('')

  const load = useCallback(async (field, term) => {
    setLoading(true)
    try {
      // 상태 검색만 서버가 enum 이름을 받는다. '정지' 처럼 한글로 쳐도 통하도록 바꿔준다.
      const q = field === 'status' ? (STATUS_VALUE[term.trim()] ?? term.trim()) : term.trim()
      const [page, nextStats] = await Promise.all([
        fetchMembers({ field, q }),
        fetchMemberStats(),
      ])
      setMembers(page.content ?? [])
      setTotal(page.totalElements ?? 0)
      setStats(nextStats)
      setListErr('')
    } catch (error) {
      setMembers([])
      setTotal(0)
      setListErr(adminErrorMessage(error, '회원 목록을 불러오지 못했습니다.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => load(searchField, searchTerm), SEARCH_DEBOUNCE_MS)
    return () => clearTimeout(timer)
  }, [load, searchField, searchTerm])

  const detailMember = members.find((m) => m.id === detailId) ?? null

  /** @param {object} draft 상세 모달에서 넘어온 회원 전체 값 */
  async function saveMember(draft) {
    if (!detailMember) {
      return
    }
    setSaving(true)
    setSaveErr('')
    try {
      await updateMember(draft.id, {
        nickname: draft.nickname,
        email: draft.email,
        emailVerified: draft.emailVerified,
        supportedParty: draft.supportedParty,
        signupChannel: draft.signupChannel,
        zipcode: draft.zipcode,
        addressBase: draft.addressBase,
        addressDetail: draft.addressDetail,
        isPlus: draft.isPlus,
        agreeMarketing: draft.agreeMarketing,
      })
      // 상태·권한은 전용 엔드포인트라 바뀐 것만 따로 보낸다.
      if (draft.status !== detailMember.status) {
        await updateMemberStatus(draft.id, draft.status)
      }
      if (draft.role !== detailMember.role) {
        await updateMemberRole(draft.id, draft.role)
      }
      setDetailId(null)
      await load(searchField, searchTerm)
    } catch (error) {
      setSaveErr(adminErrorMessage(error, '저장하지 못했습니다.'))
    } finally {
      setSaving(false)
    }
  }

  const cards = [
    {
      label: '활성 회원',
      value: `${stats?.activeCount ?? 0}명`,
      sub: `전체 ${total}명`,
    },
    {
      label: '장기 미접속 (1개월)',
      value: `${stats?.inactiveCount ?? 0}명`,
      sub: `${stats?.inactivePct ?? 0}%`,
    },
    { label: 'Plus 회원', value: `${stats?.plusCount ?? 0}명`, sub: `${stats?.plusPct ?? 0}%` },
    { label: '금주 매출', value: won(stats?.weeklyRevenue), sub: 'Plus 구독 기준' },
  ]

  return (
    <section className="mm">
      <div className="mm__cards">
        {cards.map((c) => (
          <article key={c.label} className="mm-card">
            <span className="mm-card__label">{c.label}</span>
            <strong className="mm-card__value">{c.value}</strong>
            <span className="mm-card__sub">{c.sub}</span>
          </article>
        ))}
      </div>

      <div className="mm__search">
        <select
          className="mm__search-field"
          value={searchField}
          onChange={(e) => setSearchField(e.target.value)}
          aria-label="검색 분류"
        >
          {SEARCH_FIELDS.map((f) => (
            <option key={f.key} value={f.key}>
              {f.label}
            </option>
          ))}
        </select>
        <input
          className="mm__search-input"
          type="search"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder={searchField === 'status' ? '정상 / 정지 / 탈퇴' : '검색어 입력'}
          aria-label="검색어"
        />
        <span className="mm__count">{members.length}명</span>
      </div>

      <div className="mm__list">
        <div className="mm-list__inner">
          <div className="mm-list__head">
            <span>아이디 / 가입일</span>
            <span>닉네임</span>
            <span>이메일</span>
            <span>지지정당</span>
            <span>가입경로</span>
            <span>상태</span>
            <span>구분</span>
            <span />
          </div>

          {listErr ? (
            <p className="mm__empty">{listErr}</p>
          ) : loading ? (
            <p className="mm__empty">불러오는 중...</p>
          ) : members.length === 0 ? (
            <p className="mm__empty">조건에 맞는 회원이 없습니다.</p>
          ) : (
            members.map((m) => (
              <article key={m.id} className="mm-row">
                <div className="mm-row__id">
                  <strong>{m.loginId}</strong>
                  <span className="mm-row__meta">{day(m.joinedAt)}</span>
                </div>
                <span className="mm-cell">{m.nickname}</span>
                <span className="mm-cell">{m.email}</span>
                <span className="mm-cell">{m.supportedParty ?? '—'}</span>
                <span className="mm-cell">{m.signupChannel ?? '—'}</span>
                <span className="mm-cell">{STATUS_LABEL[m.status] ?? m.status}</span>
                <span className="mm-row__badges">
                  {m.isPlus && <span className="mm-badge">Plus</span>}
                  {m.role !== 'USER' && (
                    <span className="mm-badge mm-badge--role">{ROLE_LABEL[m.role] ?? m.role}</span>
                  )}
                </span>
                <button
                  type="button"
                  className="mm-row__detail"
                  onClick={() => {
                    setSaveErr('')
                    setDetailId(m.id)
                  }}
                >
                  상세
                </button>
              </article>
            ))
          )}
        </div>
      </div>

      {detailMember && (
        <MemberDetailModal
          key={detailMember.id}
          member={detailMember}
          canManageRole={canManageRole}
          saving={saving}
          error={saveErr}
          onSave={saveMember}
          onClose={() => setDetailId(null)}
        />
      )}
    </section>
  )
}

export default MemberManage
