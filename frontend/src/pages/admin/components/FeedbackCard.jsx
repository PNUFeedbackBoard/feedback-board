import { AUTHOR_TYPE, labelOf } from '../../../constants/enums.js'
import CategoryTag from './CategoryTag.jsx'
import PriorityChip from './PriorityChip.jsx'
import PriorityRequestBadge from './PriorityRequestBadge.jsx'

/**
 * 피드백 카드. 접수 · 개발 보드 · 답변 세 화면이 함께 쓴다.
 *
 * 표시 항목은 기획 6-3 이 정한 7가지다.
 * 우선 처리 요청 배지 · 중요도 · 제목 · 유형 · 경과일 · 회원 여부 · 답변 여부
 * 담당자는 그 위에 얹은 항목이며 지정된 카드에만 나온다.
 *
 * @param {object} props
 * @param {object} props.item AdminFeedbackSummary
 * @param {string} [props.assignee] 담당자 이름. undefined 면 줄 자체가 나오지 않는다
 * @param {boolean} [props.dimmed] 비회원처럼 손댈 수 없는 항목을 흐리게 표시한다
 * @param {import('react').ReactNode} [props.action] 카드 아래에 붙는 버튼
 * @param {boolean} [props.selected] 체크 상태. onSelect 를 함께 주어야 체크칸이 나온다
 * @param {(next: boolean) => void} [props.onSelect] 체크칸을 누를 때 호출된다
 * @param {() => void} [props.onOpen] 카드를 누를 때 호출된다. 상세 패널을 여는 데 쓴다
 */
export default function FeedbackCard({
	item,
	assignee,
	dimmed = false,
	action,
	selected,
	onSelect,
	onOpen,
}) {
	return (
		<article
			className={cardClassName(dimmed, selected)}
			data-priority-requested={item.priorityRequested}
			// 열 수 있는 카드만 눌리게 한다. onOpen 이 없으면 그냥 보는 카드다.
			onClick={onOpen}
		>
			{onSelect && (
				// 카드를 누르면 패널이 열리는 자리도 있으므로 체크칸까지 번지지 않게 막는다.
				<label className="card__check" onClick={(event) => event.stopPropagation()}>
					<input
						type="checkbox"
						checked={selected ?? false}
						onChange={(event) => onSelect(event.target.checked)}
					/>
					<span className="card__check-label">선택</span>
				</label>
			)}

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

			{/* 선택 박스 같은 조작은 카드를 여는 동작과 겹치지 않게 막는다. */}
			{action && <div onClick={(event) => event.stopPropagation()}>{action}</div>}
		</article>
	)
}

function cardClassName(dimmed, selected) {
	const names = ['card']
	if (dimmed) names.push('is-dimmed')
	if (selected) names.push('is-picked')
	return names.join(' ')
}

/**
 * 등록일로부터 며칠 지났는지. 기획 6-3 의 "경과일" 이다.
 * createdAt 은 시간대 없는 LocalDateTime 문자열이라 브라우저 현지 시각으로 해석된다.
 */
function daysSince(createdAt) {
	const MS_PER_DAY = 24 * 60 * 60 * 1000
	return Math.max(0, Math.floor((Date.now() - new Date(createdAt).getTime()) / MS_PER_DAY))
}
