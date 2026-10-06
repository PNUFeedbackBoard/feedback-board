import { FEEDBACK_CATEGORY, labelOf } from '../../constants/enums.js'
import './feedback-badges.css'

/** 디자인 Card/Badge/Sort. */
export default function CategoryTag({ category }) {
  return <span className="feedback-badge feedback-badge--category" data-category={category}>{labelOf(FEEDBACK_CATEGORY, category)}</span>
}
