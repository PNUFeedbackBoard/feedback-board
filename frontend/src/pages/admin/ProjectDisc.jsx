import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { House } from 'lucide-react'
import './project-disc.css'

/**
 * 화면 아래에 고정된 프로젝트 전환 바.
 * 움직이는 원판 대신 익숙한 도구 모음 모양을 사용해 위치가 흔들리지 않고 바로 선택할 수 있다.
 */
export default function ProjectDisc({ projects, projectCode }) {
	const navigate = useNavigate()
	const slots = useMemo(() => [
		{ key: '', name: '전체 현황', to: '/admin', home: true },
		...projects.map((project) => ({
			key: project.code,
			name: project.name,
			logoUrl: project.logoUrl,
			to: `/admin/${project.code}/board`,
		})),
	], [projects])

	return (
		<div className="disc">
			<nav className="disc__slots" aria-label="프로젝트 전환">
				{slots.map((slot, index) => {
					const selected = slot.key === (projectCode ?? '')
					return (
						<button
							key={slot.to}
							type="button"
							className={selected ? 'disc__slot is-selected' : 'disc__slot'}
							aria-current={selected ? 'page' : undefined}
							onClick={() => navigate(slot.to)}
							title={slot.name}
						>
							<span className="disc__logo" data-slot={slot.home ? 'home' : index % 5} aria-hidden="true">
								<ProjectLogo slot={slot} />
							</span>
							<span className="disc__name">{slot.name}</span>
						</button>
					)
				})}
			</nav>
		</div>
	)
}

function ProjectLogo({ slot }) {
	const [failedUrl, setFailedUrl] = useState(null)
	if (slot.home) return <House size={18} strokeWidth={2} />
	if (slot.logoUrl && failedUrl !== slot.logoUrl) {
		return <img src={slot.logoUrl} alt="" onError={() => setFailedUrl(slot.logoUrl)} />
	}
	return slot.name.slice(0, 1)
}
