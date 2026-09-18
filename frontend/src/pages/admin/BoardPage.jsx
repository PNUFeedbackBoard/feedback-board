import { useEffect, useState } from 'react'
import { useOutletContext, useParams } from 'react-router-dom'
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
import { FEEDBACK_STATUS, labelOf } from '../../constants/enums.js'
import DetailPanel from './DetailPanel.jsx'
import FeedbackCard from './components/FeedbackCard.jsx'
import StatusBadge from './components/StatusBadge.jsx'
import './board.css'

/**
 * 개발 보드 — 진행 중인 일만 보는 칸반이다. 기획 6-3
 *
 * **팀 확인이 필요한 기획 변경이다.** 기획의 칸반은 진행 전·진행 중·완료 세 열인데
 * 진행 전(접수)을 별도 탭으로 떼어 두 열로 줄였다. 이유는 IntakePage 의 주석에 적었다.
 *
 * 상태를 바꾸는 길이 둘이다. 기획 6-3 이 "카드를 이동" 과 "상세 패널에서도 변경" 을 함께 정했다.
 * 끌어다 놓기는 여러 건을 훑어 옮길 때, 패널은 한 건을 확인하고 정확히 바꿀 때 낫다.
 *
 * 카드를 다른 열로 끌어다 놓으면 상태가 바뀐다.
 * **0-1단계 스텁은 PATCH 를 받아도 저장하지 않으므로 새로고침하면 되돌아간다.**
 * B 의 변경 API 가 준비되면 화면 수정 없이 그대로 남는다.
 *
 */

/** 이 보드가 열로 쓰는 상태. 접수는 별도 탭이라 빠졌고 반영 불가는 하단 접힘이다. */
const BOARD_COLUMNS = ['IN_PROGRESS', 'DONE']

export default function BoardPage() {
	const { projectCode } = useParams()
	const { assignees } = useOutletContext()
	// 응답이 어느 프로젝트의 것인지 함께 담아 둔다. 그래야 프로젝트를 옮긴 직후에
	// effect 안에서 상태를 되돌리지 않고도 이전 응답을 화면에서 걸러 낼 수 있다.
	const [state, setState] = useState({ projectCode: null, items: [], message: '' })
	/** 끌고 있는 카드. 손에 들린 모습을 따로 그리는 데 쓴다. */
	const [dragging, setDragging] = useState(null)
	/** 저장에 실패했을 때의 안내. 보드는 그대로 두고 한 줄만 띄운다. */
	const [notice, setNotice] = useState('')
	/** 상세 패널에 띄운 피드백 번호. 목록이 바뀌어도 같은 건을 따라가도록 번호만 들고 있다. */
	const [openedId, setOpenedId] = useState(null)

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
	function commitChange(id, changes) {
		const before = state.items
		setState((prev) => ({
			...prev,
			items: prev.items.map((item) => (item.id === id ? { ...item, ...changes } : item)),
		}))
		setNotice('')

		updateAdminFeedback(id, changes).catch((error) => {
			setState((prev) => ({ ...prev, items: before }))
			setNotice(`바꾸지 못했습니다. ${error.message}`)
		})
	}

	function handleDragEnd(event) {
		setDragging(null)
		const { active, over } = event
		if (!over) return

		const card = state.items.find((item) => item.id === active.id)
		if (!card || card.status === over.id) return
		commitChange(card.id, { status: over.id })
	}

	if (state.projectCode !== projectCode) return <p className="board__notice">불러오는 중…</p>
	if (state.message) return <p className="board__notice">{state.message}</p>

	const rejected = state.items.filter((item) => item.status === 'REJECTED')
	const waiting = state.items.filter((item) => item.status === 'RECEIVED').length
	const opened = state.items.find((item) => item.id === openedId)

	return (
		<DndContext
			sensors={sensors}
			onDragStart={(event) => setDragging(state.items.find((item) => item.id === event.active.id))}
			onDragCancel={() => setDragging(null)}
			onDragEnd={handleDragEnd}
		>
			<div className="board">
				{notice && <p className="board__alert">{notice}</p>}

				{waiting > 0 && (
					<p className="board__waiting">
						접수 탭에 {waiting}건이 기다리고 있습니다. 담당자를 지정하면 여기로 올라옵니다.
					</p>
				)}

				<div className="board__columns board__columns--two">
					{BOARD_COLUMNS.map((status) => (
						<Column
							key={status}
							status={status}
							// 서버가 정렬해 내려준 순서를 그대로 쓴다. 우선 처리 요청 항목이 최상단에 오는 것도
							// 서버가 정한다(기획 4-5). 화면에서 다시 정렬하지 않는다.
							items={state.items.filter((item) => item.status === status)}
							assignees={assignees}
							onOpen={setOpenedId}
						/>
					))}
				</div>

				{/* 반영 불가는 열을 차지하지 않고 보드 하단에 접은 상태로 둔다. 기획 6-3 */}
				<RejectedArea items={rejected} assignees={assignees} onOpen={setOpenedId} />
			</div>

			{/* 끌고 있는 동안 손에 들린 카드. 원래 자리의 카드는 흐려진다. */}
			<DragOverlay dropAnimation={null}>
				{dragging && (
					<div className="card-held">
						<FeedbackCard item={dragging} assignee={assignees[dragging.id]} />
					</div>
				)}
			</DragOverlay>

			{opened && (
				<DetailPanel
					item={opened}
					assignee={assignees[opened.id]}
					onChange={(changes) => commitChange(opened.id, changes)}
					onClose={() => setOpenedId(null)}
				/>
			)}
		</DndContext>
	)
}

