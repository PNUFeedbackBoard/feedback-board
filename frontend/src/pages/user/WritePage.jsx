import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { createFeedback, getMe, getProjects } from '../../api/endpoints.js'
import { FEEDBACK_CATEGORY, PRIORITY, toOptions } from '../../constants/enums.js'
import UserShell from './UserShell.jsx'
import './write.css'

const CATEGORY_OPTIONS = toOptions(FEEDBACK_CATEGORY)
const PRIORITY_OPTIONS = toOptions(PRIORITY)

const INITIAL_FORM = {
	category: '',
	reportedPriority: 'NORMAL',
	title: '',
	content: '',
}

/**
 * 경로: /write. 피드백 작성 화면. 기획 5-2, 5-3.
 *
 * 사이트 지정 규칙(기획 5-2):
 *   - ?site= 로 들어오면 그 사이트로 고정하고 바꿀 수 없다(진입 링크, 또는 로그인 화면의
 *     비회원 "계속"이 이 값을 그대로 넘겨준다).
 *   - site 가 없으면(회원이 홈에서 "새 질문 적기"를 눌러 들어온 경우) 드롭다운을 보여 준다.
 *
 * 작성자는 본문으로 보내지 않는다 — 서버가 세션으로 판단한다(FeedbackCreateRequest 설명 참고).
 * 그래서 이 화면은 로그인 여부를 "성공 후 어디로 보낼지" 정하는 데만 쓴다.
 */
