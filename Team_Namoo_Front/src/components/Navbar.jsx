import { Link, useNavigate } from 'react-router-dom'
import { PARTIES } from '../constants/parties'

function Navbar({ user, onLogout }) {
  const navigate = useNavigate()

  return (
    <header className="navbar">
      <Link to="/" className="navbar__brand">
        정치
      </Link>

      <nav className="navbar__parties" aria-label="국회 의석수 상위 정당">
        {PARTIES.map((name) => (
          <Link
            key={name}
            to={`/party/${encodeURIComponent(name)}`}
            className="navbar__party"
          >
            {name}
          </Link>
        ))}
      </nav>

      <div className="navbar__auth">
        {user ? (
          <>
            <span className="navbar__greeting">
              안녕하세요 {user.nickname}님 환영합니다
            </span>
            <button type="button" className="navbar__link" onClick={onLogout}>
              로그아웃
            </button>
          </>
        ) : (
          <>
            <button
              type="button"
              className="navbar__btn"
              onClick={() => navigate('/login')}
            >
              로그인
            </button>
            <button
              type="button"
              className="navbar__btn navbar__btn--primary"
              onClick={() => navigate('/signup')}
            >
              회원가입
            </button>
          </>
        )}
      </div>
    </header>
  )
}

export default Navbar
