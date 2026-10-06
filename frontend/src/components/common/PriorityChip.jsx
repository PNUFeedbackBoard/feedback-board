import { PRIORITY, labelOf } from '../../constants/enums.js'
import './feedback-badges.css'

/** 디자인 Card/Badge/Urgent. 현재 중요도 한 가지만 표시한다. */
export default function PriorityChip({ priority }) {
  return <span className="feedback-badge feedback-badge--priority" data-priority={priority}>{labelOf(PRIORITY, priority)}</span>
}
