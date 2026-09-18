import { AUTHOR_TYPE, FEEDBACK_CATEGORY, FEEDBACK_SORT, toOptions } from '../../../constants/enums.js'
import './filter-bar.css'

/**
 * 접수 목록의 필터와 정렬. 기획 4-5 와 6-3
 *
 * 기획의 필터 항목은 사이트·진행 상태·유형·기간·회원 여부다.
 * 사이트는 하단 디스크가, 진행 상태는 탭 자체가 정하므로 여기서는 나머지 셋과 정렬을 다룬다.
 *
 * 값은 그대로 getAdminFeedbacks 의 파라미터 이름과 같다. 화면에서 이름을 바꾸지 않는다.
 *
 * @param {{ value: object, onChange: (next: object) => void }} props
 */
export default function FilterBar({ value, onChange }) {
	function set(key, next) {
		onChange({ ...value, [key]: next || undefined })
	}

	const isDefault =
		!value.category && !value.authorType && !value.from && !value.to && value.sort === 'PRIORITY'

	return (
		<div className="filters">
			<label className="filters__field">
				<span className="filters__label">정렬</span>
				<select
					className="filters__control"
					value={value.sort ?? 'PRIORITY'}
					onChange={(event) => set('sort', event.target.value)}
				>
					{toOptions(FEEDBACK_SORT).map((option) => (
						<option key={option.value} value={option.value}>
							{option.label}
						</option>
					))}
				</select>
			</label>

			<label className="filters__field">
				<span className="filters__label">유형</span>
				<select
					className="filters__control"
					value={value.category ?? ''}
					onChange={(event) => set('category', event.target.value)}
				>
					<option value="">전체</option>
					{toOptions(FEEDBACK_CATEGORY).map((option) => (
						<option key={option.value} value={option.value}>
							{option.label}
						</option>
					))}
				</select>
			</label>

			<label className="filters__field">
				<span className="filters__label">작성자</span>
				<select
					className="filters__control"
					value={value.authorType ?? ''}
					onChange={(event) => set('authorType', event.target.value)}
				>
					<option value="">전체</option>
					{toOptions(AUTHOR_TYPE).map((option) => (
						<option key={option.value} value={option.value}>
							{option.label}
						</option>
					))}
				</select>
			</label>

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

			{!isDefault && (
				<button
					type="button"
					className="filters__reset"
					onClick={() => onChange({ sort: 'PRIORITY' })}
				>
					초기화
				</button>
			)}
		</div>
	)
}
