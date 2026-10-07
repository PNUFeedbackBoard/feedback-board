import { useEffect, useState } from 'react'
import { NavLink, Outlet, useMatch } from 'react-router-dom'
import { getMe, getProjects } from '../../api/endpoints.js'
import iniLogo from '../../assets/ini-logo-optimized.png'
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

	const [me, setMe] = useState(null)
	const [projects, setProjects] = useState([])

	useEffect(() => {
		// 로그인하지 않았으면 401 이 온다. 화면을 막지 않고 헤더에만 안내를 띄운다.
		getMe()
			.then(setMe)
			.catch(() => setMe(null))
		getProjects()
			.then(setProjects)
			.catch(() => setProjects([]))
	}, [])

	const currentProject = projects.find((project) => project.code === projectCode)

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
							<span>PROJECT</span>
							<strong>{currentProject?.name ?? projectCode}</strong>
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

			<main className="admin-body"><Outlet context={{ me }} /></main>

			<ProjectDisc projects={projects} projectCode={projectCode} />

		</div>
	)
}
