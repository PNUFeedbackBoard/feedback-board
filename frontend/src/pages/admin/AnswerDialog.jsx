import { useEffect, useRef, useState } from 'react'
import { getAdminFeedback, upsertAnswer } from '../../api/endpoints.js'
import './assignee-dialog.css'

/**
 * 답변 작성 창. 기획 6-4
 *
 * 답변은 피드백 1건당 1건이므로 등록과 수정을 하나의 PUT 으로 처리한다.
 * 상태를 함께 `DONE` 으로 바꾸는 선택 항목은 기획이 정한 것이다.
 *
 * 목록 응답에는 본문이 없어(AdminFeedbackSummary 에 content 가 없다) 창을 열 때 상세를 따로 받는다.
 */
export default function AnswerDialog({ feedback, onDone, onCancel }) {
	const [content, setContent] = useState('')
	const [markDone, setMarkDone] = useState(true)
	const [detail, setDetail] = useState(null)
	const [saving, setSaving] = useState(false)
	const [error, setError] = useState('')
	const inputRef = useRef(null)

	useEffect(() => {
		inputRef.current?.focus()

		let cancelled = false
		getAdminFeedback(feedback.id)
			.then((found) => {
				if (cancelled) return
				setDetail(found)
				// 이미 답변이 있으면 그 내용을 불러 고쳐 쓰게 한다.
				if (found.answer) setContent(found.answer.content)
			})
			.catch(() => setDetail(null))

		return () => {
			cancelled = true
		}
	}, [feedback.id])

	function submit(event) {
		event.preventDefault()
		if (!content.trim() || saving) return

		setSaving(true)
		setError('')
		upsertAnswer(feedback.id, { content: content.trim(), markDone })
			.then(() => onDone(markDone))
			.catch((problem) => {
				setSaving(false)
				setError(problem.message)
			})
	}

	return (
		<div
			className="dialog-veil"
			onPointerDown={(event) => event.target === event.currentTarget && onCancel()}
		>
			<form
				className="dialog dialog--wide"
				onSubmit={submit}
				onKeyDown={(event) => event.key === 'Escape' && onCancel()}
			>
				<h2 className="dialog__title">답변 작성</h2>
				<p className="dialog__subject">{feedback.title}</p>

				{/* 문의 전문. 아직 받아 오는 중이면 자리만 비워 둔다. */}
				{detail && <p className="dialog__body">{detail.content}</p>}

				<textarea
					ref={inputRef}
					className="dialog__textarea"
					value={content}
					onChange={(event) => setContent(event.target.value)}
					placeholder="답변 내용을 적어 주세요."
					rows={6}
				/>

				<label className="dialog__check">
					<input
						type="checkbox"
						checked={markDone}
						onChange={(event) => setMarkDone(event.target.checked)}
					/>
					답변과 함께 처리 완료로 바꾸기
				</label>

				{error && <p className="dialog__error">{error}</p>}

				<div className="dialog__actions">
					<button type="button" className="dialog__button" onClick={onCancel}>
						취소
					</button>
					<button
						type="submit"
						className="dialog__button dialog__button--primary"
						disabled={!content.trim() || saving}
					>
						{saving ? '보내는 중…' : '답변 등록'}
					</button>
				</div>
			</form>
		</div>
	)
}
