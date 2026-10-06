import './pagination.css'

export default function Pagination({ page, size, totalCount, onChange }) {
	const totalPages = Math.max(1, Math.ceil(totalCount / size))
	if (totalCount <= size) return null

	return (
		<nav className="pagination" aria-label="목록 페이지">
			<button type="button" disabled={page === 0} onClick={() => onChange(page - 1)}>
				이전
			</button>
			<span>
				<strong>{page + 1}</strong> / {totalPages}
			</span>
			<button
				type="button"
				disabled={page + 1 >= totalPages}
				onClick={() => onChange(page + 1)}
			>
				다음
			</button>
		</nav>
	)
}