export default function WritePage() {
	const [searchParams] = useSearchParams()
	const navigate = useNavigate()
	const lockedSite = searchParams.get('site')

	const [projects, setProjects] = useState([])
	const [isMember, setIsMember] = useState(false)
	const [form, setForm] = useState({ ...INITIAL_FORM, projectCode: lockedSite ?? '' })
	const [fieldErrors, setFieldErrors] = useState({})
	const [submitting, setSubmitting] = useState(false)
	const [submitError, setSubmitError] = useState('')
	const [done, setDone] = useState(false)

	useEffect(() => {
		let cancelled = false

		// 드롭다운에 쓸 목록이다. 사이트가 이미 고정이어도 이름 표시에 쓸 수 있어 항상 받아 둔다.
		getProjects()
			.then((list) => !cancelled && setProjects(list))
			.catch(() => !cancelled && setProjects([]))

		// 로그인 여부만 안다 — 등록 자체는 서버가 세션으로 판단하므로 화면은 "성공 후 행선지"에만 쓴다.
		getMe()
			.then(() => !cancelled && setIsMember(true))
			.catch(() => !cancelled && setIsMember(false))

		return () => {
			cancelled = true
		}
	}, [])

	function setField(name, value) {
		setForm((previous) => ({ ...previous, [name]: value }))
		setFieldErrors((previous) => ({ ...previous, [name]: undefined }))
	}

	function validate() {
		const errors = {}
		if (!form.projectCode) errors.projectCode = '사이트를 선택해 주세요.'
		if (!form.category) errors.category = '유형을 선택해 주세요.'
		if (!form.title.trim()) errors.title = '제목을 입력해 주세요.'
		else if (form.title.length > 100) errors.title = '제목은 100자 이하로 입력해 주세요.'
		if (!form.content.trim()) errors.content = '내용을 입력해 주세요.'
		else if (form.content.trim().length < 10 || form.content.length > 2000) {
			errors.content = '내용은 10자 이상 2,000자 이하로 입력해 주세요.'
		}
		return errors
	}

	async function submit(event) {
		event.preventDefault()
		const errors = validate()
		setFieldErrors(errors)
		if (Object.keys(errors).length > 0) return

		setSubmitting(true)
		setSubmitError('')
		try {
			await createFeedback({
				projectCode: form.projectCode,
				category: form.category,
				reportedPriority: form.reportedPriority,
				title: form.title.trim(),
				content: form.content.trim(),
			})
			setDone(true)
		} catch (problem) {
			setSubmitting(false)
			// 서버 검증 오류(필드별)는 그대로 각 칸 아래에 띄운다. 공통 메시지는 폼 위쪽에 둔다.
			if (problem.details?.length) {
				setFieldErrors(Object.fromEntries(problem.details.map((item) => [item.field, item.message])))
			}
			setSubmitError(problem.message)
		}
	}

	function closeDonePopup() {
		navigate(isMember ? '/home' : '/')
	}

	const projectName = projects.find((project) => project.code === lockedSite)?.name

	return (
		<UserShell title="피드백 작성">
			<form className="write" onSubmit={submit} noValidate>
				<h1 className="write__title">피드백 작성</h1>

				{submitError && <p className="write__alert">{submitError}</p>}

				<div className="write__field">
					<span className="write__label">사이트</span>
					{lockedSite ? (
						<p className="write__locked-site">{projectName ?? lockedSite}</p>
					) : (
						<select
							className="write__select"
							value={form.projectCode}
							onChange={(event) => setField('projectCode', event.target.value)}
						>
							<option value="">사이트를 선택하세요</option>
							{projects.map((project) => (
								<option key={project.code} value={project.code}>
									{project.name}
								</option>
							))}
						</select>
					)}
					{fieldErrors.projectCode && <p className="write__field-error">{fieldErrors.projectCode}</p>}
				</div>

				<fieldset className="write__field">
					<legend className="write__label">유형</legend>
					<div className="write__radio-row">
						{CATEGORY_OPTIONS.map((option) => (
							<label key={option.value} className="write__radio">
								<input
									type="radio"
									name="category"
									value={option.value}
									checked={form.category === option.value}
									onChange={(event) => setField('category', event.target.value)}
								/>
								{option.label}
							</label>
						))}
					</div>
					{fieldErrors.category && <p className="write__field-error">{fieldErrors.category}</p>}
				</fieldset>

				<fieldset className="write__field">
					<legend className="write__label">긴급도</legend>
					<div className="write__radio-row">
						{PRIORITY_OPTIONS.map((option) => (
							<label key={option.value} className="write__radio">
								<input
									type="radio"
									name="reportedPriority"
									value={option.value}
									checked={form.reportedPriority === option.value}
									onChange={(event) => setField('reportedPriority', event.target.value)}
								/>
								{option.label}
							</label>
						))}
					</div>
				</fieldset>

				<label className="write__field">
					<span className="write__label">제목</span>
					<input
						className="write__input"
						type="text"
						value={form.title}
						maxLength={100}
						onChange={(event) => setField('title', event.target.value)}
						placeholder="무엇이 문제였는지 한 줄로 적어 주세요."
					/>
					<span className="write__count">{form.title.length} / 100</span>
					{fieldErrors.title && <p className="write__field-error">{fieldErrors.title}</p>}
				</label>

				<label className="write__field">
					<span className="write__label">내용</span>
					<textarea
						className="write__textarea"
						value={form.content}
						maxLength={2000}
						rows={8}
						onChange={(event) => setField('content', event.target.value)}
						placeholder="언제, 어떤 상황에서, 무슨 일이 있었는지 적어 주시면 확인이 빨라집니다."
					/>
					<span className="write__count">{form.content.length} / 2,000</span>
					{fieldErrors.content && <p className="write__field-error">{fieldErrors.content}</p>}
				</label>

				<button type="submit" className="write__submit" disabled={submitting}>
					{submitting ? '제출하는 중…' : '제출하기'}
				</button>
			</form>

			{done && (
				<div className="write__veil">
					<div className="write__dialog" role="dialog" aria-modal="true" aria-labelledby="write-done-title">
						<h2 id="write-done-title">질문이 접수되었습니다</h2>
						<p>
							{isMember
								? '홈에서 처리 상태와 답변을 확인할 수 있습니다.'
								: '비회원으로 등록한 문의는 다시 확인할 수 없습니다. 답변이 필요하면 로그인 후 등록해 주세요.'}
						</p>
						<button type="button" className="write__submit" onClick={closeDonePopup}>
							확인
						</button>
					</div>
				</div>
			)}
		</UserShell>
	)
}
