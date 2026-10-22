import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it } from 'vitest'
import ProjectDisc from './ProjectDisc.jsx'

const STORAGE_KEY = 'feedback-board:project-disc-onboarding:v1'

describe('ProjectDisc onboarding', () => {
	beforeEach(() => {
		window.localStorage.clear()
	})

	it('첫 관리자 방문에만 원판 사용법을 안내한다', async () => {
		const user = userEvent.setup()
		const firstVisit = renderDisc()

		expect(await screen.findByLabelText('프로젝트 메뉴 안내')).toBeInTheDocument()
		expect(window.localStorage.getItem(STORAGE_KEY)).toBe('shown')
		await user.click(screen.getByRole('button', { name: '알겠어요' }))
		expect(screen.queryByLabelText('프로젝트 메뉴 안내')).not.toBeInTheDocument()

		firstVisit.unmount()
		renderDisc()
		expect(screen.queryByLabelText('프로젝트 메뉴 안내')).not.toBeInTheDocument()
	})

	it('접힌 원판 위에 반투명 위쪽 화살표를 표시한다', () => {
		window.localStorage.setItem(STORAGE_KEY, 'shown')
		const { container } = renderDisc()

		expect(container.querySelector('.disc__idle-arrow')).toBeInTheDocument()
		expect(container.querySelector('.disc')).not.toHaveClass('is-open')
	})

	it('현재 프로젝트 슬롯을 선택 상태로 표시해 아이콘 색상 변경 대상을 구분한다', () => {
		window.localStorage.setItem(STORAGE_KEY, 'shown')
		const { container } = renderDisc(
			[{ code: 'aicap', name: 'AI역량지원시스템' }],
			'aicap',
		)
		const selectedSlot = screen.getByRole('button', { name: 'AI역량지원시스템' })

		expect(selectedSlot).toHaveClass('is-selected')
		expect(selectedSlot).toHaveAttribute('aria-current', 'page')
		expect(container.querySelector('[data-project-logo="aicap"]')).toBeInTheDocument()
	})
})

function renderDisc(projects = [], projectCode = null) {
	return render(
		<MemoryRouter>
			<ProjectDisc projects={projects} projectCode={projectCode} />
		</MemoryRouter>,
	)
}
