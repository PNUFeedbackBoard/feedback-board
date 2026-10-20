import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getAdminFeedbacks, hideAdminFeedback } from '../../api/endpoints.js'
import AnswersPage from './AnswersPage.jsx'

vi.mock('../../api/endpoints.js', () => ({
  getAdminFeedback: vi.fn(),
  getAdminFeedbacks: vi.fn(),
  hideAdminFeedback: vi.fn(),
  togglePriorityRequest: vi.fn(),
  updateAdminFeedback: vi.fn(),
}))

const feedback = {
  id: 47,
  version: 3,
  title: '등록한 공지 게시글이 목록에 보이지 않습니다',
  authorType: 'MEMBER',
  authorName: '정도현',
  createdAt: '2026-10-19T16:52:00',
  answered: false,
  category: 'BUG',
  priority: 'HIGH',
  status: 'RECEIVED',
  priorityRequested: false,
  assigneeId: null,
  assigneeName: null,
}

describe('AnswersPage feedback hiding', () => {
  beforeEach(() => {
    getAdminFeedbacks.mockResolvedValue({ items: [feedback], totalCount: 1 })
    hideAdminFeedback.mockResolvedValue(null)
  })

  it('개발자가 사용자 원문을 지우지 않고 관리자 목록에서 피드백을 숨긴다', async () => {
    const user = userEvent.setup()
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    renderPage('DEVELOPER')

    expect(await screen.findByText(feedback.title)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: '숨기기' }))

    await waitFor(() => expect(hideAdminFeedback).toHaveBeenCalledWith(47, 3))
    expect(screen.queryByText(feedback.title)).not.toBeInTheDocument()
    expect(screen.getByText('피드백을 관리자 목록에서 숨겼습니다.')).toBeInTheDocument()
  })

  it('열람자에게는 숨김 조작을 노출하지 않는다', async () => {
    renderPage('VIEWER')

    expect(await screen.findByText(feedback.title)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: '숨기기' })).not.toBeInTheDocument()
  })
})

function renderPage(role) {
  return render(
    <MemoryRouter initialEntries={['/admin/aicms/answers']}>
      <Routes>
        <Route path="/admin/:projectCode/answers" element={<Outlet context={{ me: { role } }} />}>
          <Route index element={<AnswersPage />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  )
}
