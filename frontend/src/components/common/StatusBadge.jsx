import { FEEDBACK_STATUS, labelOf } from '../../constants/enums.js'
import './status-badge.css'

/**
 * 진행 상태 배지 4종(기획 4-2). 접수(파랑) · 처리 중(주황) · 처리 완료(초록) · 반영 불가(회색).
 *
 * 관리용 화면(`pages/admin/components/badges.css`)에 같은 모양의 임시 버전이 먼저 있었다.
 * 0-2단계 산출물이 이제 생겼으니, 이후 그쪽을 이 컴포넌트로 바꿔 끼우면 된다(기획 13-4).
 *
 * @param {{ status: 'RECEIVED' | 'IN_PROGRESS' | 'DONE' | 'REJECTED' }} props
 */
export default function StatusBadge({ status }) {
	return (
		<span className="status-badge" data-status={status}>
			{labelOf(FEEDBACK_STATUS, status)}
		</span>
	)
}
