import { memo, useEffect, useMemo, useState } from 'react'
import { ChevronDown, FileText, Plus } from 'lucide-react'
import { Link } from 'react-router-dom'
import { getMe, getMyFeedbacks } from '../../api/endpoints.js'
import CategoryTag from '../../components/common/CategoryTag.jsx'
import CurrentUserBadge from '../../components/common/CurrentUserBadge.jsx'
import LogoutButton from '../../components/common/LogoutButton.jsx'
import PriorityChip from '../../components/common/PriorityChip.jsx'
import StatusBadge from '../../components/common/StatusBadge.jsx'
import { FEEDBACK_STATUS } from '../../constants/enums.js'
import '../page-shell.css'
import './home-page.css'

const ALL_PROJECTS = 'all'
const PAGE_SIZE = 12
const DAY_FORMATTER = new Intl.DateTimeFormat('ko-KR', { dateStyle: 'long' })

export default function HomePage() {
	const [items, setItems] = useState(null)
	const [currentUser, setCurrentUser] = useState(null)
	const [message, setMessage] = useState('')
	const [activeProject, setActiveProject] = useState(ALL_PROJECTS)
	const [sortOrder, setSortOrder] = useState('latest')
	const [statusFilter, setStatusFilter] = useState('all')
	const [answerFilter, setAnswerFilter] = useState('all')
	const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)

	useEffect(() => {
		let cancelled = false
		Promise.all([getMyFeedbacks(), getMe()])
			.then(([found, me]) => {
				if (cancelled) return
				setItems(found)
				setCurrentUser(me)
			})
			.catch((error) => !cancelled && setMessage(error.message))
		return () => {
			cancelled = true
		}
	}, [])

	const feedbackIndex = useMemo(() => {
		if (!items) return { all: [], byProject: new Map(), projects: [] }
		const projects = new Map()
		items.forEach((item) => {
			const existing = projects.get(item.projectCode)
			projects.set(item.projectCode, {
				code: item.projectCode,
				name: item.projectName,
				count: (existing?.count ?? 0) + 1,
			})
		})
		const all = [...items].sort((a, b) => {
			if (a.createdAt === b.createdAt) return 0
			return a.createdAt < b.createdAt ? 1 : -1
		})
		const byProject = new Map([...projects.keys()].map((code) => [code, []]))
		all.forEach((item) => byProject.get(item.projectCode)?.push(item))
		return { all, byProject, projects: [...projects.values()] }
	}, [items])
	const projectTabs = feedbackIndex.projects

	useEffect(() => {
		if (activeProject !== ALL_PROJECTS && !projectTabs.some(({ code }) => code === activeProject)) {
			setActiveProject(ALL_PROJECTS)
		}
	}, [activeProject, projectTabs])

	const visibleItems = useMemo(() => {
		if (!items) return []
		const sourceItems = activeProject === ALL_PROJECTS
			? feedbackIndex.all
			: feedbackIndex.byProject.get(activeProject) ?? []
		const filtered = sourceItems
			.filter((item) => statusFilter === 'all' || item.status === statusFilter)
			.filter((item) => answerFilter === 'all' || item.answered === (answerFilter === 'answered'))
		return sortOrder === 'latest' ? filtered : filtered.reverse()
	}, [activeProject, answerFilter, feedbackIndex, items, sortOrder, statusFilter])
	const renderedItems = useMemo(
		() => visibleItems.slice(0, visibleCount),
		[visibleCount, visibleItems],
	)

	function changeProject(projectCode) {
		setActiveProject(projectCode)
		setVisibleCount(PAGE_SIZE)
	}

	function changeFilter(setter, value) {
		setter(value)
		setVisibleCount(PAGE_SIZE)
	}

	return (
		<main className="app-page my-feedback-page">
			<div className="app-page__inner my-feedback-page__inner">
				<header className="my-feedback-header">
					<div className="my-feedback-heading">
						<p className="my-feedback-heading__eyebrow"><FileText aria-hidden="true" /> MY FEEDBACK</p>
						<h1>문의 내역</h1>
						<p>서비스별로 남긴 문의와 처리 현황을 한눈에 확인하세요.</p>
					</div>
					<div className="app-header__aside my-feedback-header__aside">
						<CurrentUserBadge user={currentUser} />
						<div className="app-actions">
							<Link className="app-button my-feedback-write-button" to="/write">
								<Plus aria-hidden="true" /> 새 문의 작성
							</Link>
							<LogoutButton />
						</div>
					</div>
				</header>

				{message && <p className="app-alert">{message}</p>}
				{items === null && !message && <LoadingPaper />}
				{items?.length === 0 && <EmptyPaper />}

				{items?.length > 0 && (
					<section className="feedback-notebook" aria-label="내 문의 목록">
						<div className="feedback-bookmarks" role="tablist" aria-label="문의 사이트 선택">
							<ProjectTab
								active={activeProject === ALL_PROJECTS}
								count={items.length}
								index={0}
								label="전체"
								onClick={() => changeProject(ALL_PROJECTS)}
							/>
							{projectTabs.map((project, index) => (
								<ProjectTab
									active={activeProject === project.code}
									count={project.count}
									index={index + 1}
									key={project.code}
									label={project.name}
									onClick={() => changeProject(project.code)}
								/>
							))}
						</div>

						<div className="feedback-notebook__paper" role="tabpanel" id="feedback-project-panel">
							<div className="feedback-filterbar">
								<FilterSelect label="정렬" value={sortOrder} onChange={(value) => changeFilter(setSortOrder, value)}>
									<option value="latest">최신순</option>
									<option value="oldest">오래된순</option>
								</FilterSelect>
								<FilterSelect label="상태" value={statusFilter} onChange={(value) => changeFilter(setStatusFilter, value)}>
									<option value="all">전체 상태</option>
									{Object.entries(FEEDBACK_STATUS).map(([value, label]) => (
										<option key={value} value={value}>{label}</option>
									))}
								</FilterSelect>
								<FilterSelect label="답변" value={answerFilter} onChange={(value) => changeFilter(setAnswerFilter, value)}>
									<option value="all">답변 전체</option>
									<option value="answered">답변 완료</option>
									<option value="waiting">답변 대기</option>
								</FilterSelect>
								<p className="feedback-filterbar__count"><strong>{visibleItems.length}</strong>개의 문의</p>
							</div>

							{visibleItems.length > 0 ? (
								<ul className="feedback-paper-list">
									{renderedItems.map((item) => <FeedbackPaperCard item={item} key={item.id} />)}
								</ul>
							) : (
								<div className="feedback-filter-empty">
									<span aria-hidden="true">⌁</span>
									<strong>조건에 맞는 문의가 없어요.</strong>
									<p>필터를 바꾸면 다른 문의를 확인할 수 있습니다.</p>
								</div>
							)}
							{visibleCount < visibleItems.length && (
								<button
									className="feedback-load-more"
									onClick={() => setVisibleCount((count) => count + PAGE_SIZE)}
									type="button"
								>
									더 보기 <span>{visibleItems.length - renderedItems.length}개 남음</span>
								</button>
							)}
						</div>
					</section>
				)}
			</div>
		</main>
	)
}

