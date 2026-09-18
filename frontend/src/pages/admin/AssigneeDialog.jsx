import { useEffect, useRef, useState } from 'react'
import { getAdminUsers } from '../../api/endpoints.js'
import './assignee-dialog.css'

/**
 * 담당자 입력 창. 접수에서 처리 중으로 옮기는 순간에 뜬다.
 *
 * **기획에 없는 기능이다.** 8장 데이터 모델의 feedbacks 에 담당자 컬럼이 없고
 * AdminFeedbackSummary 에도 필드가 없다. 그래서 지금은 화면 안에서만 유지되고
 * 새로고침하면 사라진다.
 *
 * TODO(팀 합의 필요): 담당자를 서버에 남기려면 계약이 바뀌어야 한다.
 *                    A - feedbacks 에 assignee_id 추가, Feedback 엔티티에 필드 추가
 *                    B - PATCH /api/admin/feedbacks/{id} 가 assigneeId 를 받고
 *                        AdminFeedbackSummary·Detail 이 assigneeName 을 내려준다
 *                    계약이 생기면 이 창은 그대로 두고 저장 부분만 바꾸면 된다.
 */
export default function AssigneeDialog({ feedbackTitle, onConfirm, onCancel }) {
	const [name, setName] = useState('')
	const [developers, setDevelopers] = useState([])
	const inputRef = useRef(null)

	useEffect(() => {
		inputRef.current?.focus()

		// 개발자 목록을 받아 고를 수 있게 한다. 이름을 매번 손으로 적으면 표기가 갈린다.
		// 열람자 계정은 이 API 에서 403 을 받으므로 그때는 직접 입력만 남는다.
		let cancelled = false
		getAdminUsers()
			.then((users) => {
				if (cancelled) return
				setDevelopers(users.filter((user) => user.role === 'DEVELOPER' && user.status === 'ACTIVE'))
			})
			.catch(() => setDevelopers([]))

		return () => {
			cancelled = true
		}
	}, [])

	function submit(event) {
		event.preventDefault()
		onConfirm(name.trim())
	}

	return (
		<div className="dialog-veil" onPointerDown={(event) => event.target === event.currentTarget && onCancel()}>
			<form
				className="dialog"
				onSubmit={submit}
				onKeyDown={(event) => event.key === 'Escape' && onCancel()}
			>
				<h2 className="dialog__title">담당자 지정</h2>
				<p className="dialog__subject">{feedbackTitle}</p>
				<p className="dialog__help">처리 중으로 옮깁니다. 누가 맡는지 적어 두면 보드에서 바로 보입니다.</p>

				<input
					ref={inputRef}
					className="dialog__input"
					list="assignee-candidates"
					value={name}
					onChange={(event) => setName(event.target.value)}
					placeholder="이름 (비워 두면 미지정)"
					autoComplete="off"
				/>
				<datalist id="assignee-candidates">
					{developers.map((developer) => (
						<option key={developer.id} value={developer.name} />
					))}
				</datalist>

				<div className="dialog__actions">
					<button type="button" className="dialog__button" onClick={onCancel}>
						취소
					</button>
					<button type="submit" className="dialog__button dialog__button--primary">
						옮기기
					</button>
				</div>
			</form>
		</div>
	)
}
