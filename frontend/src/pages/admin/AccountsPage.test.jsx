import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getAdminUsers, getMe, updateAdminUser } from '../../api/endpoints.js'
import AccountsPage from './AccountsPage.jsx'

vi.mock('../../api/endpoints.js', () => ({
  getAdminUsers: vi.fn(),
  getMe: vi.fn(),
  updateAdminUser: vi.fn(),
}))

const currentDeveloper = {
  id: 1,
  version: 0,
  email: 'dev@example.com',
  name: '개발자',
  role: 'DEVELOPER',
  status: 'ACTIVE',
  createdAt: '2026-01-01T00:00:00',
}
const targetUser = {
  id: 2,
  version: 3,
  email: 'user@example.com',
  name: '대상 사용자',
  role: 'USER',
  status: 'ACTIVE',
  createdAt: '2026-01-02T00:00:00',
}

describe('AccountsPage', () => {
  beforeEach(() => {
    getMe.mockResolvedValue(currentDeveloper)
    getAdminUsers.mockResolvedValue([currentDeveloper, targetUser])
  })

  it('역할 변경 요청에 화면이 읽은 계정 버전을 포함한다', async () => {
    const user = userEvent.setup()
    updateAdminUser.mockResolvedValue({ ...targetUser, version: 4, role: 'VIEWER' })
    render(<AccountsPage />)

    await user.selectOptions(await screen.findByLabelText('대상 사용자 역할'), 'VIEWER')

    expect(updateAdminUser).toHaveBeenCalledWith(2, { version: 3, role: 'VIEWER' })
    await waitFor(() => expect(screen.getByLabelText('대상 사용자 역할')).toHaveValue('VIEWER'))
  })

  it('변경 충돌 뒤 계정 목록을 다시 불러온다', async () => {
    const user = userEvent.setup()
    updateAdminUser.mockRejectedValue(new Error('최신 내용을 확인해 주세요.'))
    getAdminUsers
      .mockResolvedValueOnce([currentDeveloper, targetUser])
      .mockResolvedValueOnce([currentDeveloper, { ...targetUser, version: 4, role: 'VIEWER' }])
    render(<AccountsPage />)

    await user.selectOptions(await screen.findByLabelText('대상 사용자 역할'), 'DEVELOPER')

    expect(await screen.findByText('최신 내용을 확인해 주세요.')).toBeInTheDocument()
    await waitFor(() => expect(getAdminUsers).toHaveBeenCalledTimes(2))
    await waitFor(() => expect(screen.getByLabelText('대상 사용자 역할')).toHaveValue('VIEWER'))
  })
})
