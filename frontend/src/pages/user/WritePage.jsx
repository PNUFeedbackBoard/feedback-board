import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { createFeedback, getMe, getProjects } from '../../api/endpoints.js'
import { FEEDBACK_CATEGORY, PRIORITY, toOptions } from '../../constants/enums.js'
import '../page-shell.css'

const EMPTY_FORM = {
	projectCode: '',
	category: 'BUG',
	reportedPriority: 'NORMAL',
	title: '',
	content: '',
}

export default function WritePage() {
	const [searchParams] = useSearchParams()
	const requestedSite = searchParams.get('site')
	const [projects, setProjects] = useState([])
	const [form, setForm] = useState(EMPTY_FORM)
	const [busy, setBusy] = useState(false)
	const [message, setMessage] = useState('')
	const [createdId, setCreatedId] = useState(null)
	const [isMember, setIsMember] = useState(false)

	useEffect(() => {
		let cancelled = false
		getProjects()
			.then((found) => {
				if (cancelled) return
				setProjects(found)
				const selected = found.some((project) => project.code === requestedSite)
					? requestedSite
					: found[0]?.code ?? ''
				setForm((current) => ({ ...current, projectCode: selected }))
			})
			.catch((error) => !cancelled && setMessage(error.message))
		return () => {
			cancelled = true
		}
	}, [requestedSite])

	useEffect(() => {
		getMe().then(() => setIsMember(true)).catch(() => setIsMember(false))
	}, [])

	const fixedProject = useMemo(
		() => projects.find((project) => project.code === requestedSite),
		[projects, requestedSite],
	)

	function setField(field, value) {
		setForm((current) => ({ ...current, [field]: value }))
	}

	async function submit(event) {
		event.preventDefault()
		setBusy(true)
		setMessage('')
		try {
			const created = await createFeedback(form)
			setCreatedId(created.id)
		} catch (error) {
			setMessage(error.message)
		} finally {
			setBusy(false)
		}
	}

	if (createdId) {
		return (
			<main className="app-page">
				<div className="app-page__inner app-card app-stack">
					<p className="app-page__eyebrow">RECEIVED</p>
					<h1>피드백이 접수되었습니다</h1>
					<p className="app-success">접수 번호는 #{createdId}입니다.</p>
					<p className="app-page__muted">회원으로 작성했다면 내 문의에서 처리 상태를 계속 확인할 수 있습니다.</p>
					<div className="app-actions">
						{isMember && <Link className="app-button" to="/home">내 문의 보기</Link>}
						<Link className="app-button app-button--secondary" to="/">처음으로</Link>
					</div>
				</div>
			</main>
		)
	}

  return (
		<main className="app-page">
			<div className="app-page__inner app-stack">
				<header className="app-page__header">
					<div>
						<p className="app-page__eyebrow">NEW FEEDBACK</p>
						<h1>피드백 작성</h1>
						<p className="app-page__muted">문제가 발생한 상황과 원하는 개선점을 구체적으로 알려 주세요.</p>
					</div>
					<Link className="app-button app-button--secondary" to="/home">내 문의</Link>
				</header>

				<form className="app-card app-stack" onSubmit={submit}>
					{fixedProject ? (
						<p><strong>대상 서비스</strong><br />{fixedProject.name}</p>
					) : (
						<label className="app-field">
							대상 서비스
							<select value={form.projectCode} onChange={(event) => setField('projectCode', event.target.value)} required>
								{projects.map((project) => <option key={project.code} value={project.code}>{project.name}</option>)}
							</select>
						</label>
					)}

					<div className="app-grid">
						<label className="app-field">
							유형
							<select value={form.category} onChange={(event) => setField('category', event.target.value)}>
								{toOptions(FEEDBACK_CATEGORY).map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
							</select>
						</label>
						<label className="app-field">
							체감 긴급도
							<select value={form.reportedPriority} onChange={(event) => setField('reportedPriority', event.target.value)}>
								{toOptions(PRIORITY).map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
							</select>
						</label>
					</div>

					<label className="app-field">
						제목
						<input value={form.title} onChange={(event) => setField('title', event.target.value)} maxLength={100} required />
					</label>
					<label className="app-field">
						내용
						<textarea value={form.content} onChange={(event) => setField('content', event.target.value)} minLength={10} maxLength={2000} required />
						<small className="app-page__muted">{form.content.length}/2,000자</small>
					</label>

					{message && <p className="app-alert">{message}</p>}
					<div className="app-actions">
						<button className="app-button" type="submit" disabled={busy || !form.projectCode}>
							{busy ? '접수 중…' : '피드백 접수'}
						</button>
					</div>
				</form>
			</div>
		</main>
  )
}
