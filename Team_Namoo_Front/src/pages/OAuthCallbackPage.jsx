import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { oauthCallback, oauthSignup } from '../api/authApi'
import { useAuthStore } from '../store/authStore'
import { PARTIES } from '../constants/parties'
import { SIGNUP_CHANNELS } from '../constants/adminMembers'

const PROVIDER_LABEL = { google: '구글', naver: '네이버' }

/** 백엔드가 주는 한글 메시지를 꺼낸다. */
function errorMessage(error, fallback) {
  const body = error?.response?.data
  return typeof body === 'string' && body ? body : fallback
}

/**
 * SNS 인증 후 돌아오는 곳(/oauth/:provider/callback).
 * 이미 연결된 계정이면 바로 로그인되고, 처음이면 닉네임·지지정당을 받아 가입을 마친다.
 */
function OAuthCallbackPage() {
  const { provider } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const refresh = useAuthStore((state) => state.refresh)

  const code = params.get('code')
  const state = params.get('state')
  const denied = params.get('error')
  // 인증을 거부했거나 주소에 code/state 가 없으면 서버에 물어볼 것도 없다.
  const invalid = Boolean(denied) || !code || !state

  const [phase, setPhase] = useState(invalid ? 'error' : 'loading') // loading | signup | error
  const [message, setMessage] = useState(
    invalid ? 'SNS 로그인이 취소되었거나 정보가 올바르지 않습니다.' : '',
  )
  const [email, setEmail] = useState('')
  const [nickname, setNickname] = useState('')
  const [supportedParty, setSupportedParty] = useState('')
  const [signupChannel, setSignupChannel] = useState(SIGNUP_CHANNELS[0])
  const [agreeMarketing, setAgreeMarketing] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (invalid) {
      return undefined
    }
    let cancelled = false
    oauthCallback(provider, code, state)
      .then(async (result) => {
        if (cancelled) {
          return
        }
        if (result.status === 'LOGIN') {
          await refresh()
          navigate('/', { replace: true })
          return
        }
        setEmail(result.email ?? '')
        setNickname(result.suggestedNickname ?? '')
        setPhase('signup')
      })
      .catch((error) => {
        if (cancelled) {
          return
        }
        setMessage(errorMessage(error, 'SNS 로그인에 실패했습니다. 다시 시도해주세요.'))
        setPhase('error')
      })
    return () => {
      cancelled = true
    }
  }, [provider, code, state, invalid, navigate, refresh])

  async function completeSignup(event) {
    event.preventDefault()
    if (submitting) {
      return
    }
    setSubmitting(true)
    setMessage('')
    try {
      await oauthSignup({
        nickname: nickname.trim(),
        supportedParty,
        signupChannel,
        agreeMarketing,
      })
      await refresh()
      navigate('/', { replace: true })
    } catch (error) {
      setMessage(errorMessage(error, '가입을 마치지 못했습니다.'))
      setSubmitting(false)
    }
  }

  if (phase === 'loading') {
    return (
      <main className="login-page">
        <p className="login-box__title">
          {PROVIDER_LABEL[provider] ?? provider} 로그인 처리 중...
        </p>
      </main>
    )
  }

  if (phase === 'error') {
    return (
      <main className="login-page">
        <div className="login-box">
          <h1 className="login-box__title">SNS 로그인</h1>
          <p className="login-box__error" role="alert">
            {message}
          </p>
          <button
            type="button"
            className="login-box__submit"
            onClick={() => navigate('/login', { replace: true })}
          >
            로그인으로 돌아가기
          </button>
        </div>
      </main>
    )
  }

  return (
    <main className="login-page">
      <form className="login-box" onSubmit={completeSignup}>
        <h1 className="login-box__title">
          {PROVIDER_LABEL[provider] ?? provider} 계정으로 가입
        </h1>
        <p className="login-box__label">{email}</p>

        <label className="login-box__field">
          <span className="login-box__label">닉네임</span>
          <input
            type="text"
            className="login-box__input"
            value={nickname}
            onChange={(event) => setNickname(event.target.value)}
            minLength={2}
            maxLength={12}
            required
          />
        </label>

        <label className="login-box__field">
          <span className="login-box__label">지지 정당</span>
          <select
            className="login-box__input"
            value={supportedParty}
            onChange={(event) => setSupportedParty(event.target.value)}
            required
          >
            <option value="" disabled>
              선택하세요
            </option>
            {PARTIES.map((party) => (
              <option key={party} value={party}>
                {party}
              </option>
            ))}
          </select>
        </label>

        <label className="login-box__field">
          <span className="login-box__label">가입 경로</span>
          <select
            className="login-box__input"
            value={signupChannel}
            onChange={(event) => setSignupChannel(event.target.value)}
          >
            {SIGNUP_CHANNELS.map((channel) => (
              <option key={channel} value={channel}>
                {channel}
              </option>
            ))}
          </select>
        </label>

        <label className="login-box__keep">
          <input
            type="checkbox"
            checked={agreeMarketing}
            onChange={(event) => setAgreeMarketing(event.target.checked)}
          />
          <span>마케팅 정보 수신 동의 (선택)</span>
        </label>

        {message && (
          <p className="login-box__error" role="alert">
            {message}
          </p>
        )}

        <button type="submit" className="login-box__submit" disabled={submitting}>
          {submitting ? '가입 중...' : '가입 완료'}
        </button>
      </form>
    </main>
  )
}

export default OAuthCallbackPage
