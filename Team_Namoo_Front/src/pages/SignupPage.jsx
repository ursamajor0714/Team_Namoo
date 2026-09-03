import { useRef, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { checkLoginId, checkEmail, sendEmailCode, verifyEmailCode } from '../api/authApi'
import { PARTIES } from '../constants/parties'
import { TERMS_OF_SERVICE, PRIVACY_NOTICE } from '../constants/agreements'
import { containsBannedWord } from '../constants/bannedWords'
import Modal from '../components/Modal'

const SIGNUP_CHANNELS = ['인스타그램', '페이스북', '커뮤니티', '검색']
const LOGIN_ID_RE = /^[A-Za-z0-9]{4,20}$/
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
// 대문자·소문자·숫자·특수문자를 모두 포함하고 8자 이상
const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/

const EMPTY_FIELD_ERRORS = { loginId: '', nickname: '', email: '', party: '' }

/**
 * 비밀번호 규칙별 충족 여부. 입력 중 실시간 체크리스트로 보여준다.
 * @param {string} password
 * @returns {{ label: string, ok: boolean }[]}
 */
function passwordChecklist(password) {
  return [
    { label: '8자 이상', ok: password.length >= 8 },
    { label: '대문자', ok: /[A-Z]/.test(password) },
    { label: '소문자', ok: /[a-z]/.test(password) },
    { label: '숫자', ok: /\d/.test(password) },
    { label: '특수문자', ok: /[^A-Za-z0-9]/.test(password) },
  ]
}

/**
 * 회원가입 페이지. 단계(step)를 나눠 진행한다.
 *   'age'     : 만 14세 이상 확인
 *   'under14' : 가입 불가 안내
 *   'terms'   : 약관 및 정책 동의 (필수 동의해야 다음 단계)
 *   'form'    : 가입 정보 입력 폼
 * 검증 방식: 빈 칸은 브라우저 기본 검증(required) 말풍선이 처리하고, 형식 오류는 submit 시
 *           validate() 가 잡아 해당 필드 바로 아래 메시지를 띄우고 그 칸으로 포커스를 옮긴다.
 * 제출 성공 시: 백엔드가 받는 4필드(loginId/password/email/nickname)만 전송 -> 자동 로그인 -> 홈.
 * 주소/지지정당/가입경로/약관동의는 아직 백엔드에 저장할 곳이 없어 UI로만 수집한다(전송 보류).
 */
function SignupPage() {
  const user = useAuthStore((state) => state.user)
  const signup = useAuthStore((state) => state.signup)
  const login = useAuthStore((state) => state.login)
  const navigate = useNavigate()

  const [step, setStep] = useState('age')

  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [nickname, setNickname] = useState('')
  const [email, setEmail] = useState('')
  const [zipcode, setZipcode] = useState('')
  const [addressBase, setAddressBase] = useState('')
  const [addressDetail, setAddressDetail] = useState('')
  const [supportedParty, setSupportedParty] = useState('')
  const [signupChannel, setSignupChannel] = useState('')
  const [agree, setAgree] = useState({ terms: false, privacy: false, marketing: false })

  const [pendingParty, setPendingParty] = useState(null)
  // 필드별 형식 오류 메시지 (빈 칸은 브라우저 기본 말풍선이 담당)
  const [fieldErrors, setFieldErrors] = useState(EMPTY_FIELD_ERRORS)
  // 서버/제출 단계 오류 (아이디·이메일 중복 등)
  const [submitError, setSubmitError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // 아이디 중복확인 결과: null(미확인) | 'checking' | 'available' | 'taken'
  const [loginIdStatus, setLoginIdStatus] = useState(null)
  // 이메일 인증: 코드 발송 여부 / 입력 코드 / 인증 완료 / 진행중 / 안내 메시지
  const [emailCodeSent, setEmailCodeSent] = useState(false)
  const [emailCode, setEmailCode] = useState('')
  const [emailVerified, setEmailVerified] = useState(false)
  const [emailAuthPending, setEmailAuthPending] = useState(false)
  const [emailAuthMsg, setEmailAuthMsg] = useState('')

  // 검증 실패 시 포커스를 옮길 대상들
  const loginIdRef = useRef(null)
  const passwordRef = useRef(null)
  const passwordConfirmRef = useRef(null)
  const nicknameRef = useRef(null)
  const emailRef = useRef(null)
  const partyRef = useRef(null)

  /** @param {'terms'|'privacy'|'marketing'} key */
  function toggleAgree(key) {
    setAgree((prev) => ({ ...prev, [key]: !prev[key] }))
  }

  /** @param {'loginId'|'nickname'|'email'|'party'} field 해당 필드를 수정하면 그 오류를 지운다. */
  function clearFieldError(field) {
    setFieldErrors((prev) => (prev[field] ? { ...prev, [field]: '' } : prev))
  }

  /** 아이디 중복확인 버튼. 형식이 맞을 때만 서버에 질의한다. */
  async function handleCheckLoginId() {
    if (!LOGIN_ID_RE.test(loginId)) {
      setFieldErrors((prev) => ({ ...prev, loginId: '아이디는 영문·숫자 4~20자로 입력해주세요.' }))
      return
    }
    setLoginIdStatus('checking')
    try {
      const available = await checkLoginId(loginId)
      setLoginIdStatus(available ? 'available' : 'taken')
    } catch {
      setLoginIdStatus(null)
      setFieldErrors((prev) => ({ ...prev, loginId: '중복확인에 실패했습니다. 잠시 후 다시 시도해주세요.' }))
    }
  }

  /** 이메일 "인증" 버튼: 중복 확인 후 인증 코드를 발송한다. */
  async function handleSendEmailCode() {
    if (!EMAIL_RE.test(email)) {
      setFieldErrors((prev) => ({ ...prev, email: '이메일 형식이 올바르지 않습니다.' }))
      return
    }
    setEmailAuthPending(true)
    setEmailAuthMsg('')
    try {
      const available = await checkEmail(email)
      if (!available) {
        setFieldErrors((prev) => ({ ...prev, email: '이미 사용 중인 이메일입니다.' }))
        return
      }
      await sendEmailCode(email)
      setEmailCodeSent(true)
      setEmailAuthMsg('인증 코드를 메일로 보냈습니다. 5분 이내에 입력해주세요.')
    } catch (err) {
      const body = err.response?.data
      setEmailAuthMsg(typeof body === 'string' && body ? body : '인증 코드 발송에 실패했습니다.')
    } finally {
      setEmailAuthPending(false)
    }
  }

  /** 인증 코드 "확인" 버튼. 성공하면 이메일 필드를 잠근다. */
  async function handleVerifyEmailCode() {
    const code = emailCode.trim()
    if (!code) {
      return
    }
    setEmailAuthPending(true)
    try {
      await verifyEmailCode(email, code)
      setEmailVerified(true)
      setEmailAuthMsg('이메일 인증이 완료되었습니다.')
    } catch (err) {
      const body = err.response?.data
      setEmailAuthMsg(typeof body === 'string' && body ? body : '인증에 실패했습니다.')
    } finally {
      setEmailAuthPending(false)
    }
  }

  /**
   * @returns {{ field: string, ref: React.RefObject<HTMLElement>, message: string } | null}
   *          검증 실패 시 대상 필드/포커스 ref/메시지, 통과 시 null.
   */
  function validate() {
    if (!LOGIN_ID_RE.test(loginId)) {
      return { field: 'loginId', ref: loginIdRef, message: '아이디는 영문·숫자 4~20자로 입력해주세요.' }
    }
    if (!PASSWORD_RE.test(password)) {
      return { field: 'password', ref: passwordRef, message: '비밀번호 조건을 확인해주세요.' }
    }
    if (password !== passwordConfirm) {
      return { field: 'passwordConfirm', ref: passwordConfirmRef, message: '비밀번호가 일치하지 않습니다.' }
    }
    const trimmedNickname = nickname.trim()
    if (trimmedNickname.length < 2 || trimmedNickname.length > 12) {
      return { field: 'nickname', ref: nicknameRef, message: '닉네임은 2~12자로 입력해주세요.' }
    }
    if (containsBannedWord(trimmedNickname)) {
      return { field: 'nickname', ref: nicknameRef, message: '비속어·혐오 표현은 닉네임에 사용할 수 없습니다.' }
    }
    if (!EMAIL_RE.test(email)) {
      return { field: 'email', ref: emailRef, message: '이메일 형식이 올바르지 않습니다.' }
    }
    if (!supportedParty) {
      return { field: 'party', ref: partyRef, message: '지지 정당을 선택해주세요.' }
    }
    return null
  }

  /** @param {React.FormEvent<HTMLFormElement>} event */
  async function handleSubmit(event) {
    event.preventDefault()
    if (submitting) {
      return
    }
    const failure = validate()
    if (failure) {
      // loginId/nickname/email/party 는 필드 아래 메시지로, password 류는 각자 실시간 UI로 안내한다.
      if (failure.field in EMPTY_FIELD_ERRORS) {
        setFieldErrors((prev) => ({ ...prev, [failure.field]: failure.message }))
      }
      const target = failure.ref.current
      if (target) {
        // sticky 네비바에 가리지 않도록 화면 중앙으로 스크롤한 뒤 포커스(포커스 자체 스크롤은 막는다).
        target.scrollIntoView({ block: 'center', behavior: 'smooth' })
        target.focus({ preventScroll: true })
      }
      return
    }
    // 백엔드 signup 은 이메일 인증 완료를 요구한다(안 하면 400).
    if (!emailVerified) {
      setSubmitError('이메일 인증을 완료해주세요.')
      const target = emailRef.current
      if (target) {
        target.scrollIntoView({ block: 'center', behavior: 'smooth' })
        target.focus({ preventScroll: true })
      }
      return
    }
    setSubmitError('')
    setSubmitting(true)
    try {
      // 백엔드 SignupRequest 는 아직 4필드만 받는다. 나머지는 스펙 확정 후 추가 전송.
      await signup({ loginId, password, email, nickname })
      await login({ loginId, password })
      navigate('/', { replace: true })
    } catch (err) {
      // 아이디/이메일 중복 등 실패 시 백엔드가 400 + 한글 메시지 문자열을 준다.
      const body = err.response?.data
      setSubmitError(
        typeof body === 'string' && body
          ? body
          : '회원가입에 실패했습니다. 잠시 후 다시 시도해주세요.',
      )
      setSubmitting(false)
    }
  }

  // 이미 로그인한 상태면 회원가입 페이지를 보여줄 이유가 없다.
  if (user) {
    return <Navigate to="/" replace />
  }

  // 1단계: 만 14세 이상 확인
  if (step === 'age') {
    return (
      <main className="signup-page">
        <div className="age-gate">
          <h1 className="age-gate__title">만 14세 이상 확인</h1>
          <p className="age-gate__desc">회원가입을 위해 나이를 확인해주세요.</p>
          <div className="age-gate__actions">
            <button type="button" className="btn btn--primary" onClick={() => setStep('terms')}>
              만 14세 이상입니다
            </button>
            <button type="button" className="btn btn--ghost" onClick={() => setStep('under14')}>
              만 14세 미만입니다
            </button>
          </div>
        </div>
      </main>
    )
  }

  // 만 14세 미만: 가입 불가
  if (step === 'under14') {
    return (
      <main className="signup-page">
        <div className="age-gate">
          <h1 className="age-gate__title">회원가입 불가</h1>
          <p className="age-gate__desc">만 14세 미만은 회원가입이 불가능합니다.</p>
          <div className="age-gate__actions">
            <button type="button" className="btn btn--ghost" onClick={() => setStep('age')}>
              이전으로
            </button>
            <button type="button" className="btn btn--primary" onClick={() => navigate('/')}>
              홈으로
            </button>
          </div>
        </div>
      </main>
    )
  }

  // 2단계: 약관 및 정책 동의 (필수 동의해야 폼으로)
  if (step === 'terms') {
    const requiredAgreed = agree.terms && agree.privacy
    return (
      <main className="signup-page">
        <div className="signup-box">
          <h1 className="signup-box__title">약관 및 정책 동의</h1>

          <div className="signup-box__field">
            <span className="signup-box__label">이용약관</span>
            <pre className="terms-box">{TERMS_OF_SERVICE}</pre>
            <label className="signup-box__check">
              <input type="checkbox" checked={agree.terms} onChange={() => toggleAgree('terms')} />
              <span>[필수] 이용약관에 동의합니다</span>
            </label>
          </div>

          <div className="signup-box__field">
            <span className="signup-box__label">개인정보 수집·이용</span>
            <pre className="terms-box">{PRIVACY_NOTICE}</pre>
            <label className="signup-box__check">
              <input
                type="checkbox"
                checked={agree.privacy}
                onChange={() => toggleAgree('privacy')}
              />
              <span>[필수] 개인정보 수집·이용에 동의합니다</span>
            </label>
          </div>

          <label className="signup-box__check">
            <input
              type="checkbox"
              checked={agree.marketing}
              onChange={() => toggleAgree('marketing')}
            />
            <span>[선택] 마케팅 정보 수신에 동의합니다</span>
          </label>

          <div className="modal__actions">
            <button type="button" className="btn btn--ghost" onClick={() => setStep('age')}>
              이전으로
            </button>
            <button
              type="button"
              className="btn btn--primary"
              disabled={!requiredAgreed}
              onClick={() => setStep('form')}
            >
              동의하고 계속
            </button>
          </div>
        </div>
      </main>
    )
  }

  const pwChecks = passwordChecklist(password)
  const pwConfirmMatches = passwordConfirm.length > 0 && password === passwordConfirm

  return (
    <main className="signup-page">
      <form className="signup-box" onSubmit={handleSubmit}>
        <h1 className="signup-box__title">회원가입</h1>

        <label className="signup-box__field">
          <span className="signup-box__label">아이디</span>
          <div className="signup-box__inline">
            <input
              ref={loginIdRef}
              type="text"
              className="signup-box__input"
              value={loginId}
              onChange={(event) => {
                setLoginId(event.target.value)
                clearFieldError('loginId')
                setLoginIdStatus(null)
              }}
              autoComplete="username"
              required
            />
            <button
              type="button"
              className="btn btn--ghost"
              onClick={handleCheckLoginId}
              disabled={loginIdStatus === 'checking'}
            >
              {loginIdStatus === 'checking' ? '확인 중...' : '중복확인'}
            </button>
          </div>
          {fieldErrors.loginId ? (
            <span className="signup-box__hint signup-box__hint--error">{fieldErrors.loginId}</span>
          ) : loginIdStatus === 'available' ? (
            <span className="signup-box__hint signup-box__hint--ok">사용할 수 있는 아이디입니다.</span>
          ) : loginIdStatus === 'taken' ? (
            <span className="signup-box__hint signup-box__hint--error">이미 사용 중인 아이디입니다.</span>
          ) : (
            <span className="signup-box__hint">영문·숫자 4~20자</span>
          )}
        </label>

        <label className="signup-box__field">
          <span className="signup-box__label">비밀번호</span>
          <input
            ref={passwordRef}
            type="password"
            className="signup-box__input"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
            required
          />
          {password ? (
            <ul className="pw-checklist">
              {pwChecks.map((check) => (
                <li
                  key={check.label}
                  className={
                    check.ok
                      ? 'pw-checklist__item pw-checklist__item--ok'
                      : 'pw-checklist__item'
                  }
                >
                  {check.ok ? '✓' : '✕'} {check.label}
                </li>
              ))}
            </ul>
          ) : (
            <span className="signup-box__hint">
              대·소문자, 숫자, 특수문자 모두 포함 8자 이상
            </span>
          )}
        </label>

        <label className="signup-box__field">
          <span className="signup-box__label">비밀번호 확인</span>
          <input
            ref={passwordConfirmRef}
            type="password"
            className="signup-box__input"
            value={passwordConfirm}
            onChange={(event) => setPasswordConfirm(event.target.value)}
            autoComplete="new-password"
            required
          />
          {passwordConfirm.length > 0 && (
            <span
              className={
                pwConfirmMatches
                  ? 'signup-box__hint signup-box__hint--ok'
                  : 'signup-box__hint signup-box__hint--error'
              }
            >
              {pwConfirmMatches ? '비밀번호가 일치합니다' : '비밀번호가 일치하지 않습니다'}
            </span>
          )}
        </label>

        <label className="signup-box__field">
          <span className="signup-box__label">닉네임</span>
          <input
            ref={nicknameRef}
            type="text"
            className="signup-box__input"
            value={nickname}
            onChange={(event) => {
              setNickname(event.target.value)
              clearFieldError('nickname')
            }}
            required
          />
          {fieldErrors.nickname ? (
            <span className="signup-box__hint signup-box__hint--error">{fieldErrors.nickname}</span>
          ) : (
            <span className="signup-box__hint">2~12자 · 비속어·혐오 표현 사용 불가</span>
          )}
        </label>

        <label className="signup-box__field">
          <span className="signup-box__label">이메일</span>
          <div className="signup-box__inline">
            <input
              ref={emailRef}
              type="email"
              className="signup-box__input"
              value={email}
              onChange={(event) => {
                setEmail(event.target.value)
                clearFieldError('email')
                // 이메일이 바뀌면 이전 인증 상태는 무효
                if (emailCodeSent || emailVerified) {
                  setEmailCodeSent(false)
                  setEmailVerified(false)
                  setEmailCode('')
                  setEmailAuthMsg('')
                }
              }}
              autoComplete="email"
              disabled={emailVerified}
              required
            />
            <button
              type="button"
              className="btn btn--ghost"
              onClick={handleSendEmailCode}
              disabled={emailAuthPending || emailVerified}
            >
              {emailVerified ? '인증완료' : emailCodeSent ? '재전송' : '인증'}
            </button>
          </div>
          {emailCodeSent && !emailVerified && (
            <div className="signup-box__inline">
              <input
                type="text"
                className="signup-box__input"
                value={emailCode}
                onChange={(event) => setEmailCode(event.target.value)}
                inputMode="numeric"
                autoComplete="one-time-code"
                aria-label="이메일 인증 코드"
                placeholder="인증 코드 6자리"
              />
              <button
                type="button"
                className="btn btn--ghost"
                onClick={handleVerifyEmailCode}
                disabled={emailAuthPending || !emailCode.trim()}
              >
                확인
              </button>
            </div>
          )}
          {fieldErrors.email ? (
            <span className="signup-box__hint signup-box__hint--error">{fieldErrors.email}</span>
          ) : emailAuthMsg ? (
            <span
              className={
                emailVerified
                  ? 'signup-box__hint signup-box__hint--ok'
                  : 'signup-box__hint'
              }
            >
              {emailAuthMsg}
            </span>
          ) : (
            <span className="signup-box__hint">가입하려면 이메일 인증이 필요합니다</span>
          )}
        </label>

        <div className="signup-box__field">
          <span className="signup-box__label">주소</span>
          <input
            type="text"
            className="signup-box__input"
            value={zipcode}
            onChange={(event) => setZipcode(event.target.value)}
            aria-label="우편번호"
            placeholder="우편번호"
            inputMode="numeric"
            autoComplete="postal-code"
          />
          <input
            type="text"
            className="signup-box__input"
            value={addressBase}
            onChange={(event) => setAddressBase(event.target.value)}
            aria-label="기본주소"
            placeholder="기본주소 (도로명 또는 지번)"
            autoComplete="address-line1"
          />
          <input
            type="text"
            className="signup-box__input"
            value={addressDetail}
            onChange={(event) => setAddressDetail(event.target.value)}
            aria-label="상세주소"
            placeholder="상세주소 (동/호수 등)"
            autoComplete="address-line2"
          />
          <span className="signup-box__hint">
            우편번호 검색은 추후 구현 · 현재는 직접 입력
          </span>
        </div>

        <div className="signup-box__field">
          <span className="signup-box__label">지지 정당</span>
          <div className="party-choice">
            {PARTIES.map((party, index) => (
              <button
                key={party}
                ref={index === 0 ? partyRef : undefined}
                type="button"
                className={
                  supportedParty === party
                    ? 'party-choice__option party-choice__option--active'
                    : 'party-choice__option'
                }
                onClick={() => setPendingParty(party)}
              >
                {party}
              </button>
            ))}
            <button type="button" className="party-choice__option" disabled>
              기타정당 (준비중)
            </button>
          </div>
          {fieldErrors.party ? (
            <span className="signup-box__hint signup-box__hint--error">{fieldErrors.party}</span>
          ) : (
            supportedParty && (
              <span className="signup-box__hint">선택됨: {supportedParty}</span>
            )
          )}
        </div>

        <fieldset className="signup-box__field signup-box__fieldset">
          <legend className="signup-box__label">가입 경로</legend>
          <div className="signup-box__radios">
            {SIGNUP_CHANNELS.map((channel) => (
              <label key={channel} className="signup-box__radio">
                <input
                  type="radio"
                  name="signupChannel"
                  value={channel}
                  checked={signupChannel === channel}
                  onChange={(event) => setSignupChannel(event.target.value)}
                />
                <span>{channel}</span>
              </label>
            ))}
          </div>
        </fieldset>

        {submitError && (
          <p className="login-box__error" role="alert">
            {submitError}
          </p>
        )}

        <button type="submit" className="login-box__submit" disabled={submitting}>
          {submitting ? '가입 중...' : '회원가입'}
        </button>
      </form>

      {pendingParty && (
        <Modal title="정당 선택 안내" onClose={() => setPendingParty(null)}>
          <p className="modal__text">
            선택하신 <strong>{pendingParty}</strong> 외{' '}
            <strong>타 정당 게시글·댓글 작성이 제한</strong>됩니다.
            <br />
            신중하게 선택해 주세요.
            <br />
            추후 <strong>Plus 회원권</strong>으로 전 정당 글쓰기·댓글이 허용됩니다.
          </p>
          <div className="modal__actions">
            <button
              type="button"
              className="btn btn--ghost"
              onClick={() => setPendingParty(null)}
            >
              취소
            </button>
            <button
              type="button"
              className="btn btn--primary"
              onClick={() => {
                setSupportedParty(pendingParty)
                clearFieldError('party')
                setPendingParty(null)
              }}
            >
              이 정당으로 선택
            </button>
          </div>
        </Modal>
      )}
    </main>
  )
}

export default SignupPage
