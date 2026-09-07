import { useEffect, useRef, useState } from 'react'
import { useAdStore, AD_PAGES } from '../store/adStore'
import {
  ALLOWED_IMAGE_TYPES,
  MAX_IMAGE_BYTES,
  adErrorMessage,
  uploadAdImage,
} from '../api/adApi'

const SIDES = [
  { key: 'LEFT', label: '좌측 광고' },
  { key: 'RIGHT', label: '우측 광고' },
]

const EMPTY_FORM = { file: null, previewUrl: '', linkUrl: '', startAt: '', endAt: '' }

const pageLabel = (key) => AD_PAGES.find((p) => p.key === key)?.label ?? key

/** 서버가 주는 '2026-09-05T09:00:00' → '2026-09-05 09:00' */
const fmt = (v) => (v ? v.slice(0, 16).replace('T', ' ') : '—')

/**
 * 한 쪽(page, side) 광고 등록 폼.
 * [추가] 를 누르면 (1) 서버에서 presigned URL 을 받아 (2) 브라우저가 S3 로 이미지를 직접 올리고
 * (3) 그 공개 URL 로 광고를 등록한다. 이미지를 안 고르면 기본 이미지 광고로 등록된다.
 * @param {{ page: string, side: 'LEFT'|'RIGHT', label: string }} props
 */
function AdSideForm({ page, side, label }) {
  const addAd = useAdStore((s) => s.addAd)
  const fileRef = useRef(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [err, setErr] = useState('')
  const [busy, setBusy] = useState(false)

  const set = (patch) => setForm((prev) => ({ ...prev, ...patch }))

  // 미리보기용 objectURL 은 브라우저가 자동으로 해제하지 않으므로 교체·언마운트 시 직접 해제한다.
  useEffect(() => {
    if (!form.previewUrl) {
      return undefined
    }
    return () => URL.revokeObjectURL(form.previewUrl)
  }, [form.previewUrl])

  function clearImage() {
    set({ file: null, previewUrl: '' })
  }

  function onFile(event) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) {
      return
    }
    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      setErr('이미지는 PNG, JPG, WEBP 만 올릴 수 있습니다.')
      return
    }
    if (file.size > MAX_IMAGE_BYTES) {
      setErr('이미지가 너무 큽니다 (2MB 이하).')
      return
    }
    setErr('')
    set({ file, previewUrl: URL.createObjectURL(file) })
  }

  async function submit() {
    if (busy) {
      return
    }
    // 서버는 종료가 시작보다 '뒤'일 것을 요구한다(같으면 400).
    if (form.startAt && form.endAt && form.startAt >= form.endAt) {
      setErr('종료 일시는 시작 일시보다 뒤여야 합니다.')
      return
    }
    setErr('')
    setBusy(true)
    try {
      const imageUrl = form.file ? await uploadAdImage(form.file) : null
      await addAd({
        page,
        side,
        imageUrl,
        linkUrl: form.linkUrl || null,
        startAt: form.startAt || null,
        endAt: form.endAt || null,
      })
      setForm(EMPTY_FORM)
    } catch (error) {
      setErr(adErrorMessage(error, error?.message ?? '광고 등록에 실패했습니다.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="adm-half">
      <h3 className="adm-half__title">{label}</h3>

      <div className="adm-half__preview">
        {form.previewUrl ? (
          <img src={form.previewUrl} alt="광고 미리보기" />
        ) : (
          <span>기본 이미지</span>
        )}
      </div>

      <div className="adm-half__btns">
        <input
          type="file"
          accept={ALLOWED_IMAGE_TYPES.join(',')}
          ref={fileRef}
          hidden
          onChange={onFile}
        />
        <button
          type="button"
          className="btn btn--ghost"
          onClick={() => fileRef.current?.click()}
          disabled={busy}
        >
          이미지 업로드
        </button>
        <button
          type="button"
          className="btn btn--ghost"
          onClick={clearImage}
          disabled={busy || !form.previewUrl}
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

      <button
        type="button"
        className="btn btn--primary adm-half__add"
        onClick={submit}
        disabled={busy}
      >
        {busy ? '등록 중...' : '추가'}
      </button>
    </div>
  )
}

/**
 * 관리자 - 광고관리 탭.
 * 페이지(메인 / 정당별)를 고르고, 화면을 좌/우로 갈라 각 슬롯 광고를 등록한다.
 * 등록한 광고는 서버에 저장되므로 모든 기기·모든 방문자에게 같이 보인다.
 */
function AdManage() {
  const ads = useAdStore((s) => s.ads)
  const loading = useAdStore((s) => s.loading)
  const loadAds = useAdStore((s) => s.loadAds)
  const removeAd = useAdStore((s) => s.removeAd)
  const [page, setPage] = useState('main')
  const [listErr, setListErr] = useState('')

  useEffect(() => {
    loadAds(page).catch((error) =>
      setListErr(adErrorMessage(error, '광고 목록을 불러오지 못했습니다.')),
    )
  }, [page, loadAds])

  async function onRemove(id) {
    try {
      await removeAd(id)
      setListErr('')
    } catch (error) {
      setListErr(adErrorMessage(error, '광고를 삭제하지 못했습니다.'))
    }
  }

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
        광고는 서버에 저장되어 모든 방문자에게 보입니다. 이미지는 PNG·JPG·WEBP, 2MB 이하.
        시작·종료 일시를 이어서 여러 건 등록하면 시각에 맞춰 다음 광고가 노출됩니다.
      </p>

      <div className="adm__split">
        {SIDES.map((s) => (
          <AdSideForm key={s.key} page={page} side={s.key} label={s.label} />
        ))}
      </div>

      <h2 className="adm-history__title">추가 이력 · {pageLabel(page)}</h2>
      {listErr && <p className="adm-half__err">{listErr}</p>}
      {loading ? (
        <p className="adm-history__empty">불러오는 중...</p>
      ) : ads.length === 0 ? (
        <p className="adm-history__empty">추가한 광고가 없습니다.</p>
      ) : (
        <ul className="adm-history">
          {ads.map((ad) => (
            <li key={ad.id} className="adm-history__row">
              <span className="adm-history__side">
                {ad.side === 'LEFT' ? '좌측' : '우측'}
              </span>
              <span
                className={`adm-badge adm-badge--${ad.status === '노출 중' ? 'on' : 'off'}`}
              >
                {ad.status}
              </span>
              <span className="adm-history__range">
                {fmt(ad.startAt)} ~ {fmt(ad.endAt)}
              </span>
              <span className="adm-history__meta">
                {ad.imageUrl ? '이미지 O' : '기본이미지'} · 등록 {fmt(ad.createdAt)}
              </span>
              <button
                type="button"
                className="btn btn--ghost adm-history__del"
                onClick={() => onRemove(ad.id)}
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
