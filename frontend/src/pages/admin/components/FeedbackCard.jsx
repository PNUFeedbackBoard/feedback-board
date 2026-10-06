import { UserRound } from 'lucide-react'
import { AUTHOR_TYPE, labelOf } from '../../../constants/enums.js'
import CategoryTag from '../../../components/common/CategoryTag.jsx'
import PriorityChip from '../../../components/common/PriorityChip.jsx'
import PriorityRequestBadge from '../../../components/common/PriorityRequestBadge.jsx'
import StatusBadge from '../../../components/common/StatusBadge.jsx'
import './feedback-card.css'

/** 제공된 카드 시안. 선택·상세·담당자·화면별 action 계약은 유지한다. */
export default function FeedbackCard({ item, assignee, dimmed = false, action, selected, onSelect, onOpen }) {
  const names = ['card', dimmed && 'is-dimmed', selected && 'is-picked'].filter(Boolean).join(' ')
  return (
    <article className={names} data-priority-requested={item.priorityRequested} onClick={onOpen}>
      <header className="card__head">
        <div className="card__tags">
          <CategoryTag category={item.category} />
          <PriorityChip priority={item.priority} />
          <StatusBadge status={item.status} />
          {item.priorityRequested && <PriorityRequestBadge />}
        </div>
        {onSelect && (
          <label className="card__check" onClick={(event) => event.stopPropagation()}>
            <input type="checkbox" aria-label={`${item.title} 선택`} checked={selected ?? false} onChange={(event) => onSelect(event.target.checked)} />
          </label>
        )}
      </header>
      <h3 className="card__title">
        {onOpen ? <button type="button" className="card__open" onClick={(event) => { event.stopPropagation(); onOpen() }}>{item.title}</button> : item.title}
      </h3>
      <footer className="card__footer">
        <div className="card__information">
          <p className="card__author"><UserRound aria-hidden="true" /><span>{item.authorType === 'GUEST' ? labelOf(AUTHOR_TYPE, 'GUEST') : item.authorName || labelOf(AUTHOR_TYPE, item.authorType)}</span></p>
          <p className="card__meta"><time dateTime={item.createdAt}>{formatCreatedAt(item.createdAt)}</time><span>, <strong>{daysSince(item.createdAt)}일 경과</strong></span></p>
          <p className="card__details">{labelOf(AUTHOR_TYPE, item.authorType)} · {item.answered ? '답변 완료' : '답변 없음'}</p>
          {assignee !== undefined && <p className="card__assignee">{assignee ? `담당 ${assignee}` : '담당 미지정'}</p>}
        </div>
        {action && <div className="card__action" onClick={(event) => event.stopPropagation()}>{action}</div>}
      </footer>
    </article>
  )
}

function formatCreatedAt(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '등록일 미상'
  const pad = (part) => String(part).padStart(2, '0')
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}  ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function daysSince(createdAt) {
  const elapsed = Date.now() - new Date(createdAt).getTime()
  return Number.isNaN(elapsed) ? 0 : Math.max(0, Math.floor(elapsed / 86400000))
}
