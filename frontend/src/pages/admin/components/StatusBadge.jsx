import { FEEDBACK_STATUS, labelOf } from '../../../constants/enums.js'
import './badges.css'

/**
 * 진행 상태 배지 4종. 기획 4-2
 *
 * **임시 구현이다.** D 가 components/common/StatusBadge 를 올리면 이 파일을 지우고 import 경로만 바꾼다.
 * props 이름(`status`)은 docs/stage0-2-design.md 의 계약이므로 바꾸지 않는다.
 *
 * @param {{ status: 'RECEIVED' | 'IN_PROGRESS' | 'DONE' | 'REJECTED' }} props
 */
export default function StatusBadge({ status }) {
	return (
		<span className="badge badge--status" data-status={status}>
			{labelOf(FEEDBACK_STATUS, status)}
		</span>
	)
}
