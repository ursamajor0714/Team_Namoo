import { Link } from 'react-router-dom'

const TOP_PARTIES = ['더불어민주당', '국민의힘', '조국혁신당', '진보당', '개혁신당']

function Navbar({ user }) {
  return (
    <header className="navbar">
      <nav className="navbar__parties" aria-label="국회 의석수 상위 정당">
        {TOP_PARTIES.map((name) => (
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
            <button type="button" className="navbar__link" disabled>
              내 정보
            </button>
          </>
        ) : (
          <>
            <button type="button" className="navbar__btn">
              로그인
            </button>
            <button type="button" className="navbar__btn navbar__btn--primary">
              회원가입
            </button>
          </>
        )}
      </div>
    </header>
  )
}

export default Navbar
