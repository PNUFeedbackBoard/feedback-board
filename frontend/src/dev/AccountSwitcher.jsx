import { useEffect, useState } from 'react'
import { ApiError } from '../api/client.js'
import { devLogin, devLogout, getMe } from '../api/endpoints.js'
import { ROLE, USER_STATUS, labelOf } from '../constants/enums.js'
import './account-switcher.css'

/**
 * 개발용 계정 전환 위젯. 기획 13-2 참고.
 *
 * - `import.meta.env.DEV` 가 참일 때만 렌더한다. `npm run build` 결과물에는 화면이 나오지 않는다.
 * - POST /api/dev/login 과 /api/dev/logout 을 호출한다. 두 엔드포인트는 백엔드 dev 프로필에서만 등록된다.
 * - 7단계에서 구글 로그인으로 전환할 때 이 폴더(src/dev)만 지우면 된다. 화면 코드는 건드리지 않는다.
 *
 * 스타일은 위치와 가독성에 필요한 최소한만 둔다. 색은 브라우저 시스템 색(Canvas 등)을 써서
 * 0-2단계에서 D가 정할 색상 팔레트와 겹치지 않게 했다.
 */
export default function AccountSwitcher() {
	const [collapsed, setCollapsed] = useState(
		() => window.localStorage.getItem(COLLAPSED_STORAGE_KEY) === 'true',
	)
	const [state, setState] = useState({
		busy: false,
		current: null,
		message: '현재 권한 확인 중…',
	})

	useEffect(() => {
		if (!import.meta.env.DEV) return undefined
		let cancelled = false

		getMe()
			.then((me) => {
				if (!cancelled) {
					setState({
						busy: false,
						current: accountOf(me),
						message: `${me.name} · ${labelOf(ROLE, me.role)}`,
					})
				}
			})
			.catch(() => {
				if (!cancelled) setState({ busy: false, current: null, message: '로그아웃 상태' })
			})

		return () => {
			cancelled = true
		}
	}, [])

	// 훅 호출 뒤에 검사한다. 순서가 바뀌면 rules-of-hooks 위반이다.
	if (!import.meta.env.DEV) return null

	async function run(account, label, action) {
		setState((previous) => ({ ...previous, busy: true, message: `${label} 권한 적용 중…` }))
		try {
			const result = await action()
			setState({
				busy: false,
				current: result ? account : null,
				message: result ? `${result.name} · ${labelOf(ROLE, result.role)}` : '로그아웃됨',
			})
			// 레이아웃과 목록이 새 세션 권한으로 API 를 다시 읽도록 즉시 갱신한다.
			window.setTimeout(() => window.location.reload(), 240)
		} catch (error) {
			const reason =
				error instanceof ApiError && error.status === 404
					? 'dev 프로필로 백엔드를 켰는지 확인하세요'
					: error.message
			setState((previous) => ({ ...previous, busy: false, message: `실패: ${reason}` }))
		}
	}

	function setSwitcherCollapsed(next) {
		setCollapsed(next)
		window.localStorage.setItem(COLLAPSED_STORAGE_KEY, String(next))
	}

	if (collapsed) {
		return (
			<aside className="dev-switcher dev-switcher--collapsed" aria-label="개발용 접근 권한 미리보기">
				<button
					type="button"
					className="dev-switcher__bubble"
					onClick={() => setSwitcherCollapsed(false)}
					aria-label="접근 권한 미리보기 열기"
					title="접근 권한 미리보기 열기"
				>
					<span aria-hidden="true">DEV</span>
				</button>
			</aside>
		)
	}

	return (
		<aside className="dev-switcher" aria-label="개발용 접근 권한 미리보기">
			<header className="dev-switcher__head">
				<div className="dev-switcher__heading">
					<span className="dev-switcher__tag">DEV TOOL</span>
					<strong>접근 권한 미리보기</strong>
				</div>
				<button
					type="button"
					className="dev-switcher__collapse"
					onClick={() => setSwitcherCollapsed(true)}
					aria-label="접근 권한 미리보기 접기"
					title="접기"
				>
					<span aria-hidden="true" />
				</button>
			</header>
			<p className="dev-switcher__help">
				계정을 고르면 화면을 자동으로 새로고침해 해당 권한을 바로 확인합니다.
			</p>
			<div className="dev-switcher__roles" role="group" aria-label="테스트 계정 선택">
				{ACCOUNTS.map(({ account, label }) => (
					<button
						key={account}
						type="button"
						className={state.current === account ? 'is-active' : ''}
						aria-pressed={state.current === account}
						disabled={state.busy}
						onClick={() => run(account, label, () => devLogin(account))}
					>
						{label}
					</button>
				))}
			</div>
			<footer className="dev-switcher__status" aria-live="polite">
				<span>
					현재 <strong>{state.message}</strong>
				</span>
				<button
					type="button"
					disabled={state.busy}
					onClick={() => run(null, '로그아웃', devLogout)}
				>
					로그아웃
				</button>
			</footer>
		</aside>
	)
}

/**
 * DevLoginRequest 의 account 값과 화면에 보일 이름. 기획 13-2 의 데모 계정 4종이다.
 * 백엔드 DevLoginController.DemoAccount 와 같은 4종을 둔다.
 * `승인 대기` 가 있어야 D 가 /admin/pending 화면을 실제로 열어 볼 수 있다.
 */
const ACCOUNTS = [
  { account: 'dev', label: '개발자' },
  { account: 'viewer', label: '열람자' },
  { account: 'user', label: '사용자' },
  { account: 'pending', label: '승인 대기' },
]

const COLLAPSED_STORAGE_KEY = 'feedback-board:dev-switcher-collapsed'

function accountOf(me) {
	if (me.status === 'PENDING') return 'pending'
	if (me.role === 'DEVELOPER') return 'dev'
	if (me.role === 'VIEWER') return 'viewer'
	if (me.role === 'USER' && me.status === 'ACTIVE') return 'user'
	return `${labelOf(ROLE, me.role)}-${labelOf(USER_STATUS, me.status)}`
}
