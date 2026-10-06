import { useEffect, useRef, useState } from 'react'
import { getAdminUsers } from '../../api/endpoints.js'
import './assignee-dialog.css'

/**
 * 담당자 입력 창. 접수에서 처리 중으로 옮기는 순간에 뜬다.
 *
 * 계약은 팀 합의대로 됐다 — B 가 `feedbacks.assignee_id`(FK, DEVELOPER·ACTIVE 계정만)를 추가하고
 * `PATCH /api/admin/feedbacks/{id}`가 `assigneeId`를 받아 `AdminFeedbackSummary`·`Detail`이
 * `assigneeId`·`assigneeName`을 내려준다(기획 8·9장). 담당자 해제는 `unassign: true`로 한다.
 *
 * 이 창의 입력은 여전히 자유 텍스트(자동완성 제안)지만, 서버는 등록된 개발자 id 만 받는다.
 * 그래서 제출 시 입력한 이름을 developers 목록에서 찾아 id 를 함께 넘긴다. 목록에 없는 이름이면
 * 저장할 수 없다는 뜻이라 제출을 막고 안내한다.
 */
export default function AssigneeDialog({ feedbackTitle, onConfirm, onCancel }) {
	const [name, setName] = useState('')
	const [developers, setDevelopers] = useState([])
	const [error, setError] = useState('')
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
		const trimmed = name.trim()

		// 비워 두면 미지정(해제) — 서버에는 unassign: true 로 보낸다.
		if (!trimmed) {
			setError('')
			onConfirm({ name: '', id: null })
			return
		}

		const matched = developers.find((developer) => developer.name === trimmed)
		if (!matched) {
			setError('목록에 있는 개발자 이름과 정확히 일치해야 저장됩니다.')
			return
		}
		setError('')
		onConfirm({ name: matched.name, id: matched.id })
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

				{error && <p className="dialog__error">{error}</p>}

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
