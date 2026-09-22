import { PRIORITY, labelOf } from '../../constants/enums.js'
import '../../styles/tokens.css'
import './priority-chip.css'

const PRIORITY_CODES = Object.keys(PRIORITY)

/**
 * 현재 중요도를 세 단계 안에서 표시한다.
 * props 이름과 영문 enum 값은 관리용 화면과 공유하는 계약이다.
 *
 * @param {{ priority: 'HIGH' | 'NORMAL' | 'LOW' }} props
 */
export default function PriorityChip({ priority }) {
  return (
    <span
      className="priority-chip"
      data-priority={priority}
      role="img"
      aria-label={`중요도 ${labelOf(PRIORITY, priority)}`}
    >
      {PRIORITY_CODES.map((code) => (
        <span
          key={code}
          className="priority-chip__option"
          data-level={code}
          data-selected={code === priority}
          aria-hidden="true"
        >
          {labelOf(PRIORITY, code)}
        </span>
      ))}
    </span>
  )
}
