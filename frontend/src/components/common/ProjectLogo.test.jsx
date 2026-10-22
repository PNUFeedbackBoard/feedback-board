import { render } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import ProjectLogo from './ProjectLogo.jsx'

const PROJECTS = [
	'codeplace',
	'aipms',
	'aicap',
	'aicms',
	'srvadm',
]

describe('ProjectLogo', () => {
	it.each(PROJECTS)('%s 프로젝트에 지정된 단색 아이콘을 사용한다', (code) => {
		const { container } = render(<ProjectLogo projectCode={code} projectName={code} />)
		const icon = container.querySelector('svg')

		expect(icon).toHaveAttribute('data-project-logo', code)
		expect(container.querySelector('img')).not.toBeInTheDocument()
	})

	it('등록되지 않은 프로젝트는 이름의 첫 글자를 표시한다', () => {
		const { container } = render(<ProjectLogo projectCode="future" projectName="새 서비스" />)

		expect(container.querySelector('img')).not.toBeInTheDocument()
		expect(container.querySelector('.project-logo')).toHaveTextContent('새')
	})
})
