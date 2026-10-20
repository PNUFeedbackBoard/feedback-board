import { useEffect, useMemo, useState } from 'react'
import { ArrowLeft, CheckCircle2, FilePenLine, Home, Inbox, Send } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { createFeedback, getMe, getProjects } from '../../api/endpoints.js'
import CurrentUserBadge from '../../components/common/CurrentUserBadge.jsx'
import LogoutButton from '../../components/common/LogoutButton.jsx'
import { FEEDBACK_CATEGORY, PRIORITY, toOptions } from '../../constants/enums.js'
import '../page-shell.css'
import './write-page.css'

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
	const [projectsReady, setProjectsReady] = useState(false)
	const [form, setForm] = useState(EMPTY_FORM)
	const [busy, setBusy] = useState(false)
	const [message, setMessage] = useState('')
	const [createdId, setCreatedId] = useState(null)
	const [currentUser, setCurrentUser] = useState(undefined)
	const [projectChoice, setProjectChoice] = useState({ requestedSite, open: false })
	const projectChoiceOpen = projectChoice.requestedSite === requestedSite && projectChoice.open

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
				setProjectsReady(true)
			})
			.catch((error) => {
				if (cancelled) return
				setProjectsReady(true)
				setMessage(error.message)
			})
		return () => {
			cancelled = true
		}
	}, [requestedSite])

	useEffect(() => {
		getMe().then(setCurrentUser).catch(() => setCurrentUser(null))
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
		return <ReceiptComplete createdId={createdId} isMember={Boolean(currentUser)} />
	}

	return (
		<main className="app-page write-feedback-page">
			<div className="app-page__inner write-feedback-page__inner">
				<header className="write-feedback-header">
					<div className="write-feedback-heading">
						<Link className="write-feedback-back" to={currentUser ? '/home' : '/'}>
							<ArrowLeft aria-hidden="true" /> {currentUser ? '문의 내역' : '처음으로'}
						</Link>
						<p className="write-feedback-heading__eyebrow"><FilePenLine aria-hidden="true" /> NEW FEEDBACK</p>
						<h1>새 문의 작성</h1>
					</div>
					{currentUser && (
						<div className="app-header__aside write-feedback-header__aside">
							<CurrentUserBadge user={currentUser} />
							<div className="app-actions">
								<Link className="app-button app-button--secondary" to="/home"><Inbox aria-hidden="true" /> 내 문의</Link>
								<LogoutButton />
							</div>
						</div>
					)}
				</header>

				<form className="feedback-form-paper" onSubmit={submit}>
					<div className="feedback-form-paper__topline" aria-hidden="true">
						<span>ISSUE &amp; IDEA</span>
						<span>FEEDBACK FORM · 01</span>
					</div>

					<section className="feedback-form-section" aria-labelledby="service-section-title">
						<div className="feedback-form-section__number">01</div>
						<div className="feedback-form-section__content">
							<div className="feedback-form-section__heading">
								<div>
									<p>어디에서 발견했나요?</p>
									<h2 id="service-section-title">대상 서비스</h2>
								</div>
								<span>필수</span>
							</div>

							{!projectsReady ? (
								<p className="paper-notice" role="status">대상 서비스를 확인하는 중…</p>
							) : fixedProject && !projectChoiceOpen ? (
								<div className="paper-source-service" role="status">
									<div className="paper-source-service__mark" aria-hidden="true"><CheckCircle2 /></div>
									<div className="paper-source-service__summary">
										<span>연결된 서비스</span>
										<strong>{fixedProject.name}</strong>
										<small>서비스의 피드백 버튼을 통해 자동 선택되었습니다.</small>
									</div>
									<button
										className="paper-text-button"
										type="button"
										onClick={() => setProjectChoice({ requestedSite, open: true })}
									>
										다른 서비스 선택
									</button>
								</div>
							) : (
								<div className="paper-field-stack">
									<label className="paper-field">
										<span>서비스 선택</span>
										<select value={form.projectCode} onChange={(event) => setField('projectCode', event.target.value)} required>
											{projects.map((project) => <option key={project.code} value={project.code}>{project.name}</option>)}
										</select>
									</label>
									{fixedProject && (
										<button
											className="paper-text-button paper-text-button--restore"
											type="button"
											onClick={() => {
												setField('projectCode', fixedProject.code)
												setProjectChoice({ requestedSite, open: false })
											}}
										>
											{fixedProject.name} 자동 선택으로 돌아가기
										</button>
									)}
								</div>
							)}
						</div>
					</section>

					<div className="feedback-form-paper__rule" />

					<section className="feedback-form-section" aria-labelledby="detail-section-title">
						<div className="feedback-form-section__number">02</div>
						<div className="feedback-form-section__content">
							<div className="feedback-form-section__heading">
								<div>
									<p>무엇을 알려주고 싶나요?</p>
									<h2 id="detail-section-title">문의 내용</h2>
								</div>
								<span>필수</span>
							</div>

							<div className="paper-field-grid">
								<label className="paper-field">
									<span>문의 유형</span>
									<select value={form.category} onChange={(event) => setField('category', event.target.value)}>
										{toOptions(FEEDBACK_CATEGORY).map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
									</select>
								</label>
								<label className="paper-field">
									<span>체감 긴급도</span>
									<select value={form.reportedPriority} onChange={(event) => setField('reportedPriority', event.target.value)}>
										{toOptions(PRIORITY).map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
									</select>
								</label>
							</div>

							<label className="paper-field">
								<span>제목</span>
								<input
									value={form.title}
									onChange={(event) => setField('title', event.target.value)}
									maxLength={100}
									placeholder="문의 내용을 한 문장으로 적어주세요"
									required
								/>
							</label>
							<div className="paper-field">
								<label htmlFor="feedback-content">상세 내용</label>
								<textarea
									id="feedback-content"
									value={form.content}
									onChange={(event) => setField('content', event.target.value)}
									minLength={10}
									maxLength={2000}
									placeholder="발생한 상황, 기대했던 결과, 개선 아이디어를 구체적으로 적어주세요."
									aria-describedby="feedback-content-count"
									required
								/>
								<small id="feedback-content-count" className="paper-field__count">{form.content.length.toLocaleString('ko-KR')} / 2,000자</small>
							</div>
						</div>
					</section>

					{message && <p className="paper-alert" role="alert">{message}</p>}

					<footer className="feedback-form-paper__footer">
						<p><span aria-hidden="true">✦</span> 보내주신 의견은 서비스 개선에 소중히 활용됩니다.</p>
						<button className="paper-submit-button" type="submit" disabled={busy || !form.projectCode}>
							<Send aria-hidden="true" /> {busy ? '접수 중…' : '피드백 접수'}
						</button>
					</footer>
				</form>
			</div>
		</main>
	)
}

function ReceiptComplete({ createdId, isMember }) {
	return (
		<main className="app-page write-feedback-page write-feedback-page--complete">
			<div className="app-page__inner write-feedback-page__inner write-feedback-page__inner--complete">
				<section className="feedback-receipt" aria-labelledby="receipt-title">
					<div className="feedback-receipt__binder" aria-hidden="true" />
					<div className="feedback-receipt__icon" aria-hidden="true"><CheckCircle2 /></div>
					<p className="feedback-receipt__eyebrow">RECEIVED · THANK YOU</p>
					<h1 id="receipt-title">피드백이 접수되었습니다</h1>
					<p className="feedback-receipt__description">소중한 의견을 남겨주셔서 감사합니다.<br />담당자가 내용을 꼼꼼히 확인할게요.</p>

					<div className="feedback-receipt__number">
						<span>접수 번호</span>
						<strong>#{createdId}</strong>
						<small>문의 확인 시 필요한 번호입니다.</small>
					</div>

					<ol className="feedback-receipt__steps" aria-label="문의 처리 과정">
						<li className="is-complete"><span>1</span><strong>접수 완료</strong></li>
						<li><span>2</span><strong>내용 확인</strong></li>
						<li><span>3</span><strong>처리·답변</strong></li>
					</ol>

					{isMember && <p className="feedback-receipt__member-note"><Inbox aria-hidden="true" /> 내 문의에서 처리 상태와 답변을 계속 확인할 수 있어요.</p>}

					<div className="feedback-receipt__actions">
						{isMember && <Link className="paper-submit-button" to="/home"><Inbox aria-hidden="true" /> 내 문의 보기</Link>}
						<Link className="paper-secondary-button" to="/"><Home aria-hidden="true" /> 처음으로</Link>
					</div>
					<p className="feedback-receipt__footer">PNU AI INSTITUTE · ISSUE &amp; IDEA ARCHIVE</p>
				</section>
			</div>
		</main>
	)
}
