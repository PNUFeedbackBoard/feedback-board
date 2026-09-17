import './badges.css'

/**
 * 우선 처리 요청 표시. 기획 4-4
 *
 * 열람자가 요청한 항목에 붙는다. 어떤 정렬을 골라도 목록 최상단에 고정된다.
 * **임시 구현이다.** D 의 components/common/PriorityRequestBadge 가 올라오면 지운다.
 * 값을 받지 않는 것도 계약이다. (붙어 있으면 요청된 것)
 */
export default function PriorityRequestBadge() {
	return <span className="badge badge--priority-request">우선 처리 요청</span>
}
