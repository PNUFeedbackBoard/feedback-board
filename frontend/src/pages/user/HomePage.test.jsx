import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getMe, getMyFeedbacks } from '../../api/endpoints.js'
import HomePage from './HomePage.jsx'

vi.mock('../../api/endpoints.js', () => ({
  getMe: vi.fn(),
  getMyFeedbacks: vi.fn(),
  logout: vi.fn(),
}))

const feedbacks = [
  {
    id: 1,
    projectCode: 'codeplace',
    projectName: '코드플레이스',
    title: '코드 실행이 멈춰요',
    category: 'BUG',
    status: 'RECEIVED',
    createdAt: '2026-10-20T10:00:00',
    answered: false,
    reportedPriority: 'HIGH',
  },
  {
    id: 2,
    projectCode: 'codeplace',
    projectName: '코드플레이스',
    title: '글꼴 크기를 바꾸고 싶어요',
    category: 'FEATURE',
    status: 'DONE',
    createdAt: '2026-10-18T10:00:00',
    answered: true,
    reportedPriority: 'LOW',
  },
  {
    id: 3,
    projectCode: 'aipms',
    projectName: 'AIPMS',
    title: '파일 업로드가 안 돼요',
    category: 'BUG',
    status: 'IN_PROGRESS',
    createdAt: '2026-10-19T10:00:00',
    answered: false,
    reportedPriority: 'NORMAL',
  },
]

describe('HomePage', () => {
  beforeEach(() => {
    getMe.mockResolvedValue({ id: 7, name: '테스트 사용자', email: 'user@example.com' })
    getMyFeedbacks.mockResolvedValue(feedbacks)
  })

  it('문의가 있는 사이트만 책갈피 탭으로 만든다', async () => {
    renderHome()

    const tablist = await screen.findByRole('tablist', { name: '문의 사이트 선택' })
    expect(within(tablist).getAllByRole('tab')).toHaveLength(3)
    expect(within(tablist).getByRole('tab', { name: /전체/ })).toBeInTheDocument()
    expect(within(tablist).getByRole('tab', { name: /코드플레이스/ })).toBeInTheDocument()
    expect(within(tablist).getByRole('tab', { name: /AIPMS/ })).toBeInTheDocument()
    expect(within(tablist).queryByRole('tab', { name: /AICMS/ })).not.toBeInTheDocument()
  })

  it('책갈피를 누르면 해당 사이트의 문의만 보여 준다', async () => {
    const user = userEvent.setup()
    renderHome()

    await user.click(await screen.findByRole('tab', { name: /AIPMS/ }))

    expect(screen.getByText('파일 업로드가 안 돼요')).toBeInTheDocument()
    expect(screen.queryByText('코드 실행이 멈춰요')).not.toBeInTheDocument()
    expect(screen.getByText((_, element) => (
      element.classList.contains('feedback-filterbar__count')
      && element.textContent === '1개의 문의'
    ))).toBeInTheDocument()
  })

  it('문의가 없으면 사이트 책갈피 없이 안내만 보여 준다', async () => {
    getMyFeedbacks.mockResolvedValue([])
    renderHome()

    expect(await screen.findByText('아직 남긴 문의가 없어요.')).toBeInTheDocument()
    expect(screen.queryByRole('tablist')).not.toBeInTheDocument()
  })

  it('문의가 많으면 처음에는 일부만 그리고 요청할 때 다음 묶음을 표시한다', async () => {
    const user = userEvent.setup()
    getMyFeedbacks.mockResolvedValue(Array.from({ length: 15 }, (_, index) => ({
      ...feedbacks[0],
      id: index + 1,
      title: `대량 문의 ${index + 1}`,
      createdAt: `2026-10-${String(index + 1).padStart(2, '0')}T10:00:00`,
    })))
    renderHome()

    const list = await screen.findByRole('list')
    expect(within(list).getAllByRole('listitem')).toHaveLength(12)

    await user.click(screen.getByRole('button', { name: /더 보기/ }))

    expect(within(list).getAllByRole('listitem')).toHaveLength(15)
    expect(screen.queryByRole('button', { name: /더 보기/ })).not.toBeInTheDocument()
  })
})

function renderHome() {
  return render(
    <MemoryRouter>
      <HomePage />
    </MemoryRouter>,
  )
}
