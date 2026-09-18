import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import {
	DndContext,
	DragOverlay,
	PointerSensor,
	useDraggable,
	useDroppable,
	useSensor,
	useSensors,
} from '@dnd-kit/core'
import { getAdminFeedbacks, updateAdminFeedback } from '../../api/endpoints.js'
import { AUTHOR_TYPE, BOARD_COLUMN_STATUSES, FEEDBACK_STATUS, labelOf } from '../../constants/enums.js'
import AssigneeDialog from './AssigneeDialog.jsx'
import CategoryTag from './components/CategoryTag.jsx'
import PriorityChip from './components/PriorityChip.jsx'
import PriorityRequestBadge from './components/PriorityRequestBadge.jsx'
import StatusBadge from './components/StatusBadge.jsx'
import './board.css'

/**
 * 개발 보드 — 칸반. 기획 6-3
 *
 * 카드를 다른 열로 끌어다 놓으면 상태가 바뀐다.
 * **0-1단계 스텁은 PATCH 를 받아도 저장하지 않으므로 새로고침하면 되돌아간다.**
 * B 의 변경 API 가 준비되면 화면 수정 없이 그대로 남는다.
 *
 * 접수에서 처리 중으로 넘어갈 때만 담당자를 묻는다. 일이 시작되는 시점이라
 * 그때 적어 두면 보드에서 누가 무엇을 잡고 있는지 한눈에 보인다.
 * 담당자는 기획에 없는 기능이라 아직 화면 안에서만 유지된다. AssigneeDialog 의 주석 참고.
 *
 * TODO(C, 3단계): 카드 상세 패널(전체 내용, 유형·중요도 변경, 답변 화면 이동)을 붙인다.
 * TODO(C, 5단계): 상단에 정렬·유형·기간 필터를 배치한다. 정렬은 각 열 내부에 적용한다.
 */
export default function BoardPage() {
	const { projectCode } = useParams()
	// 응답이 어느 프로젝트의 것인지 함께 담아 둔다. 그래야 프로젝트를 옮긴 직후에
	// effect 안에서 상태를 되돌리지 않고도 이전 응답을 화면에서 걸러 낼 수 있다.
	const [state, setState] = useState({ projectCode: null, items: [], message: '' })
	/** 화면 안에서만 유지되는 담당자. { [피드백 번호]: 이름 } */
	const [assignees, setAssignees] = useState({})
	/** 끌고 있는 카드. 손에 들린 모습을 따로 그리는 데 쓴다. */
	const [dragging, setDragging] = useState(null)
	/** 담당자 입력을 기다리는 이동. 창에서 확인을 눌러야 실제로 옮긴다. */
	const [pending, setPending] = useState(null)
	/** 저장에 실패했을 때의 안내. 보드는 그대로 두고 한 줄만 띄운다. */
	const [notice, setNotice] = useState('')

	// 살짝 눌렀다 떼는 것은 클릭으로 남겨 둔다. 6px 넘게 움직여야 끌기로 친다.
	const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 6 } }))

	useEffect(() => {
		let cancelled = false

		// 0-1단계 스텁은 필터를 무시하고 14건을 전부 내려준다.
		// 계약대로 project 를 보내 두면 B 의 1단계 작업이 끝나는 순간 화면 수정 없이 걸러진다.
		getAdminFeedbacks({ project: projectCode })
			.then((page) => {
				if (!cancelled) setState({ projectCode, items: page.items, message: '' })
			})
			.catch((error) => {
				if (!cancelled) setState({ projectCode, items: [], message: error.message })
			})

		return () => {
			cancelled = true
		}
	}, [projectCode])

	/**
	 * 먼저 화면을 옮기고 서버에 알린다. 실패하면 되돌리고 이유를 띄운다.
	 * 끌어다 놓은 손맛이 서버 응답을 기다리느라 끊기지 않게 하기 위한 순서다.
	 */
	function commitMove(id, status, assignee) {
		const before = state.items
		setState((prev) => ({
			...prev,
			items: prev.items.map((item) => (item.id === id ? { ...item, status } : item)),
		}))
		if (assignee !== undefined) {
			setAssignees((prev) => ({ ...prev, [id]: assignee }))
		}
		setNotice('')

		updateAdminFeedback(id, { status }).catch((error) => {
			setState((prev) => ({ ...prev, items: before }))
			setNotice(`상태를 바꾸지 못했습니다. ${error.message}`)
		})
	}

	function handleDragEnd(event) {
		setDragging(null)
		const { active, over } = event
		if (!over) return

		const card = state.items.find((item) => item.id === active.id)
		if (!card || card.status === over.id) return

		// 접수에서 처리 중으로 넘어가는 순간이 일이 시작되는 시점이다. 그때만 담당자를 묻는다.
		if (card.status === 'RECEIVED' && over.id === 'IN_PROGRESS') {
			setPending({ id: card.id, status: over.id, title: card.title })
			return
		}
		commitMove(card.id, over.id)
	}

	if (state.projectCode !== projectCode) return <p className="board__notice">불러오는 중…</p>
	if (state.message) return <p className="board__notice">{state.message}</p>

	const rejected = state.items.filter((item) => item.status === 'REJECTED')

	return (
		<DndContext
			sensors={sensors}
			onDragStart={(event) => setDragging(state.items.find((item) => item.id === event.active.id))}
			onDragCancel={() => setDragging(null)}
			onDragEnd={handleDragEnd}
		>
			<div className="board">
				{notice && <p className="board__alert">{notice}</p>}

				<div className="board__columns">
					{BOARD_COLUMN_STATUSES.map((status) => (
						<Column
							key={status}
							status={status}
							// 서버가 정렬해 내려준 순서를 그대로 쓴다. 우선 처리 요청 항목이 최상단에 오는 것도
							// 서버가 정한다(기획 4-5). 화면에서 다시 정렬하지 않는다.
							items={state.items.filter((item) => item.status === status)}
							assignees={assignees}
						/>
					))}
				</div>

				{/* 반영 불가는 열을 차지하지 않고 보드 하단에 접은 상태로 둔다. 기획 6-3 */}
				<RejectedArea items={rejected} assignees={assignees} />
			</div>

			{/* 끌고 있는 동안 손에 들린 카드. 원래 자리의 카드는 흐려진다. */}
			<DragOverlay dropAnimation={null}>
				{dragging && (
					<div className="card-held">
						<FeedbackCard item={dragging} assignee={assignees[dragging.id]} />
					</div>
				)}
			</DragOverlay>

			{pending && (
				<AssigneeDialog
					feedbackTitle={pending.title}
					onCancel={() => setPending(null)}
					onConfirm={(name) => {
						commitMove(pending.id, pending.status, name)
						setPending(null)
					}}
				/>
			)}
		</DndContext>
	)
}

