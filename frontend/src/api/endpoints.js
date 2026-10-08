/**
 * 기획 9장 API 초안의 엔드포인트를 함수로 감싼 것이다.
 *
 * - 화면은 이 파일의 함수만 호출한다. 화면 코드에 주소 문자열을 직접 쓰지 않는다.
 * - 주소가 바뀌면 여기만 고치면 된다.
 * - 각 함수의 응답 모양은 주석의 계약 이름을 참고한다. (실제 타입 정의는 두지 않는다)
 *
 * 백엔드 계약을 화면에서 직접 반복하지 않도록 모든 요청을 이 파일로 모은다.
 */

import { get, patch, post, put } from './client.js'

// ── 공개 ────────────────────────────────────────────────────────────

/**
 * 프로젝트 목록.
 * @returns {Promise<Array>} ProjectResponse[] — { id, code, name, logoUrl, siteUrl, sortOrder }
 */
export function getProjects() {
  return get('/projects')
}

/**
 * 피드백 등록. 비회원도 호출할 수 있다.
 * @param {object} body FeedbackCreateRequest — { projectCode, category, reportedPriority, title, content }
 * @returns {Promise<object>} FeedbackCreateResponse — { id }
 */
export function createFeedback(body) {
  return post('/feedbacks', body)
}

// ── 회원 ────────────────────────────────────────────────────────────

/**
 * 로그인한 내 정보.
 * 화면은 역할을 자체 판단하지 않고 여기서 받은 role 값만 쓴다. (기획 13-2)
 * @returns {Promise<object>} MeResponse — { id, email, name, role, status }
 */
export function getMe() {
  return get('/me')
}

/**
 * 내가 등록한 피드백 목록.
 * @returns {Promise<Array>} MyFeedbackSummary[]
 */
export function getMyFeedbacks() {
  return get('/me/feedbacks')
}

/**
 * 내 피드백 상세. 답변을 포함한다.
 * @param {number | string} id 피드백 번호
 * @returns {Promise<object>} MyFeedbackDetail
 */
export function getMyFeedback(id) {
  return get(`/me/feedbacks/${id}`)
}

// ── 관리 · 개발자와 열람자 ──────────────────────────────────────────

/**
 * 전체 현황 대시보드 지표.
 * @returns {Promise<object>} DashboardResponse
 */
export function getDashboard() {
  return get('/admin/dashboard')
}

/**
 * 관리용 피드백 목록.
 * @param {object} [params] 필터와 정렬.
 *   { project, status, category, sort, from, to, authorType, answered, priorityRequested, page, size }
 *   sort 는 FEEDBACK_SORT 의 키(PRIORITY | LATEST | OLDEST)를 쓴다.
 *   비어 있는 값은 client.js 가 쿼리에서 알아서 빼준다.
 * @returns {Promise<object>} AdminFeedbackPage — { items, totalCount }
 */
export function getAdminFeedbacks(params) {
  return get('/admin/feedbacks', { params })
}

/**
 * 관리용 피드백 상세.
 * @param {number | string} id 피드백 번호
 * @returns {Promise<object>} AdminFeedbackDetail
 */
export function getAdminFeedback(id) {
  return get(`/admin/feedbacks/${id}`)
}

// ── 관리 · 개발자 전용 ──────────────────────────────────────────────

/**
 * 상태·유형·중요도 변경. 보낸 필드만 바뀐다.
 * 열람자가 호출하면 403 이 온다. (기획 9장)
 * @param {number | string} id 피드백 번호
 * @param {object} body FeedbackUpdateRequest — { status, category, priority, assigneeId, unassign }
 *   전부 생략 가능. assigneeId 는 DEVELOPER·ACTIVE 계정이 아니면 400. 담당자 해제는
 *   assigneeId 대신 unassign: true 를 보낸다(기획 8·9장).
 * @returns {Promise<object>} AdminFeedbackDetail
 */
export function updateAdminFeedback(id, body) {
  return patch(`/admin/feedbacks/${id}`, body)
}

/**
 * 답변 등록과 수정. 피드백 1건당 답변은 1건이므로 등록·수정을 하나의 PUT 으로 처리한다.
 * @param {number | string} id 피드백 번호
 * @param {object} body AnswerUpsertRequest — { content, markDone }
 * @returns {Promise<object>} AnswerResponse — { id, content, createdAt, updatedAt }
 */
export function upsertAnswer(id, body) {
  return put(`/admin/feedbacks/${id}/answer`, body)
}

/**
 * 계정 목록.
 * @returns {Promise<Array>} AdminUserResponse[] — { id, email, name, role, status, createdAt }
 */
export function getAdminUsers() {
  return get('/admin/users')
}

/**
 * 계정 승인과 역할 변경.
 * @param {number | string} id 계정 번호
 * @param {object} body AdminUserUpdateRequest — { role, status }
 * @returns {Promise<object>} AdminUserResponse
 */
export function updateAdminUser(id, body) {
  return patch(`/admin/users/${id}`, body)
}

// ── 관리 · 열람자 전용 ──────────────────────────────────────────────

/**
 * 우선 처리 요청 토글. 다시 부르면 해제된다. (기획 4-4)
 * @param {number | string} id 피드백 번호
 * @returns {Promise<object>} PriorityRequestResponse — { id, priorityRequested, priority }
 */
export function togglePriorityRequest(id) {
  return post(`/admin/feedbacks/${id}/priority-request`)
}

// ── 개발 전용 ───────────────────────────────────────────────────────
// dev 프로필에서만 등록된다. 운영 프로필에서는 404 가 온다. 구글 로그인 이후에도 개발용 권한 미리보기로 유지한다.

/**
 * 데모 계정 로그인.
 * 백엔드 DevLoginController 는 dev · viewer · user · pending 네 가지를 받는다. 그 밖의 값은 400 이다.
 * @param {'dev' | 'viewer' | 'user' | 'pending'} account DevLoginRequest 의 account 값
 * @returns {Promise<object>} MeResponse
 */
export function devLogin(account) {
  return post('/dev/login', { account })
}

/**
 * 데모 계정 로그아웃. 본문 없이 204 가 온다.
 * @returns {Promise<null>}
 */
export function devLogout() {
  return post('/dev/logout')
}

// 구글 로그인과 데모 로그인 공통 인증 보조 함수.
export function getAuthConfig() { return get('/auth/config') }
export function startGoogleLogin(intent = 'user', site = null) {
  const params = new URLSearchParams({ intent })
  if (site) params.set('site', site)
  window.location.assign(`/api/auth/login?${params}`)
}
export function logout() { return post('/auth/logout') }
