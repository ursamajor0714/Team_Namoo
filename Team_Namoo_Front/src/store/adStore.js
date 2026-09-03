import { create } from 'zustand'
import { PARTIES } from '../constants/parties'

// 광고 슬롯 상태 저장소.
// 백엔드 광고 API 가 없어 브라우저 localStorage 에만 저장한다 - 관리자가 등록한 광고를
// 같은 브라우저의 메인/정당 페이지가 읽는다. (배포/다른 기기와는 공유되지 않음)

const STORAGE_KEY = 'teamnamoo_ads'

/** 광고를 붙일 페이지. main + 정당 5개. */
export const AD_PAGES = [
  { key: 'main', label: '메인' },
  ...PARTIES.map((p) => ({ key: p, label: p })),
]

/** @typedef {{ id: string, page: string, side: 'left'|'right', image: string|null, linkUrl: string, startAt: string, endAt: string, createdAt: string }} Ad */

/** 로컬 시간 기준 'YYYY-MM-DDTHH:mm' (datetime-local 입력값과 같은 포맷). */
export function nowLocalIso() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`
}

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function persist(ads) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(ads))
  } catch {
    // 용량 초과(이미지 과다) 등 - 저장 실패해도 화면 state 는 유지된다.
  }
}

let seq = 0
const newId = () => `ad_${Date.now()}_${seq++}`

export const useAdStore = create((set, get) => ({
  /** @type {Ad[]} */
  ads: load(),

  /** @param {Omit<Ad,'id'|'createdAt'>} ad */
  addAd: (ad) => {
    const ads = [
      ...get().ads,
      { ...ad, id: newId(), createdAt: nowLocalIso() },
    ]
    persist(ads)
    set({ ads })
  },

  /** @param {string} id */
  removeAd: (id) => {
    const ads = get().ads.filter((a) => a.id !== id)
    persist(ads)
    set({ ads })
  },
}))

/**
 * (page, side) 슬롯에서 지금 노출할 광고 하나. 기간이 now 를 포함하는 광고 중
 * 시작 일시가 가장 이른 것. 없으면 null(기본 이미지).
 * @param {Ad[]} ads
 * @param {string} page
 * @param {'left'|'right'} side
 * @param {string} now 'YYYY-MM-DDTHH:mm'
 * @returns {Ad|null}
 */
export function pickActiveAd(ads, page, side, now) {
  const matches = ads
    .filter((a) => a.page === page && a.side === side)
    .filter(
      (a) => (!a.startAt || a.startAt <= now) && (!a.endAt || now <= a.endAt),
    )
    .sort((a, b) => (a.startAt || '').localeCompare(b.startAt || ''))
  return matches[0] ?? null
}

/**
 * 관리 화면용 광고 상태 라벨.
 * @param {Ad} ad @param {string} now
 * @returns {'노출 중'|'예약'|'종료'|'기간 미설정'}
 */
export function adScheduleLabel(ad, now) {
  if (!ad.startAt && !ad.endAt) {
    return '기간 미설정'
  }
  if (ad.startAt && now < ad.startAt) {
    return '예약'
  }
  if (ad.endAt && now > ad.endAt) {
    return '종료'
  }
  return '노출 중'
}
