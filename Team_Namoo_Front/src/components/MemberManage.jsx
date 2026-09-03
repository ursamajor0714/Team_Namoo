import { useMemo, useState } from 'react'
import Modal from './Modal'
import { useAuthStore } from '../store/authStore'
import { isSuperAdmin } from '../constants/admin'
import { PARTIES } from '../constants/parties'
import {
  MOCK_MEMBERS,
  SIGNUP_CHANNELS,
  SEARCH_FIELDS,
  buildMemberStats,
} from '../constants/adminMembers'

/** 숫자를 원 단위 콤마로. */
function won(n) {
  return `${n.toLocaleString('ko-KR')}원`
}

/**
 * 회원 상세 + 수정 모달. 가입 정보를 쭉 보여주고 이 안에서만 수정한다.
 * 맨 아래 관리자 임명/해제는 슈퍼 어드민에게만 보인다.
 * @param {{ member: object, canManageRole: boolean, onSave: (patch: object) => void, onClose: () => void }} props
 */
function MemberDetailModal({ member, canManageRole, onSave, onClose }) {
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
          <input type="text" value={draft.role} readOnly />
        </label>
        <label className="mm-detail__row">
          <span>닉네임</span>
          <input
            type="text"
            value={draft.nickname}
            onChange={(e) => set({ nickname: e.target.value })}
          />
        </label>
        <label className="mm-detail__row">
          <span>이메일</span>
          <input
            type="text"
            value={draft.email}
            onChange={(e) => set({ email: e.target.value })}
          />
        </label>
        <label className="mm-detail__row mm-detail__row--check">
          <input
            type="checkbox"
            checked={draft.emailVerified}
            onChange={(e) => set({ emailVerified: e.target.checked })}
          />
          <span>이메일 인증됨</span>
        </label>
        <label className="mm-detail__row">
          <span>지지정당</span>
          <select
            value={draft.supportedParty}
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
            value={draft.signupChannel}
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
            value={draft.zipcode}
            onChange={(e) => set({ zipcode: e.target.value })}
          />
        </label>
        <label className="mm-detail__row">
          <span>기본주소</span>
          <input
            type="text"
            value={draft.addressBase}
            onChange={(e) => set({ addressBase: e.target.value })}
          />
        </label>
        <label className="mm-detail__row">
          <span>상세주소</span>
          <input
            type="text"
            value={draft.addressDetail}
            onChange={(e) => set({ addressDetail: e.target.value })}
          />
        </label>
        <label className="mm-detail__row">
          <span>가입일</span>
          <input type="text" value={draft.joinedAt} readOnly />
        </label>
        <label className="mm-detail__row">
          <span>최근접속</span>
          <input type="text" value={draft.lastAccessAt} readOnly />
        </label>
        <label className="mm-detail__row mm-detail__row--check">
          <input
            type="checkbox"
            checked={draft.isPlus}
            onChange={(e) => set({ isPlus: e.target.checked })}
          />
          <span>Plus 회원</span>
        </label>
        <label className="mm-detail__row mm-detail__row--check">
          <input
            type="checkbox"
            checked={draft.agreeMarketing}
            onChange={(e) => set({ agreeMarketing: e.target.checked })}
          />
          <span>마케팅 수신 동의</span>
        </label>

        <div className="mm-detail__section">
          <span className="mm-detail__section-label">계정 상태</span>
          <span className="mm-detail__section-value">{draft.status}</span>
          {draft.status === '정지' ? (
            <button
              type="button"
              className="btn btn--primary"
              onClick={() => set({ status: '정상' })}
            >
              정지 해제
            </button>
          ) : (
            <button
              type="button"
              className="btn btn--ghost"
              onClick={() => set({ status: '정지' })}
            >
              정지
            </button>
          )}
        </div>

        {canManageRole && (
          <div className="mm-detail__section">
            <span className="mm-detail__section-label">관리자 권한</span>
            {draft.role === '슈퍼관리자' ? (
              <span className="mm-detail__section-value">슈퍼 관리자 · 변경 불가</span>
            ) : draft.role === '관리자' ? (
              <button
                type="button"
                className="btn btn--ghost"
                onClick={() => set({ role: '일반' })}
              >
                관리자 해제
              </button>
            ) : (
              <button
                type="button"
                className="btn btn--primary"
                onClick={() => set({ role: '관리자' })}
              >
                관리자 임명
              </button>
            )}
          </div>
        )}
      </div>

      <p className="mm__note">목 데이터입니다. 저장해도 서버에는 반영되지 않습니다.</p>

      <div className="modal__actions">
        <button type="button" className="btn btn--ghost" onClick={onClose}>
          취소
        </button>
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => {
            onSave(draft)
            onClose()
          }}
        >
          저장
        </button>
      </div>
    </Modal>
  )
}

