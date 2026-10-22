import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createFeedback, getMe, getProjects } from '../../api/endpoints.js'
import WritePage from './WritePage.jsx'

vi.mock('../../api/endpoints.js', () => ({
  createFeedback: vi.fn(),
  getMe: vi.fn(),
  getProjects: vi.fn(),
  logout: vi.fn(),
}))

const projects = [
  { id: 1, code: 'codeplace', name: '코드플레이스' },
  { id: 2, code: 'aipms', name: 'AIPMS' },
]

describe('WritePage', () => {
  beforeEach(() => {
    getProjects.mockResolvedValue(projects)
    getMe.mockResolvedValue({ id: 7, name: '테스트 사용자', email: 'user@example.com' })
    createFeedback.mockResolvedValue({ id: 37 })
  })

  it('연결된 서비스를 유지해 문의를 접수하고 종이 영수증 화면을 보여 준다', async () => {
    const user = userEvent.setup()
    renderWritePage('/write?site=codeplace')

    expect(await screen.findByText('코드플레이스')).toBeInTheDocument()
    await user.type(screen.getByLabelText('제목'), '채점 화면이 멈춥니다')
    await user.type(screen.getByLabelText('상세 내용'), '제출 이후 채점 화면이 십 분 이상 멈춰 있습니다.')
    await user.click(screen.getByRole('button', { name: '피드백 접수' }))

    expect(createFeedback).toHaveBeenCalledWith({
      projectCode: 'codeplace',
      category: 'BUG',
      reportedPriority: 'NORMAL',
      title: '채점 화면이 멈춥니다',
      content: '제출 이후 채점 화면이 십 분 이상 멈춰 있습니다.',
    })
    expect(await screen.findByRole('heading', { name: '피드백이 접수되었습니다' })).toBeInTheDocument()
    expect(screen.getByText('#37')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /내 문의 보기/ })).toHaveAttribute('href', '/home')
  })

  it('비회원 접수 완료 화면에는 내 문의 링크를 노출하지 않는다', async () => {
    const user = userEvent.setup()
    getMe.mockRejectedValue(new Error('로그인되지 않음'))
    renderWritePage()

    await screen.findByRole('option', { name: '코드플레이스' })
    await user.type(screen.getByLabelText('제목'), '새로운 기능을 제안합니다')
    await user.type(screen.getByLabelText('상세 내용'), '목록을 파일로 내려받을 수 있으면 좋겠습니다.')
    await user.click(screen.getByRole('button', { name: '피드백 접수' }))

    expect(await screen.findByText('#37')).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /내 문의 보기/ })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: /처음으로/ })).toHaveAttribute('href', '/')
  })
})

function renderWritePage(path = '/write') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <WritePage />
    </MemoryRouter>,
  )
}
