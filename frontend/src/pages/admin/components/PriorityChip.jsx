import { PRIORITY, labelOf } from '../../../constants/enums.js'
import './badges.css'

/**
 * 중요도 칩 3종. 기획 4-3
 *
 * **임시 구현이다.** D 의 components/common/PriorityChip 이 올라오면 지운다.
 * props 이름(`priority`)은 계약이다.
 *
 * @param {{ priority: 'HIGH' | 'NORMAL' | 'LOW' }} props
 */
export default function PriorityChip({ priority }) {
	return (
		<span className="badge badge--priority" data-priority={priority}>
			{labelOf(PRIORITY, priority)}
		</span>
	)
}
