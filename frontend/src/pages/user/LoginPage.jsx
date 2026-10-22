import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getAuthConfig, getMe, getProjects, startGoogleLogin } from '../../api/endpoints.js'
import '../page-shell.css'
import LoginButtons from '../../components/common/LoginButtons.jsx'
import ProjectLogo from '../../components/common/ProjectLogo.jsx'
import iniLogo from '../../assets/ini-logo-optimized.png'
import './site-logo.css'

const NO_SOURCE_CONTEXT = { ready: true, project: null, invalid: false }

export default function LoginPage() {
	const navigate = useNavigate()
	const [params] = useSearchParams()
	const requestedSite = params.get('site')
	const [authConfig, setAuthConfig] = useState(null)
	const [sourceLookup, setSourceLookup] = useState({ requestedSite: null, project: null, invalid: false })
	const [message, setMessage] = useState('')
	const [guestNotice, setGuestNotice] = useState(false)
	const guestDialogRef = useRef(null)
	const sourceContext = !requestedSite
		? NO_SOURCE_CONTEXT
		: sourceLookup.requestedSite === requestedSite
			? { ready: true, project: sourceLookup.project, invalid: sourceLookup.invalid }
			: { ready: false, project: null, invalid: false }
	const sourceSite = sourceContext.project?.code ?? null

	const goToStart = useCallback((me) => {
		if (me.status === 'PENDING') navigate('/admin/pending')
		else if (me.role === 'DEVELOPER' || me.role === 'VIEWER') navigate('/admin')
		else navigate(sourceSite ? `/write?site=${encodeURIComponent(sourceSite)}` : '/home')
	}, [navigate, sourceSite])

	useEffect(() => {
		if (!sourceContext.ready) return
		getMe()
			.then(goToStart)
			.catch(() => {})
	}, [goToStart, sourceContext.ready])

	useEffect(() => {
		if (!requestedSite) return

		let cancelled = false
		getProjects()
			.then((projects) => {
				if (cancelled) return
				const project = projects.find((candidate) => candidate.code === requestedSite) ?? null
				setSourceLookup({ requestedSite, project, invalid: !project })
			})
			.catch((error) => {
				if (cancelled) return
				setSourceLookup({ requestedSite, project: null, invalid: false })
				setMessage(error.message)
			})
		return () => {
			cancelled = true
		}
	}, [requestedSite])

	useEffect(() => {
		getAuthConfig().then(setAuthConfig).catch((error) => setMessage(error.message))
	}, [])

	useEffect(() => {
		if (!guestNotice) return undefined

		const previouslyFocused = document.activeElement
		const previousOverflow = document.body.style.overflow
		document.body.style.overflow = 'hidden'
		const focusFrame = window.requestAnimationFrame(() => {
			guestDialogRef.current?.querySelector('button:not(:disabled)')?.focus()
		})
		const closeOnEscape = (event) => {
			if (event.key === 'Escape') setGuestNotice(false)
		}
		document.addEventListener('keydown', closeOnEscape)

		return () => {
			window.cancelAnimationFrame(focusFrame)
			document.removeEventListener('keydown', closeOnEscape)
			document.body.style.overflow = previousOverflow
			if (previouslyFocused instanceof HTMLElement) previouslyFocused.focus()
		}
	}, [guestNotice])

	const keepFocusInGuestDialog = (event) => {
		if (event.key !== 'Tab') return
		const buttons = guestDialogRef.current?.querySelectorAll('button:not(:disabled)')
		if (!buttons?.length) return

		const firstButton = buttons[0]
		const lastButton = buttons[buttons.length - 1]
		if (event.shiftKey && (document.activeElement === firstButton || document.activeElement === guestDialogRef.current)) {
			event.preventDefault()
			lastButton.focus()
		} else if (!event.shiftKey && document.activeElement === lastButton) {
			event.preventDefault()
			firstButton.focus()
		}
	}

	const authErrors = {
		disabled: '사용이 중지된 계정입니다. 관리자에게 문의해 주세요.',
		account_conflict: '이미 다른 구글 계정과 연결된 이메일입니다.',
		invalid_google_account: '확인된 이메일을 가진 구글 계정으로 로그인해 주세요.',
		school_email_required: '부산대학교 이메일(@pusan.ac.kr) 계정으로만 로그인할 수 있습니다. Google 로그인을 다시 눌러 다른 계정을 선택해 주세요.',
		google_failed: '구글 로그인이 취소되었거나 실패했습니다. 다시 시도해 주세요.',
	}
	const authError = authErrors[params.get('authError')]
	const googleReady = Boolean(authConfig?.googleEnabled)

	return (
		<main className="app-page login-page">
			<div className="app-page__inner app-stack login-page__inner">
				<header className="app-card login-panel">
					<img className="login-brand__mark" src={iniLogo} alt="INI 로고" />
					<p className="app-page__eyebrow">ISSUE &amp; IDEA</p>
					<h1>통합 피드백 보드</h1>
					<p className="app-page__muted">
						부산대학교 AI융합교육원 서비스에서 발견한 문제와 아이디어를 한곳에 남겨 주세요.
					</p>
					{sourceContext.project && (
						<div className="login-source-context" role="status">
							<ProjectLogo project={sourceContext.project} className="login-source-context__logo" />
							<div className="login-source-context__copy">
								<span>피드백 대상</span>
								<strong>{sourceContext.project.name}</strong>
								<small>로그인 후 해당 서비스가 자동으로 선택됩니다.</small>
							</div>
						</div>
					)}
					<LoginButtons
						onGoogleLogin={() => startGoogleLogin('user', sourceSite)}
						onAdminGoogleLogin={() => startGoogleLogin('admin')}
						onGuestLogin={() => setGuestNotice(true)}
						disabled={!sourceContext.ready}
						googleDisabled={!googleReady}
					/>
				</header>

				{(message || authError) && <p className="app-alert" role="alert">{message || authError}</p>}
				{sourceContext.invalid && (
					<p className="app-notice" role="status">
						연결된 서비스 정보를 확인하지 못했습니다. 계속 진행하면 작성 화면에서 대상 서비스를 직접 선택할 수 있습니다.
					</p>
				)}
				{authConfig && !authConfig.googleEnabled && (
					<p className="app-page__muted">
						구글 로그인 설정이 아직 완료되지 않았습니다.
						{import.meta.env.DEV && authConfig.previewEnabled && ' 개발용 접근 권한 미리보기에서 화면을 확인할 수 있습니다.'}
					</p>
				)}

				{guestNotice && (
					<div
						className="guest-dialog__backdrop"
						onClick={(event) => {
							if (event.target === event.currentTarget) setGuestNotice(false)
						}}
					>
						<section
							ref={guestDialogRef}
							className="guest-dialog"
							role="dialog"
							aria-modal="true"
							aria-labelledby="guest-title"
							aria-describedby="guest-description"
							tabIndex={-1}
							onKeyDown={keepFocusInGuestDialog}
						>
							<p className="app-page__eyebrow">GUEST FEEDBACK</p>
							<h2 id="guest-title">비회원으로 작성할까요?</h2>
							<p id="guest-description" className="guest-dialog__description">
								비회원으로 남긴 문의는 제출 후 진행 상태와 개발자 답변을 다시 확인할 수 없습니다.
							</p>
							<div className="app-actions">
								<button className="app-button" type="button" onClick={() => navigate(sourceSite ? `/write?site=${encodeURIComponent(sourceSite)}` : '/write')}>
									비회원으로 계속
								</button>
								<button className="app-button app-button--secondary" type="button" onClick={() => setGuestNotice(false)}>
									취소
								</button>
							</div>
						</section>
					</div>
				)}
			</div>
		</main>
	)
}