const FeedbackPaperCard = memo(function FeedbackPaperCard({ item }) {
	return (
		<li>
			<Link className="feedback-paper-card" to={`/my/${item.id}`}>
				<div className="feedback-paper-card__body">
					<p className="feedback-paper-card__site">
						<span aria-hidden="true" /> {item.projectName}
					</p>
					<strong className="feedback-paper-card__title">{item.title}</strong>
					<p className="feedback-paper-card__date">
						<time dateTime={item.createdAt}>{formatDay(item.createdAt)}</time>
						<span aria-hidden="true">·</span>
						문의 #{item.id}
					</p>
				</div>
				<div className="feedback-paper-card__badges" aria-label="문의 분류와 처리 정보">
					<CategoryTag category={item.category} />
					<PriorityChip priority={item.reportedPriority} />
					<StatusBadge status={item.status} />
					<span className={`answer-stamp ${item.answered ? 'answer-stamp--done' : ''}`}>
						{item.answered ? '답변 완료' : '답변 대기'}
					</span>
				</div>
				<span className="feedback-paper-card__arrow" aria-hidden="true">→</span>
			</Link>
		</li>
	)
})

function ProjectTab({ active, count, index, label, onClick }) {
	return (
		<button
			aria-controls="feedback-project-panel"
			aria-label={`${label}, 문의 ${count}개`}
			aria-selected={active}
			className="feedback-bookmark"
			data-color={index % 6}
			onClick={onClick}
			role="tab"
			type="button"
		>
			<span className="feedback-bookmark__label">{label}</span>
			<span className="feedback-bookmark__count" aria-hidden="true">{count}</span>
		</button>
	)
}

function FilterSelect({ children, label, onChange, value }) {
	return (
		<label className="paper-select">
			<span className="paper-select__label">{label}</span>
			<select value={value} onChange={(event) => onChange(event.target.value)}>
				{children}
			</select>
			<ChevronDown aria-hidden="true" />
		</label>
	)
}

function LoadingPaper() {
	return (
		<div className="feedback-state-paper" aria-live="polite">
			<span className="feedback-state-paper__mark" aria-hidden="true">···</span>
			<strong>문의 내역을 펼치는 중이에요.</strong>
		</div>
	)
}

function EmptyPaper() {
	return (
		<div className="feedback-state-paper">
			<span className="feedback-state-paper__mark" aria-hidden="true">✎</span>
			<strong>아직 남긴 문의가 없어요.</strong>
			<p>첫 문의를 남기면 해당 서비스의 책갈피가 여기에 생깁니다.</p>
			<Link className="app-button my-feedback-write-button" to="/write"><Plus aria-hidden="true" /> 첫 문의 작성</Link>
		</div>
	)
}

function formatDay(value) {
	return DAY_FORMATTER.format(new Date(value))
}
