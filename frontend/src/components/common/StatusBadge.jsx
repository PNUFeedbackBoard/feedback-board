import { FEEDBACK_STATUS, labelOf } from '../../constants/enums.js'
import './feedback-badges.css'

/** 디자인 Card/Badge/Process. 영문 상태 코드와 기존 props 계약을 유지한다. */
export default function StatusBadge({ status }) {
  return <span className="feedback-badge feedback-badge--status" data-status={status}>{labelOf(FEEDBACK_STATUS, status)}</span>
}
