import { Link } from 'react-router-dom'
import iniLogo from '../../assets/ini-logo.png'
import './user-shell.css'

/**
 * 사용자용 화면 공통 헤더. 로그인 화면 다음부터(작성 · 홈 · 문의 상세) 쓴다.
 *
 * 관리용 레이아웃(`AdminLayout.jsx`)의 브랜드 마크를 그대로 가져와 같은 서비스처럼 보이게
 * 하되, 탭이나 프로젝트 전환 같은 관리자 전용 요소는 없다 — 사용자 쪽은 화면이 적고
 * 동선이 선형이라 그런 틀이 필요 없다.
 *
 * @param {{ title?: string }} props title 은 헤더 가운데에 작게 띄우는 화면 이름이다
 */
export default function UserShell({ title, children }) {
	return (
		<div className="user-shell">
			<header className="user-shell__header">
				<Link to="/home" className="user-shell__brand" aria-label="INI 홈으로">
					<img className="user-shell__mark" src={iniLogo} alt="" aria-hidden="true" />
					<span className="user-shell__type">
						<strong>INI</strong>
						<span>ISSUE &amp; IDEA</span>
					</span>
				</Link>
				{title && <span className="user-shell__title">{title}</span>}
			</header>
			<main className="user-shell__body">{children}</main>
		</div>
	)
}