/**
 * 관리자 - 회원관리 탭.
 * 목록은 읽기 전용, 수정은 상세 모달에서만. 데이터는 목이라 저장돼도 화면 state 에만 남는다.
 */
function MemberManage() {
  const currentUser = useAuthStore((state) => state.user)
  const canManageRole = isSuperAdmin(currentUser)

  const [members, setMembers] = useState(MOCK_MEMBERS)
  const [searchField, setSearchField] = useState(SEARCH_FIELDS[0].key)
  const [searchTerm, setSearchTerm] = useState('')
  const [detailId, setDetailId] = useState(null)

  const stats = useMemo(() => buildMemberStats(members), [members])

  const filtered = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    if (!term) {
      return members
    }
    return members.filter((m) =>
      String(m[searchField] ?? '')
        .toLowerCase()
        .includes(term),
    )
  }, [members, searchField, searchTerm])

  /** @param {object} draft 상세 모달에서 넘어온 회원 전체 값 */
  function saveMember(draft) {
    setMembers((prev) => prev.map((m) => (m.id === draft.id ? { ...m, ...draft } : m)))
  }

  const cards = [
    { label: '활성 회원', value: `${stats.active}명`, sub: `전체 ${members.length}명` },
    {
      label: '장기 미접속 (1개월)',
      value: `${stats.inactive}명`,
      sub: `${stats.inactivePct}%`,
    },
    { label: 'Plus 회원', value: `${stats.plus}명`, sub: `${stats.plusPct}%` },
    { label: '금주 매출', value: won(stats.weeklyRevenue), sub: 'Plus 구독 기준' },
  ]

  const detailMember = members.find((m) => m.id === detailId) ?? null

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
          placeholder="검색어 입력"
          aria-label="검색어"
        />
        <span className="mm__count">{filtered.length}명</span>
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

          {filtered.length === 0 ? (
            <p className="mm__empty">조건에 맞는 회원이 없습니다.</p>
          ) : (
            filtered.map((m) => (
              <article key={m.id} className="mm-row">
                <div className="mm-row__id">
                  <strong>{m.loginId}</strong>
                  <span className="mm-row__meta">{m.joinedAt}</span>
                </div>
                <span className="mm-cell">{m.nickname}</span>
                <span className="mm-cell">{m.email}</span>
                <span className="mm-cell">{m.supportedParty}</span>
                <span className="mm-cell">{m.signupChannel}</span>
                <span className="mm-cell">{m.status}</span>
                <span className="mm-row__badges">
                  {m.isPlus && <span className="mm-badge">Plus</span>}
                  {m.role !== '일반' && (
                    <span className="mm-badge mm-badge--role">{m.role}</span>
                  )}
                </span>
                <button
                  type="button"
                  className="mm-row__detail"
                  onClick={() => setDetailId(m.id)}
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
          onSave={saveMember}
          onClose={() => setDetailId(null)}
        />
      )}
    </section>
  )
}

export default MemberManage