/** 칸반의 한 열. 카드를 받아 준다. */
function Column({ status, items, assignees }) {
	const { setNodeRef, isOver } = useDroppable({ id: status })

	return (
		<section ref={setNodeRef} className={isOver ? 'board__column is-over' : 'board__column'}>
			<header className="board__column-head">
				<StatusBadge status={status} />
				<span className="board__count">{items.length}</span>
			</header>

			<div className="board__cards">
				{items.map((item) => (
					<DraggableCard key={item.id} item={item} assignee={assignees[item.id]} />
				))}
				{items.length === 0 && <p className="board__empty">여기로 끌어다 놓으세요</p>}
			</div>
		</section>
	)
}

/** 반영 불가. 열이 아니지만 여기로도 끌어다 놓을 수 있다. */
function RejectedArea({ items, assignees }) {
	const { setNodeRef, isOver } = useDroppable({ id: 'REJECTED' })

	return (
		<details ref={setNodeRef} className={isOver ? 'board__rejected is-over' : 'board__rejected'}>
			<summary>
				{labelOf(FEEDBACK_STATUS, 'REJECTED')}
				<span className="board__count">{items.length}</span>
			</summary>
			<div className="board__cards board__cards--row">
				{items.map((item) => (
					<DraggableCard key={item.id} item={item} assignee={assignees[item.id]} />
				))}
				{items.length === 0 && <p className="board__empty">항목이 없습니다</p>}
			</div>
		</details>
	)
}

/** 끌 수 있는 카드. 끄는 동안 원래 자리는 흐려지고 실제 모습은 DragOverlay 가 그린다. */
function DraggableCard({ item, assignee }) {
	const { attributes, listeners, setNodeRef, isDragging } = useDraggable({ id: item.id })

	return (
		<div
			ref={setNodeRef}
			className={isDragging ? 'card-slot is-dragging' : 'card-slot'}
			{...listeners}
			{...attributes}
		>
			<FeedbackCard item={item} assignee={assignee} />
		</div>
	)
}

/**
 * 카드 표시 항목은 기획 6-3 이 정한 7가지다.
 * 우선 처리 요청 배지 · 중요도 · 제목 · 유형 · 경과일 · 회원 여부 · 답변 여부
 * 담당자는 그 위에 얹은 항목이며 지정된 카드에만 나온다.
 */
function FeedbackCard({ item, assignee }) {
	return (
		<article className="card" data-priority-requested={item.priorityRequested}>
			{item.priorityRequested && <PriorityRequestBadge />}

			<h3 className="card__title">{item.title}</h3>

			<div className="card__tags">
				<PriorityChip priority={item.priority} />
				<CategoryTag category={item.category} />
			</div>

			<footer className="card__meta">
				<span>{daysSince(item.createdAt)}일 경과</span>
				<span>{labelOf(AUTHOR_TYPE, item.authorType)}</span>
				<span>{item.answered ? '답변 완료' : '답변 없음'}</span>
			</footer>

			{assignee !== undefined && (
				<p className="card__assignee">{assignee ? `담당 ${assignee}` : '담당 미지정'}</p>
			)}
		</article>
	)
}

/**
 * 등록일로부터 며칠 지났는지. 기획 6-3 의 "경과일" 이다.
 * createdAt 은 시간대 없는 LocalDateTime 문자열이라 브라우저 현지 시각으로 해석된다.
 */
function daysSince(createdAt) {
	const MS_PER_DAY = 24 * 60 * 60 * 1000
	return Math.max(0, Math.floor((Date.now() - new Date(createdAt).getTime()) / MS_PER_DAY))
}
