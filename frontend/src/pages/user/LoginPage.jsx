import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import heroImage from '../../assets/hero.png'
import iniLogo from '../../assets/ini-logo.png'
import { devLogin, getMe } from '../../api/endpoints.js'
import './login.css'

/**
 * 경로: /. 로그인 화면. 기획 5-1, 5-3.
 *
 * 구글 OAuth 는 7단계에 붙는다(기획 10장, 13-2). 그 전까지 "구글로 로그인" 버튼은
 * 데모 로그인(POST /api/dev/login, account=user)을 호출한다 — 화면 흐름(로그인 → 홈)은
 * 실제와 같고, 7단계에서는 이 호출 한 줄만 진짜 구글 로그인으로 바뀐다.
 *
 * ?site= 로 들어오면(기획 5-2) 피드백 작성 화면까지 그 값을 들고 간다. 로그인 화면 자체는
 * 사이트를 표시하지 않는다 — 어차피 다음 화면(작성 또는 홈)에서 결정되기 때문이다.
 *
 * 이미 로그인된 채로 들어오면 서버가 내려준 역할(GET /api/me)대로 첫 화면으로 보낸다.
 */
export default function LoginPage() {
	const [searchParams] = useSearchParams()
	const navigate = useNavigate()
	const [showGuestNotice, setShowGuestNotice] = useState(false)
	const [busy, setBusy] = useState(false)
	const [error, setError] = useState('')

	const site = searchParams.get('site')
	const writePath = site ? `/write?site=${encodeURIComponent(site)}` : '/write'

	// 역할 판단은 서버가 준 값(me.role, me.status)만 본다.
	const goToStart = useCallback(
		(me) => {
			if (me.status === 'PENDING') navigate('/admin/pending')
			else if (me.role === 'DEVELOPER' || me.role === 'VIEWER') navigate('/admin')
			else navigate('/home')
		},
		[navigate],
	)

	useEffect(() => {
		getMe()
			.then(goToStart)
			.catch(() => {})
	}, [goToStart])

	async function loginWithGoogle() {
		// 데모 로그인은 dev 프로필에만 있다. 운영 빌드에서는 7단계 전까지 막아 둔다.
		if (!import.meta.env.DEV) {
			setError('운영 로그인은 Google OAuth 설정이 완료된 뒤 제공됩니다.')
			return
		}
		setBusy(true)
		setError('')
		try {
			// TODO(B, 7단계): 구글 OAuth 로 교체한다. 화면은 이 함수 안쪽만 바뀌면 된다.
			goToStart(await devLogin('user'))
		} catch (problem) {
			setBusy(false)
			setError(problem.message)
		}
	}

	return (
		<div className="login">
			<section className="login__panel">
				<div className="login__brand">
					<img className="login__mark" src={iniLogo} alt="" aria-hidden="true" />
					<span className="login__brand-type">
						<strong>INI</strong>
						<span>ISSUE &amp; IDEA</span>
					</span>
				</div>

				<h1 className="login__title">
					부산대 AI융합교육원
					<br />5개 시스템의 피드백을 한 곳에서
				</h1>
				<p className="login__copy">
					코드플레이스 · AIPMS · AICMS · AI역량지원시스템 · pickle(서버관리) 이용 중 겪은
					오류나 제안을 남겨 주세요. 로그인하면 처리 상태와 답변을 다시 확인할 수 있습니다.
				</p>

				{error && <p className="login__error">{error}</p>}

				<div className="login__actions">
					<button
						type="button"
						className="login__button login__button--primary"
						disabled={busy}
						onClick={loginWithGoogle}
					>
						{busy ? '로그인하는 중…' : '구글로 로그인'}
					</button>
					<button
						type="button"
						className="login__button"
						disabled={busy}
						onClick={() => setShowGuestNotice(true)}
					>
						비회원으로 계속
					</button>
				</div>
			</section>

			<div className="login__hero" aria-hidden="true">
				<img src={heroImage} alt="" />
			</div>

			{showGuestNotice && (
				<div
					className="login__veil"
					onPointerDown={(event) => event.target === event.currentTarget && setShowGuestNotice(false)}
				>
					<div className="login__dialog" role="dialog" aria-modal="true" aria-labelledby="guest-notice-title">
						<h2 id="guest-notice-title" className="login__dialog-title">
							비회원으로 계속하시겠어요?
						</h2>
						<p className="login__dialog-copy">
							비회원으로 등록한 문의는 답변을 받을 수 없고, 등록한 내용과 진행 상태를
							나중에 다시 확인할 수도 없습니다.
						</p>
						<div className="login__dialog-actions">
							<button
								type="button"
								className="login__button"
								disabled={busy}
								onClick={loginWithGoogle}
							>
								로그인
							</button>
							<button
								type="button"
								className="login__button login__button--primary"
								onClick={() => navigate(writePath)}
							>
								계속
							</button>
						</div>
					</div>
				</div>
			)}
		</div>
	)
}
