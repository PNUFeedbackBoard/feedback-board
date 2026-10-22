import LogoutButton from '../../components/common/LogoutButton.jsx'
import { useEffect, useState } from 'react'
import { Navigate, NavLink, Outlet, useMatch } from 'react-router-dom'
import { ExternalLink } from 'lucide-react'
import { getMe, getProjects } from '../../api/endpoints.js'
import iniLogo from '../../assets/ini-logo-optimized.png'
import ProjectLogo from '../../components/common/ProjectLogo.jsx'
import { ROLE, labelOf } from '../../constants/enums.js'
import ProjectDisc from './ProjectDisc.jsx'
import '../../styles/tokens.css'
import './admin-layout.css'

/**
 * 관리용 공통 레이아웃. 기획 6-1 의 화면 구조를 그대로 따른다.
 *
 *   공통 헤더 (제목 · 계정)
 *   상단 탭   (선택된 프로젝트 안의 메뉴 — 접수 / 개발 보드 / 목록)
 *   본문      (<Outlet />)
 *   하단      (프로젝트 전환)
 *
 * 권한의 기준은 getMe() 응답이다. 하위 화면은 Outlet context 로 같은 값을 받아
 * 서버에서 거부될 조작을 애초에 버튼이나 드래그 대상으로 보여 주지 않는다.
 */
export default function AdminLayout() {
	// 상단 탭은 "선택된 프로젝트 안의 메뉴"라 프로젝트가 정해진 경로에서만 나온다.
	// 홈(/admin)과 계정 관리(/admin/accounts)는 프로젝트 바깥이므로 탭이 없다.
	const projectMatch = useMatch('/admin/:projectCode/:tab')
	const projectCode = projectMatch?.params.projectCode ?? null

	const [auth, setAuth] = useState({ phase: 'loading', me: null, message: '' })
	const [projects, setProjects] = useState([])
	const [projectsMessage, setProjectsMessage] = useState('')

	useEffect(() => {
		let cancelled = false
		getMe()
			.then((found) => {
				if (cancelled) return
				setAuth({ phase: 'ready', me: found, message: '' })
				if (found.status !== 'ACTIVE' || !['DEVELOPER', 'VIEWER'].includes(found.role)) return
				getProjects()
					.then((items) => !cancelled && setProjects(items))
					.catch((error) => !cancelled && setProjectsMessage(error.message))
			})
			.catch((error) => {
				if (cancelled) return
				setAuth({
					phase: error.status === 401 ? 'unauthenticated' : 'error',
					me: null,
					message: error.message,
				})
			})
		return () => {
			cancelled = true
		}
	}, [])

	if (auth.phase === 'loading') return <RouteStatus>계정 권한을 확인하는 중…</RouteStatus>
	if (auth.phase === 'unauthenticated') return <Navigate to="/" replace />
	if (auth.phase === 'error') return <RouteStatus alert>{auth.message}</RouteStatus>

	const me = auth.me
	if (me.status === 'PENDING') return <Navigate to="/admin/pending" replace />
	if (me.status !== 'ACTIVE') return <Navigate to="/" replace />
	if (me.role === 'USER') return <Navigate to="/home" replace />
	if (!['DEVELOPER', 'VIEWER'].includes(me.role)) return <Navigate to="/" replace />

	const currentProject = projects.find((project) => project.code === projectCode)
	const currentSiteUrl = safeHttpUrl(currentProject?.siteUrl)

	return (
		<div className="admin-shell">
			<header className="admin-header">
				<NavLink to="/admin" className="admin-brand" aria-label="INI 전체 현황">
					<img className="admin-brand__mark" src={iniLogo} alt="" aria-hidden="true" />
					<span className="admin-brand__type">
						<strong>INI</strong>
						<span>ISSUE &amp; IDEA</span>
					</span>
				</NavLink>

				<span className="admin-header__title">통합 피드백 보드</span>
				<span className="admin-header__account">
					{me ? (
						<>
							<span className="admin-header__presence" aria-hidden="true" />
							<span>{me.name}</span>
              <LogoutButton />
							<span className="admin-header__role">{labelOf(ROLE, me.role)}</span>
							{me.role === 'DEVELOPER' && <NavLink to="/admin/accounts">계정 관리</NavLink>}
						</>
					) : (
						'로그인이 필요합니다'
					)}
				</span>
			</header>

			{projectCode && (
				<nav className="admin-tabs" aria-label="프로젝트 메뉴">
					<div className="admin-tabs__inner">
						<span className="admin-tabs__project">
							<span className="admin-tabs__label">PROJECT</span>
							<ProjectLogo project={currentProject} projectCode={projectCode} className="admin-tabs__project-logo" />
							<strong>{currentProject?.name ?? projectCode}</strong>
							{currentSiteUrl && (
								<a
									className="admin-tabs__site"
									href={currentSiteUrl}
									target="_blank"
									rel="noreferrer"
								>
									서비스 열기 <ExternalLink size={13} aria-hidden="true" />
								</a>
							)}
						</span>
						<div className="admin-tabs__links">
							<NavLink to={`/admin/${projectCode}/intake`} className="admin-tabs__tab">
								접수
							</NavLink>
							<NavLink to={`/admin/${projectCode}/board`} className="admin-tabs__tab">
								개발 보드
							</NavLink>
							<NavLink to={`/admin/${projectCode}/answers`} className="admin-tabs__tab">
								목록
							</NavLink>
						</div>
					</div>
				</nav>
			)}

			<main className="admin-body">
				{projectsMessage && <p className="app-alert" role="alert">{projectsMessage}</p>}
				<Outlet context={{ me }} />
			</main>

			<ProjectDisc projects={projects} projectCode={projectCode} />

		</div>
	)
}

function RouteStatus({ alert = false, children }) {
	return (
		<main className="app-page">
			<div className="app-page__inner">
				<p className={alert ? 'app-alert' : 'app-card'} role={alert ? 'alert' : 'status'}>{children}</p>
			</div>
		</main>
	)
}

function safeHttpUrl(value) {
	if (!value) return null
	try {
		const url = new URL(value)
		return url.protocol === 'http:' || url.protocol === 'https:' ? url.href : null
	} catch {
		return null
	}
}
