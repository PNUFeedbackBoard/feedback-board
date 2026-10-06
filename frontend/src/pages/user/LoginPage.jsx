import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getAuthConfig, getMe, startGoogleLogin } from '../../api/endpoints.js'
import '../page-shell.css'
import iniLogo from '../../assets/ini-logo.png'
import './site-logo.css'

export default function LoginPage() {
	const navigate = useNavigate()
	const [params] = useSearchParams()
	const site = params.get('site')
	const [authConfig, setAuthConfig] = useState(null)
	const [message, setMessage] = useState('')
	const [guestNotice, setGuestNotice] = useState(false)

	const goToStart = useCallback((me) => {
		if (me.status === 'PENDING') navigate('/admin/pending')
		else if (me.role === 'DEVELOPER' || me.role === 'VIEWER') navigate('/admin')
		else navigate(site ? `/write?site=${encodeURIComponent(site)}` : '/home')
	}, [navigate, site])

	useEffect(() => {
		getMe()
			.then(goToStart)
			.catch(() => {})
	}, [goToStart])

  useEffect(() => {
    getAuthConfig().then(setAuthConfig).catch((error) => setMessage(error.message))
  }, [])
  const authErrors = {
    disabled: '사용이 중지된 계정입니다. 관리자에게 문의해 주세요.',
    account_conflict: '이미 다른 구글 계정과 연결된 이메일입니다.',
    invalid_google_account: '확인된 이메일을 가진 구글 계정으로 로그인해 주세요.',
    google_failed: '구글 로그인이 취소되었거나 실패했습니다. 다시 시도해 주세요.',
  }
  const authError = authErrors[params.get('authError')]

  return (
		<main className="app-page">
			<div className="app-page__inner app-stack">
				<header className="app-card">
					<img className="login-brand__mark" src={iniLogo} alt="INI 로고" />
					<p className="app-page__eyebrow">ISSUE &amp; IDEA</p>
					<h1>통합 피드백 보드</h1>
					<p className="app-page__muted">
						부산대학교 AI융합교육원 서비스에서 발견한 문제와 아이디어를 한곳에 남겨 주세요.
					</p>
					<div className="app-actions">
						<button className="app-button" type="button" onClick={() => startGoogleLogin('user', site)} disabled={!authConfig?.googleEnabled}>
							Google로 로그인
						</button>
						<button className="app-button app-button--secondary" type="button"
              onClick={() => startGoogleLogin('admin')} disabled={!authConfig?.googleEnabled}>
              관리용 Google 로그인
            </button>
            <button className="app-button app-button--secondary" type="button" onClick={() => setGuestNotice(true)}>
							비회원으로 계속
						</button>
					</div>
				</header>

				{(message || authError) && <p className="app-alert" role="alert">{message || authError}</p>}
        {authConfig && !authConfig.googleEnabled && <p className="app-page__muted">
          구글 로그인 설정이 아직 완료되지 않았습니다.
          {import.meta.env.DEV && authConfig.previewEnabled && ' 개발용 접근 권한 미리보기에서 화면을 확인할 수 있습니다.'}
        </p>}

				{guestNotice && (
					<section className="app-card" role="dialog" aria-modal="true" aria-labelledby="guest-title">
						<h2 id="guest-title">비회원으로 작성할까요?</h2>
						<p className="app-page__muted">
							비회원 문의는 제출 후 진행 상태와 개발자 답변을 다시 확인할 수 없습니다.
						</p>
						<div className="app-actions">
							<button className="app-button" type="button" onClick={() => navigate(site ? `/write?site=${encodeURIComponent(site)}` : '/write')}>
								계속 작성
							</button>
							<button className="app-button app-button--secondary" type="button"
                  onClick={() => startGoogleLogin('user', site)} disabled={!authConfig?.googleEnabled}>
                Google로 로그인
							</button>
						</div>
					</section>
				)}
			</div>
		</main>
  )
}
