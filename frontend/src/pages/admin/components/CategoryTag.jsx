import { FEEDBACK_CATEGORY, labelOf } from '../../../constants/enums.js'
import './badges.css'

/**
 * 피드백 유형 태그 3종. 기획 4-1
 *
 * **임시 구현이다.** D 의 components/common/CategoryTag 가 올라오면 지운다.
 * props 이름(`category`)은 계약이다.
 *
 * @param {{ category: 'BUG' | 'FEATURE' | 'ETC' }} props
 */
export default function CategoryTag({ category }) {
	return (
		<span className="badge badge--category" data-category={category}>
			{labelOf(FEEDBACK_CATEGORY, category)}
		</span>
	)
}
