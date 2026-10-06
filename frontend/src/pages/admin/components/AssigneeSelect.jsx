import './assignee-select.css'

/**
 * 담당자 선택 드롭다운. 카드 안에서 담당자를 바로 지정하거나 바꿀 때 쓴다.
 *
 * 고른 값은 계정 id 이거나, 비우면 null 이다. 저장 방식(assigneeId / unassign)은 쓰는 쪽이 정한다.
 *
 * @param {{
 *   value: number | null | undefined,
 *   currentName?: string | null,
 *   developers: Array<{ id: number, name: string }>,
 *   onChange: (next: number | null) => void,
 * }} props currentName 은 지금 담당자 이름. 목록에 없는 계정(비활성 등)이어도 선택값이 비지 않게 한다.
 */
export default function AssigneeSelect({ value, currentName, developers, onChange }) {
	const listed = developers.some((developer) => developer.id === value)

	return (
		<label className="assignee-select">
			<span className="assignee-select__label">담당</span>
			<select
				className="assignee-select__control"
				value={value ?? ''}
				onChange={(event) => onChange(event.target.value === '' ? null : Number(event.target.value))}
			>
				<option value="">담당자 미지정</option>
				{value != null && !listed && <option value={value}>{currentName ?? `#${value}`}</option>}
				{developers.map((developer) => (
					<option key={developer.id} value={developer.id}>
						{developer.name}
					</option>
				))}
			</select>
		</label>
	)
}
