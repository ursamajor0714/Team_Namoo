import { useState } from 'react'
import { Outlet, useParams } from 'react-router-dom'
import PartySidebar from '../components/PartySidebar'

function PartyLayout() {
  const { name } = useParams()
  const [sidebarOpen, setSidebarOpen] = useState(true)

  return (
    <div className="party-layout">
      <button
        type="button"
        className={`party-sidebar-toggle${sidebarOpen ? '' : ' party-sidebar-toggle--collapsed'}`}
        onClick={() => setSidebarOpen((open) => !open)}
        aria-label={sidebarOpen ? '사이드바 닫기' : '사이드바 열기'}
        aria-expanded={sidebarOpen}
      >
        <svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
          <rect
            x="1.5"
            y="2.5"
            width="13"
            height="11"
            rx="2.5"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.3"
          />
          <line x1="6" y1="2.5" x2="6" y2="13.5" stroke="currentColor" strokeWidth="1.3" />
          <path
            d="M4 2.5 H6 V13.5 H4 A2.5 2.5 0 0 1 1.5 11 V5 A2.5 2.5 0 0 1 4 2.5 Z"
            fill="currentColor"
          />
        </svg>
      </button>
      <div className={`party-sidebar-wrap${sidebarOpen ? ' party-sidebar-wrap--open' : ''}`}>
        <PartySidebar name={name} />
      </div>
      <div className="party-content">
        <Outlet />
      </div>
    </div>
  )
}

export default PartyLayout