/** 칸반의 한 열. 카드를 받아 준다. */
function Column({ status, items, assignees, onOpen }) {
	const { setNodeRef, isOver } = useDroppable({ id: status })

	return (
		<section ref={setNodeRef} className={isOver ? 'board__column is-over' : 'board__column'}>
			<header className="board__column-head">
				<StatusBadge status={status} />
				<span className="board__count">{items.length}</span>
			</header>

			<div className="board__cards">
				{items.map((item) => (
					<DraggableCard
						key={item.id}
						item={item}
						assignee={assignees[item.id]}
						onOpen={() => onOpen(item.id)}
					/>
				))}
				{items.length === 0 && <p className="board__empty">여기로 끌어다 놓으세요</p>}
			</div>
		</section>
	)
}

/** 반영 불가. 열이 아니지만 여기로도 끌어다 놓을 수 있다. */
function RejectedArea({ items, assignees, onOpen }) {
	const { setNodeRef, isOver } = useDroppable({ id: 'REJECTED' })

	return (
		<details ref={setNodeRef} className={isOver ? 'board__rejected is-over' : 'board__rejected'}>
			<summary>
				{labelOf(FEEDBACK_STATUS, 'REJECTED')}
				<span className="board__count">{items.length}</span>
			</summary>
			<div className="board__cards board__cards--row">
				{items.map((item) => (
					<DraggableCard
						key={item.id}
						item={item}
						assignee={assignees[item.id]}
						onOpen={() => onOpen(item.id)}
					/>
				))}
				{items.length === 0 && <p className="board__empty">항목이 없습니다</p>}
			</div>
		</details>
	)
}

/** 끌 수 있는 카드. 끄는 동안 원래 자리는 흐려지고 실제 모습은 DragOverlay 가 그린다. */
function DraggableCard({ item, assignee, onOpen }) {
	const { attributes, listeners, setNodeRef, isDragging } = useDraggable({ id: item.id })

	return (
		<div
			ref={setNodeRef}
			className={isDragging ? 'card-slot is-dragging' : 'card-slot'}
			{...listeners}
			{...attributes}
		>
			{/* 6px 넘게 움직여야 끌기로 치므로, 살짝 눌렀다 떼면 여기로 와 패널이 열린다. */}
			<FeedbackCard item={item} assignee={assignee} onOpen={onOpen} />
		</div>
	)
}
