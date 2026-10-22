import { render } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import ProjectLogo from './ProjectLogo.jsx'

const PROJECTS = [
	['codeplace', 'codeplace.png'],
	['aipms', 'aipms.png'],
	['aicap', 'aicap.png'],
	['aicms', 'aicms.png'],
	['srvadm', 'srvadm.png'],
]

describe('ProjectLogo', () => {
	it.each(PROJECTS)('%s 프로젝트에 지정된 아이콘을 사용한다', (code, filename) => {
		const { container } = render(<ProjectLogo projectCode={code} projectName={code} />)
		const image = container.querySelector('img')

		expect(image?.src).toContain(filename)
	})

	it('등록되지 않은 프로젝트는 이름의 첫 글자를 표시한다', () => {
		const { container } = render(<ProjectLogo projectCode="future" projectName="새 서비스" />)

		expect(container.querySelector('img')).not.toBeInTheDocument()
		expect(container.querySelector('.project-logo')).toHaveTextContent('새')
	})
})
