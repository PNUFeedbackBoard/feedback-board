import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getMe, getProjects } from '../../api/endpoints.js'
import AdminLayout from './AdminLayout.jsx'

vi.mock('../../api/endpoints.js', () => ({
  getMe: vi.fn(),
  getProjects: vi.fn(),
  logout: vi.fn(),
}))

function renderAdmin() {
  return render(
    <MemoryRouter initialEntries={['/admin']}>
      <Routes>
        <Route path="/" element={<p>로그인 화면</p>} />
        <Route path="/home" element={<p>사용자 홈</p>} />
        <Route path="/admin/pending" element={<p>승인 대기 화면</p>} />
        <Route element={<AdminLayout />}>
          <Route path="/admin" element={<p>관리자 본문</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  )
}

describe('AdminLayout access guard', () => {
  beforeEach(() => {
    getProjects.mockResolvedValue([])
  })

  it('비로그인 사용자를 로그인 화면으로 보낸다', async () => {
    getMe.mockRejectedValue({ status: 401, message: '로그인이 필요합니다.' })

    renderAdmin()

    expect(await screen.findByText('로그인 화면')).toBeInTheDocument()
    expect(getProjects).not.toHaveBeenCalled()
  })

  it('승인 대기 계정을 승인 대기 화면으로 보낸다', async () => {
    getMe.mockResolvedValue({ id: 2, name: '대기자', role: 'USER', status: 'PENDING' })

    renderAdmin()

    expect(await screen.findByText('승인 대기 화면')).toBeInTheDocument()
    expect(getProjects).not.toHaveBeenCalled()
  })

  it('일반 사용자를 사용자 홈으로 보낸다', async () => {
    getMe.mockResolvedValue({ id: 3, name: '사용자', role: 'USER', status: 'ACTIVE' })

    renderAdmin()

    expect(await screen.findByText('사용자 홈')).toBeInTheDocument()
    expect(getProjects).not.toHaveBeenCalled()
  })

  it.each(['DEVELOPER', 'VIEWER'])('%s 계정은 관리자 본문에 진입한다', async (role) => {
    getMe.mockResolvedValue({ id: 1, name: role, role, status: 'ACTIVE' })

    renderAdmin()

    expect(await screen.findByText('관리자 본문')).toBeInTheDocument()
    expect(getProjects).toHaveBeenCalledOnce()
  })

  it('인증 확인 중 네트워크 오류는 로그인으로 오인하지 않고 표시한다', async () => {
    getMe.mockRejectedValue({ status: 0, message: '서버에 연결하지 못했습니다.' })

    renderAdmin()

    expect(await screen.findByRole('alert')).toHaveTextContent('서버에 연결하지 못했습니다.')
    expect(screen.queryByText('로그인 화면')).not.toBeInTheDocument()
  })
})
