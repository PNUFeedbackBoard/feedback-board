import {
	AUTHOR_TYPE,
	FEEDBACK_CATEGORY,
	FEEDBACK_SORT,
	FEEDBACK_STATUS,
	toOptions,
} from '../../../constants/enums.js'
import './filter-bar.css'

/** 필터를 하나도 걸지 않은 처음 상태. 정렬만 최신순으로 둔다. 목록 화면의 처음 값과 같아야 한다. */
const DEFAULT_LIST_FILTERS = { sort: 'LATEST' }

/**
 * 목록 화면의 필터와 정렬.
 *
 * 접수·처리 중·처리 완료를 한 화면에 모아 보고, 여기서 목적에 맞게 좁힌다.
 * 키 이름은 getAdminFeedbacks 의 파라미터와 같고 모든 조건은 서버에서 적용한다.
 * 중요도는 거름이 아니라 정렬(중요도순)로 본다.
 *
 * @param {{ value: object, onChange: (next: object) => void }} props
 */
export default function ListFilters({ value, onChange }) {
	function set(key, next) {
		onChange({ ...value, [key]: next || undefined })
	}

	const isDefault =
		!value.status &&
		!value.priorityRequested &&
		!value.answered &&
		!value.category &&
		!value.authorType &&
		!value.from &&
		!value.to &&
		value.sort === DEFAULT_LIST_FILTERS.sort

	return (
		<div className="filters">
			<Select label="진행 상태" value={value.status} onChange={(next) => set('status', next)}>
				{toOptions(FEEDBACK_STATUS)}
			</Select>

			<Select
				label="우선 처리 요청"
				value={value.priorityRequested}
				onChange={(next) => set('priorityRequested', next)}
				allLabel="전체"
			>
				{[{ value: 'true', label: '요청된 것만' }]}
			</Select>

			<Select label="답변" value={value.answered} onChange={(next) => set('answered', next)}>
				{[
					{ value: 'true', label: '답변 있음' },
					{ value: 'false', label: '답변 없음' },
				]}
			</Select>

			<Select label="유형" value={value.category} onChange={(next) => set('category', next)}>
				{toOptions(FEEDBACK_CATEGORY)}
			</Select>

			<Select label="작성자" value={value.authorType} onChange={(next) => set('authorType', next)}>
				{toOptions(AUTHOR_TYPE)}
			</Select>

			<label className="filters__field">
				<span className="filters__label">기간</span>
				<span className="filters__range">
					<input
						type="date"
						className="filters__control"
						value={value.from ?? ''}
						max={value.to ?? undefined}
						onChange={(event) => set('from', event.target.value)}
					/>
					<span className="filters__tilde">—</span>
					<input
						type="date"
						className="filters__control"
						value={value.to ?? ''}
						min={value.from ?? undefined}
						onChange={(event) => set('to', event.target.value)}
					/>
				</span>
			</label>

			<label className="filters__field">
				<span className="filters__label">정렬</span>
				<select
					className="filters__control"
					value={value.sort ?? DEFAULT_LIST_FILTERS.sort}
					onChange={(event) => set('sort', event.target.value)}
				>
					{toOptions(FEEDBACK_SORT).map((option) => (
						<option key={option.value} value={option.value}>
							{option.label}
						</option>
					))}
				</select>
			</label>

			{!isDefault && (
				<button type="button" className="filters__reset" onClick={() => onChange(DEFAULT_LIST_FILTERS)}>
					초기화
				</button>
			)}
		</div>
	)
}

/** 맨 앞에 "전체"를 둔 선택 상자. 전체를 고르면 값이 비어 그 조건이 빠진다. */
function Select({ label, value, onChange, children, allLabel = '전체' }) {
	return (
		<label className="filters__field">
			<span className="filters__label">{label}</span>
			<select
				className="filters__control"
				value={value ?? ''}
				onChange={(event) => onChange(event.target.value)}
			>
				<option value="">{allLabel}</option>
				{children.map((option) => (
					<option key={option.value} value={option.value}>
						{option.label}
					</option>
				))}
			</select>
		</label>
	)
}
