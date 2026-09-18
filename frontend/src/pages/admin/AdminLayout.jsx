import { useEffect, useState } from 'react'
import { NavLink, Outlet, useMatch } from 'react-router-dom'
import { getMe, getProjects } from '../../api/endpoints.js'
import { ROLE, labelOf } from '../../constants/enums.js'
import ProjectDisc from './ProjectDisc.jsx'
import './admin-tokens.temp.css'
import './admin-layout.css'

/**
 * 관리용 공통 레이아웃. 기획 6-1 의 화면 구조를 그대로 따른다.
 *
 *   공통 헤더 (제목 · 계정)
 *   상단 탭   (선택된 프로젝트 안의 메뉴 — 개발 보드 / 답변)
 *   본문      (<Outlet />)
 *   하단      (프로젝트 전환)
 *
 * 규칙 두 가지를 지킨다.
 *   - **화면이 역할을 판단하지 않는다.** getMe() 가 내려준 값을 표시만 한다. (기획 13-2)
 *   - 권한 검사는 API 가 한다. 여기서 경로를 막지 않는다. (routes.jsx 주석)
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

	return (
		<div className="admin-shell">
			<header className="admin-header">
				<span className="admin-header__title">통합 피드백 게시판</span>
				{/* TODO(C, 6단계): 계정 메뉴를 열어 계정 관리(/admin/accounts) 진입점을 붙인다. */}
				<span className="admin-header__account">
					{me ? (
						<>
							{me.name}
							<span className="admin-header__role">{labelOf(ROLE, me.role)}</span>
						</>
					) : (
						'로그인이 필요합니다'
					)}
				</span>
			</header>

			{projectCode && (
				<nav className="admin-tabs" aria-label="프로젝트 메뉴">
					<NavLink to={`/admin/${projectCode}/board`} className="admin-tabs__tab">
						개발 보드
					</NavLink>
					<NavLink to={`/admin/${projectCode}/answers`} className="admin-tabs__tab">
						답변
					</NavLink>
				</nav>
			)}

			<main className="admin-body">
				<Outlet />
			</main>

			<ProjectDisc projects={projects} projectCode={projectCode} />

		</div>
	)
}
