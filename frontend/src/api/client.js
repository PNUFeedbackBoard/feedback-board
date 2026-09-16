/**
 * 백엔드 호출용 fetch 래퍼.
 *
 * - 주소는 항상 '/api' 로 시작한다. Vite 개발 서버가 백엔드(기본 8080)로 넘긴다. vite.config.js 참고.
 * - 인증은 세션 쿠키로 한다. 그래서 모든 요청에 credentials: 'include' 를 넣는다.
 * - 실패 응답은 백엔드 공통 오류 형식을 파싱해 ApiError 로 던진다.
 *   화면은 try/catch 로 받아 error.status 와 error.message 만 보면 된다.
 *
 * 화면에서 이 파일을 직접 import 하지 말고 endpoints.js 의 함수를 쓴다.
 */

/** 모든 요청 앞에 붙는 주소. 바꾸면 Vite 프록시 설정도 같이 바꿔야 한다. */
const BASE_URL = '/api'

/**
 * 백엔드 공통 오류 응답(ApiErrorResponse)을 담은 에러.
 * { timestamp, status, code, message, path, details } 를 그대로 옮겨 담는다.
 */
export class ApiError extends Error {
  /**
   * @param {string} message 화면에 보여줄 메시지
   * @param {object} [payload] 서버가 내려준 공통 오류 본문
   */
  constructor(message, payload = {}) {
    super(message)
    this.name = 'ApiError'
    this.timestamp = payload.timestamp ?? null
    this.status = payload.status ?? 0
    this.code = payload.code ?? null
    this.path = payload.path ?? null
    /** 필드별 검증 오류 목록. 없으면 null */
    this.details = payload.details ?? null
  }
}

/**
 * 쿼리 스트링을 만든다. null·undefined·빈 문자열인 값은 빼고, 배열은 같은 키를 여러 번 붙인다.
 *
 * @param {Record<string, unknown> | undefined | null} params
 * @returns {string} '?a=1&b=2' 또는 빈 문자열
 */
function buildQuery(params) {
  if (!params) return ''
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value === null || value === undefined || value === '') continue
    if (Array.isArray(value)) {
      value.filter((item) => item !== null && item !== undefined && item !== '').forEach((item) => search.append(key, String(item)))
    } else {
      search.append(key, String(value))
    }
  }
  const query = search.toString()
  return query ? `?${query}` : ''
}

/**
 * 응답 본문을 해석한다. 204 나 빈 본문이면 null 을 준다.
 *
 * @param {Response} response
 * @returns {Promise<unknown>}
 */
async function readBody(response) {
  if (response.status === 204 || response.status === 205) return null
  const text = await response.text()
  if (!text) return null
  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('json')) {
    try {
      return JSON.parse(text)
    } catch {
      // JSON 이라고 했는데 깨진 경우. 원문을 그대로 넘겨 원인을 볼 수 있게 한다.
      return text
    }
  }
  return text
}

/**
 * 실패 응답을 ApiError 로 바꾼다.
 *
 * @param {Response} response
 * @param {unknown} body 이미 읽어 둔 응답 본문
 * @returns {ApiError}
 */
function toApiError(response, body) {
  const payload = body !== null && typeof body === 'object' ? body : {}
  const message =
    typeof payload.message === 'string' && payload.message
      ? payload.message
      : `요청을 처리하지 못했습니다. (HTTP ${response.status})`
  return new ApiError(message, { ...payload, status: payload.status ?? response.status })
}

/**
 * 실제 요청을 보낸다.
 *
 * @param {string} method HTTP 메서드
 * @param {string} path '/projects' 처럼 '/api' 뒤에 붙일 경로
 * @param {object} [options]
 * @param {unknown} [options.body] JSON 으로 직렬화해 보낼 본문
 * @param {Record<string, unknown>} [options.params] 쿼리 파라미터
 * @param {AbortSignal} [options.signal] 요청 취소용 시그널
 * @returns {Promise<unknown>} 응답 본문. 본문이 없으면 null
 */
async function request(method, path, options = {}) {
  const { body, params, signal } = options
  const url = `${BASE_URL}${path}${buildQuery(params)}`

  const init = {
    method,
    // 세션 쿠키로 인증한다. 이 줄을 빼면 로그인 상태가 서버에 전달되지 않는다.
    credentials: 'include',
    headers: { Accept: 'application/json' },
    signal,
  }

  if (body !== undefined) {
    init.headers['Content-Type'] = 'application/json'
    init.body = JSON.stringify(body)
  }

  let response
  try {
    response = await fetch(url, init)
  } catch (error) {
    // 요청 취소는 오류가 아니므로 그대로 올려보낸다.
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new ApiError('서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.', {
      status: 0,
      code: 'NETWORK_ERROR',
      path: url,
    })
  }

  const parsed = await readBody(response)
  if (!response.ok) throw toApiError(response, parsed)
  return parsed
}

/** GET 요청. 쿼리 파라미터는 options.params 로 넘긴다. */
export function get(path, options) {
  return request('GET', path, options)
}

/** POST 요청. 본문이 없으면 body 를 생략한다. */
export function post(path, body, options) {
  return request('POST', path, { ...options, body })
}

/** PATCH 요청. 보낸 필드만 바꾼다. */
export function patch(path, body, options) {
  return request('PATCH', path, { ...options, body })
}

/** PUT 요청. 전체를 덮어쓴다. */
export function put(path, body, options) {
  return request('PUT', path, { ...options, body })
}

/** DELETE 요청. delete 는 예약어라 del 로 둔다. */
export function del(path, options) {
  return request('DELETE', path, options)
}
