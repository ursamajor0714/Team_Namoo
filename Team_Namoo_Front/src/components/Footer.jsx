import { useLocation } from 'react-router-dom'

/** 사이트 최하단 푸터. 자리를 적게 차지하도록 작은 글씨/좁은 여백으로 둔다. */
function Footer() {
  const { pathname } = useLocation()

  // 관리자 화면에서는 푸터를 숨긴다.
  if (pathname.startsWith('/admin')) {
    return null
  }

  return (
    <footer className="footer">
      <p className="footer__line">© 2026 Team_Namoo. All rights reserved.</p>
      <p className="footer__line">
        <a
          className="footer__link"
          href="https://github.com/ursamajor0714/Team_Namoo"
          target="_blank"
          rel="noreferrer"
        >
          github.com/ursamajor0714/Team_Namoo
        </a>
      </p>
      <p className="footer__line">ursamajor0714 · qlcsps2</p>
    </footer>
  )
}

export default Footer
