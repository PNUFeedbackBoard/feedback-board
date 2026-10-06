import { useEffect, useRef, useState } from 'react'
import { getAdminUsers } from '../../api/endpoints.js'
import './assignee-dialog.css'

/**
 * 담당자 입력 창. 접수에서 처리 중으로 옮기는 순간에 뜬다.
 *
 * 선택한 개발자 계정 id는 상태 변경과 함께 assigneeId 필드로 서버에 저장된다.
 */
export default function AssigneeDialog({ feedbackTitle, onConfirm, onCancel }) {
	const [assigneeId, setAssigneeId] = useState('')
	const [developers, setDevelopers] = useState([])
	const inputRef = useRef(null)

	useEffect(() => {
		inputRef.current?.focus()

		// 개발자 목록을 받아 고를 수 있게 한다. 이름을 매번 손으로 적으면 표기가 갈린다.
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
		onConfirm(assigneeId ? Number(assigneeId) : null)
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

				<select
					ref={inputRef}
					className="dialog__select"
					value={assigneeId}
					onChange={(event) => setAssigneeId(event.target.value)}
				>
					<option value="">담당자 미지정</option>
					{developers.map((developer) => (
						<option key={developer.id} value={developer.id}>
							{developer.name}
						</option>
					))}
				</select>

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
