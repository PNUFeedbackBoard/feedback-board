import '../../styles/tokens.css'
import priorityRequestBadge from './assets/priority-request-badge.png'
import './priority-request-badge.css'

/**
 * 열람자가 우선 처리를 요청한 항목에 붙이는 이미지 배지다.
 * 배지가 있으면 요청된 항목이므로 별도의 prop을 받지 않는다.
 */
export default function PriorityRequestBadge() {
  return (
    <span
      className="priority-request-badge"
      role="img"
      aria-label="우선 처리 요청"
      title="우선 처리 요청"
    >
      <img className="priority-request-badge__image" src={priorityRequestBadge} alt="" />
    </span>
  )
}
