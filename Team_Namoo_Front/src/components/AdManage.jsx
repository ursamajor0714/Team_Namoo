import { useRef, useState } from 'react'
import { useAdStore, adScheduleLabel, nowLocalIso, AD_PAGES } from '../store/adStore'

/** 업로드 이미지 권장 상한(약 1.5MB). localStorage 용량 때문에 큰 파일은 막는다. */
const MAX_IMAGE_BYTES = 1_500_000

const SIDES = [
  { key: 'left', label: '좌측 광고' },
  { key: 'right', label: '우측 광고' },
]

const EMPTY_FORM = { image: null, linkUrl: '', startAt: '', endAt: '' }

const pageLabel = (key) => AD_PAGES.find((p) => p.key === key)?.label ?? key

/** '2026-09-05T09:00' → '2026-09-05 09:00' */
const fmt = (v) => (v ? v.replace('T', ' ') : '—')

/**
 * 한 쪽(page, side) 광고 등록 폼. [추가] 를 누르면 store 에 쌓이고 폼은 비워진다.
 * @param {{ page: string, side: 'left'|'right', label: string }} props
 */
function AdSideForm({ page, side, label }) {
  const addAd = useAdStore((s) => s.addAd)
  const fileRef = useRef(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [err, setErr] = useState('')

  const set = (patch) => setForm((prev) => ({ ...prev, ...patch }))

  function onFile(event) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) {
      return
    }
    if (file.size > MAX_IMAGE_BYTES) {
      setErr('이미지가 너무 큽니다 (1.5MB 이하 권장).')
      return
    }
    setErr('')
    const reader = new FileReader()
    reader.onload = () => set({ image: reader.result })
    reader.onerror = () => setErr('이미지를 읽지 못했습니다.')
    reader.readAsDataURL(file)
  }

  function submit() {
    if (form.startAt && form.endAt && form.startAt > form.endAt) {
      setErr('종료 일시가 시작보다 빠릅니다.')
      return
    }
    setErr('')
    addAd({ page, side, ...form })
    setForm(EMPTY_FORM)
  }

  return (
    <div className="adm-half">
      <h3 className="adm-half__title">{label}</h3>

      <div className="adm-half__preview">
        {form.image ? (
          <img src={form.image} alt="광고 미리보기" />
        ) : (
          <span>기본 이미지</span>
        )}
      </div>

      <div className="adm-half__btns">
        <input type="file" accept="image/*" ref={fileRef} hidden onChange={onFile} />
        <button
          type="button"
          className="btn btn--ghost"
          onClick={() => fileRef.current?.click()}
        >
          이미지 업로드
        </button>
        <button
          type="button"
          className="btn btn--ghost"
          onClick={() => set({ image: null })}
          disabled={!form.image}
        >
          기본이미지로
        </button>
      </div>

      <label className="adm-field">
        <span>노출 시작 (날짜·시간)</span>
        <input
          type="datetime-local"
          value={form.startAt}
          onChange={(e) => set({ startAt: e.target.value })}
        />
      </label>
      <label className="adm-field">
        <span>노출 종료 (날짜·시간)</span>
        <input
          type="datetime-local"
          value={form.endAt}
          onChange={(e) => set({ endAt: e.target.value })}
        />
      </label>
      <label className="adm-field">
        <span>링크 URL</span>
        <input
          type="url"
          placeholder="https://..."
          value={form.linkUrl}
          onChange={(e) => set({ linkUrl: e.target.value })}
        />
      </label>

      {err && <p className="adm-half__err">{err}</p>}

      <button type="button" className="btn btn--primary adm-half__add" onClick={submit}>
        추가
      </button>
    </div>
  )
}

/**
 * 관리자 - 광고관리 탭.
 * 페이지(메인 / 정당별)를 고르고, 화면을 좌/우로 갈라 각 슬롯 광고를 등록한다.
 * 추가하면 아래 "추가 이력"에 쌓이고, 노출 페이지의 AdRail 이 날짜·시간에 맞춰 읽어간다.
 * 저장 위치는 브라우저 localStorage 라 같은 브라우저에서만 반영된다.
 */
function AdManage() {
  const ads = useAdStore((s) => s.ads)
  const removeAd = useAdStore((s) => s.removeAd)
  const [page, setPage] = useState('main')
  const now = nowLocalIso()

  const history = ads
    .filter((a) => a.page === page)
    .sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))

  return (
    <section className="adm">
      <div className="adm__pages">
        {AD_PAGES.map((p) => (
          <button
            key={p.key}
            type="button"
            className={p.key === page ? 'adm__page adm__page--active' : 'adm__page'}
            onClick={() => setPage(p.key)}
          >
            {p.label}
          </button>
        ))}
      </div>

      <p className="adm__note">
        광고는 이 브라우저(localStorage)에만 저장됩니다. 이미지 1.5MB 이하 권장.
        시작·종료 일시를 이어서 여러 건 등록하면 시각에 맞춰 다음 광고가 노출됩니다.
      </p>

      <div className="adm__split">
        {SIDES.map((s) => (
          <AdSideForm key={s.key} page={page} side={s.key} label={s.label} />
        ))}
      </div>

      <h2 className="adm-history__title">추가 이력 · {pageLabel(page)}</h2>
      {history.length === 0 ? (
        <p className="adm-history__empty">추가한 광고가 없습니다.</p>
      ) : (
        <ul className="adm-history">
          {history.map((ad) => (
            <li key={ad.id} className="adm-history__row">
              <span className="adm-history__side">
                {ad.side === 'left' ? '좌측' : '우측'}
              </span>
              <span
                className={`adm-badge adm-badge--${
                  adScheduleLabel(ad, now) === '노출 중' ? 'on' : 'off'
                }`}
              >
                {adScheduleLabel(ad, now)}
              </span>
              <span className="adm-history__range">
                {fmt(ad.startAt)} ~ {fmt(ad.endAt)}
              </span>
              <span className="adm-history__meta">
                {ad.image ? '이미지 O' : '기본이미지'} · 등록 {fmt(ad.createdAt)}
              </span>
              <button
                type="button"
                className="btn btn--ghost adm-history__del"
                onClick={() => removeAd(ad.id)}
              >
                삭제
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default AdManage
