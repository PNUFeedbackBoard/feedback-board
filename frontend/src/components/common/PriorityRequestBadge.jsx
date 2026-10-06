import { Star } from 'lucide-react'
import './feedback-badges.css'

/** 디자인 Card/Badge/Important. 우선 처리 요청된 항목에만 붙이는 보라색 별. */
export default function PriorityRequestBadge() {
  return <span className="feedback-priority-request" role="img" aria-label="우선 처리 요청" title="우선 처리 요청"><Star aria-hidden="true" /></span>
}
