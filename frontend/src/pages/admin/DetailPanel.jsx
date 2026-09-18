import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { X } from 'lucide-react'
import { getAdminFeedback } from '../../api/endpoints.js'
import {
	AUTHOR_TYPE,
	FEEDBACK_CATEGORY,
	FEEDBACK_STATUS,
	PRIORITY,
	labelOf,
	toOptions,
} from '../../constants/enums.js'
import PriorityRequestBadge from './components/PriorityRequestBadge.jsx'
import './detail-panel.css'

/**
 * 카드 상세 패널. 기획 6-3
 *
 * 기획이 요구하는 네 가지를 담는다 — 전체 내용 확인, 유형 변경, 중요도 변경, 답변 화면 이동.
 * 상태 변경도 여기서 할 수 있다. 끌어다 놓는 것 말고 다른 길을 하나 더 두라는 것이 기획의 뜻이다.
 *
 * 목록 응답에는 본문이 없어(AdminFeedbackSummary 에 content 가 없다) 열 때 상세를 따로 받는다.
 *
 * TODO(C, 3단계): 원래 3단계 작업이다. 상태 변경 수단을 둘로 두기 위해 당겨서 만들었다.
 */
export default function DetailPanel({ item, assignee, onChange, onClose }) {
	const { projectCode } = useParams()
	const navigate = useNavigate()
	// 어느 건의 상세인지 함께 담아 둔다. 그래야 다른 카드를 열었을 때
	// effect 안에서 상태를 되돌리지 않고도 이전 응답을 화면에서 걸러 낼 수 있다.
	const [detail, setDetail] = useState({ id: null, content: '' })

	useEffect(() => {
		let cancelled = false

		getAdminFeedback(item.id)
			.then((found) => !cancelled && setDetail({ id: item.id, content: found.content }))
			.catch(() => !cancelled && setDetail({ id: item.id, content: '내용을 불러오지 못했습니다.' }))

		return () => {
			cancelled = true
		}
	}, [item.id])

	// 패널이 열려 있는 동안 Esc 로 닫는다.
	useEffect(() => {
		function onKeyDown(event) {
			if (event.key === 'Escape') onClose()
		}
		document.addEventListener('keydown', onKeyDown)
		return () => document.removeEventListener('keydown', onKeyDown)
	}, [onClose])

	return (
		<aside className="panel" aria-label="피드백 상세">
			<header className="panel__head">
				<h2 className="panel__title">{item.title}</h2>
				<button type="button" className="panel__close" onClick={onClose} aria-label="닫기">
					<X size={18} strokeWidth={2} />
				</button>
			</header>

			{item.priorityRequested && (
				<div>
					<PriorityRequestBadge />
				</div>
			)}

			<dl className="panel__facts">
				<div>
					<dt>등록</dt>
					<dd>{formatDay(item.createdAt)}</dd>
				</div>
				<div>
					<dt>작성자</dt>
					<dd>{item.authorName ?? labelOf(AUTHOR_TYPE, item.authorType)}</dd>
				</div>
				<div>
					<dt>담당</dt>
					<dd>{assignee || '미지정'}</dd>
				</div>
				<div>
					<dt>답변</dt>
					<dd>{item.answered ? '완료' : '없음'}</dd>
				</div>
			</dl>

			{/* 전체 내용. 받아 오는 중에는 자리만 비워 둔다. */}
			<section className="panel__content">
				{detail.id === item.id ? (
					detail.content
				) : (
					<span className="panel__loading">내용을 불러오는 중…</span>
				)}
			</section>

			<div className="panel__fields">
				<Field
					label="상태"
					value={item.status}
					options={toOptions(FEEDBACK_STATUS)}
					onChange={(status) => onChange({ status })}
				/>
				<Field
					label="유형"
					value={item.category}
					options={toOptions(FEEDBACK_CATEGORY)}
					onChange={(category) => onChange({ category })}
				/>
				<Field
					label="중요도"
					value={item.priority}
					options={toOptions(PRIORITY)}
					onChange={(priority) => onChange({ priority })}
				/>
			</div>

			<button
				type="button"
				className="panel__link"
				onClick={() => navigate(`/admin/${projectCode}/answers`)}
			>
				답변 화면으로 이동
			</button>
		</aside>
	)
}

function Field({ label, value, options, onChange }) {
	return (
		<label className="panel__field">
			<span className="panel__field-label">{label}</span>
			<select
				className="panel__select"
				value={value}
				onChange={(event) => onChange(event.target.value)}
			>
				{options.map((option) => (
					<option key={option.value} value={option.value}>
						{option.label}
					</option>
				))}
			</select>
		</label>
	)
}

/** 'YYYY-MM-DDTHH:mm:ss' 에서 날짜만 떼어 점으로 잇는다. */
function formatDay(createdAt) {
	return createdAt.slice(0, 10).replaceAll('-', '. ')
}
