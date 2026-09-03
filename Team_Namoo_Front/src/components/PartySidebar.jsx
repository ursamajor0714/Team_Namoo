import { NavLink } from 'react-router-dom'

const BOARD_IDS = [1, 2, 3, 4, 5]

function PartySidebar({ name }) {
  const encodedName = encodeURIComponent(name)
  const linkClass = ({ isActive }) =>
    `party-sidebar__link${isActive ? ' party-sidebar__link--active' : ''}`

  return (
    <nav className="party-sidebar" aria-label={`${name} 메뉴`}>
      <NavLink to={`/party/${encodedName}`} end className={linkClass}>
        뉴스
      </NavLink>
      {BOARD_IDS.map((id) => (
        <NavLink
          key={id}
          to={`/party/${encodedName}/board/${id}`}
          className={linkClass}
        >
          게시판{id}
        </NavLink>
      ))}
    </nav>
  )
}

export default PartySidebar
