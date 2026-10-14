import { describe, expect, it, vi } from 'vitest'
import { ApiError, get, patch } from './client.js'

function jsonResponse(body, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: { get: () => 'application/json' },
    text: vi.fn().mockResolvedValue(JSON.stringify(body)),
  }
}

describe('API client', () => {
  it('변경 요청 전에 CSRF 토큰을 받아 지정된 헤더로 보낸다', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse({ headerName: 'X-CSRF-TOKEN', token: 'csrf-token' }))
      .mockResolvedValueOnce(jsonResponse({ id: 7, version: 2 }))
    vi.stubGlobal('fetch', fetchMock)

    await patch('/admin/users/7', { version: 1, role: 'VIEWER' })

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/auth/csrf', expect.objectContaining({
      method: 'GET',
      credentials: 'include',
    }))
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/users/7', expect.objectContaining({
      method: 'PATCH',
      credentials: 'include',
      headers: expect.objectContaining({
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': 'csrf-token',
      }),
      body: JSON.stringify({ version: 1, role: 'VIEWER' }),
    }))
  })

  it.each([
    [403, 'FORBIDDEN', '권한이 없습니다.'],
    [409, 'STALE_VERSION', '최신 내용을 확인해 주세요.'],
  ])('%i 응답의 코드와 메시지를 ApiError로 보존한다', async (status, code, message) => {
    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce(jsonResponse({ headerName: 'X-CSRF-TOKEN', token: 'csrf-token' }))
      .mockResolvedValueOnce(jsonResponse({
        status,
        code,
        message,
        path: '/api/admin/users/7',
      }, status)))

    await expect(patch('/admin/users/7', { version: 0, role: 'USER' }))
      .rejects.toMatchObject({
        name: 'ApiError',
        status,
        code,
        message,
      })
  })

  it('동시에 시작된 같은 GET 요청은 하나의 네트워크 요청을 공유한다', async () => {
    let resolveFetch
    const fetchMock = vi.fn(() => new Promise((resolve) => {
      resolveFetch = resolve
    }))
    vi.stubGlobal('fetch', fetchMock)

    const first = get('/projects')
    const second = get('/projects')

    expect(first).toBe(second)
    expect(fetchMock).toHaveBeenCalledTimes(1)

    resolveFetch(jsonResponse([{ id: 1, code: 'aipms' }]))
    await expect(first).resolves.toEqual([{ id: 1, code: 'aipms' }])
  })

  it('네트워크 오류를 사용자용 ApiError로 바꾼다', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('connection refused')))

    await expect(get('/projects')).rejects.toEqual(expect.objectContaining({
      constructor: ApiError,
      status: 0,
      code: 'NETWORK_ERROR',
    }))
  })
})
