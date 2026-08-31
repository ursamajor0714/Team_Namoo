import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import Modal from '../components/Modal'

/**
 * 로그인 페이지.
 * 흐름: 아이디/비밀번호 입력값을 상태로 들고 있다가 submit 시 인증 스토어의 login 호출
 *      -> 성공하면 홈으로 이동, 실패하면 백엔드가 준 한글 메시지를 그대로 노출.
 * 레이아웃: 중앙 박스(아이디/비밀번호 세로 배치 + 로그인 유지 + 로그인 버튼),
 *          박스 아래 아이디찾기/비밀번호찾기, 그 아래 회원가입 유도 링크(/signup).
 * "로그인 유지"는 지금 프론트 상태로만 존재한다 - 실제 세션 유지는 백엔드 쿠키 max-age 설정이 필요.
 */
function LoginPage() {
  const user = useAuthStore((state) => state.user)
  const login = useAuthStore((state) => state.login)
  const navigate = useNavigate()
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [keepLoggedIn, setKeepLoggedIn] = useState(false)
  const [showPublicWarning, setShowPublicWarning] = useState(false)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // 이미 로그인한 상태면 로그인 페이지를 보여줄 이유가 없다.
  if (user) {
    return <Navigate to="/" replace />
  }

  /** @param {React.ChangeEvent<HTMLInputElement>} event */
  function handleKeepChange(event) {
    const { checked } = event.target
    setKeepLoggedIn(checked)
    // 체크할 때만 공용 PC 경고를 띄운다.
    if (checked) {
      setShowPublicWarning(true)
    }
  }

  /** @param {React.FormEvent<HTMLFormElement>} event */
  async function handleSubmit(event) {
    event.preventDefault()
    if (submitting) {
      return
    }
    setError('')
    setSubmitting(true)
    try {
      await login({ loginId, password })
      navigate('/', { replace: true })
    } catch (err) {
      // 백엔드는 로그인 실패 시 400 + 한글 메시지 문자열을 body로 준다(GlobalExceptionHandler).
      const message = err.response?.data
      setError(
        typeof message === 'string' && message
          ? message
          : '로그인에 실패했습니다. 잠시 후 다시 시도해주세요.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <form className="login-box" onSubmit={handleSubmit}>
        <h1 className="login-box__title">로그인</h1>

        <label className="login-box__field">
          <span className="login-box__label">아이디</span>
          <input
            type="text"
            className="login-box__input"
            value={loginId}
            onChange={(event) => setLoginId(event.target.value)}
            autoComplete="username"
            required
          />
        </label>

        <label className="login-box__field">
          <span className="login-box__label">비밀번호</span>
          <input
            type="password"
            className="login-box__input"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
            required
          />
        </label>

        <label className="login-box__keep">
          <input type="checkbox" checked={keepLoggedIn} onChange={handleKeepChange} />
          <span>로그인 유지</span>
        </label>

        {error && (
          <p className="login-box__error" role="alert">
            {error}
          </p>
        )}

        <button type="submit" className="login-box__submit" disabled={submitting}>
          {submitting ? '로그인 중...' : '로그인'}
        </button>
      </form>

      <nav className="login-links" aria-label="계정 도움말">
        <div className="login-links__find">
          <button type="button" className="login-links__link">
            아이디 찾기
          </button>
          <span className="login-links__sep" aria-hidden="true">
            |
          </span>
          <button type="button" className="login-links__link">
            비밀번호 찾기
          </button>
        </div>
        <Link to="/signup" className="login-links__signup">
          회원가입을 하고 더 많은 정보를 구독하세요!
        </Link>
      </nav>

      {showPublicWarning && (
        <Modal title="로그인 유지 안내" onClose={() => setShowPublicWarning(false)}>
          <p className="modal__text">
            공용 PC나 여러 사람이 사용하는 공개된 장소에서는{' '}
            <strong>로그인 유지</strong>를 사용하지 마세요.
            <br />
            타인이 내 계정에 접근할 수 있습니다.
          </p>
          <div className="modal__actions">
            <button
              type="button"
              className="btn btn--ghost"
              onClick={() => {
                setKeepLoggedIn(false)
                setShowPublicWarning(false)
              }}
            >
              사용 안 함
            </button>
            <button
              type="button"
              className="btn btn--primary"
              onClick={() => setShowPublicWarning(false)}
            >
              확인
            </button>
          </div>
        </Modal>
      )}
    </main>
  )
}

export default LoginPage
