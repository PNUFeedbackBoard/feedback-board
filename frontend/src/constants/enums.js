/**
 * 백엔드 kr.ac.pusan.feedback.common.enums 패키지의 enum 7종을 그대로 미러링한다.
 *
 * 규칙
 * - 키는 백엔드 enum 상수명과 한 글자도 다르면 안 된다. API 요청·응답에 그대로 오가는 값이다.
 * - 값은 화면에 표시할 한국어 label 이다. 화면에서 한국어 문자열을 직접 쓰지 말고 여기를 거친다.
 * - 새 상수가 필요하면 백엔드 enum 을 먼저 고치고 여기에 반영한다. 반대 순서로 하면 어긋난다.
 */

/** 피드백 유형. 백엔드 FeedbackCategory */
export const FEEDBACK_CATEGORY = {
  BUG: '오류 신고',
  FEATURE: '기능 제안',
  ETC: '기타 의견',
}

/** 진행 상태. 백엔드 FeedbackStatus */
export const FEEDBACK_STATUS = {
  RECEIVED: '접수',
  IN_PROGRESS: '처리 중',
  DONE: '처리 완료',
  REJECTED: '반영 불가',
}

/** 중요도. 백엔드 Priority */
export const PRIORITY = {
  HIGH: '높음',
  NORMAL: '보통',
  LOW: '낮음',
}

/** 역할. 백엔드 Role */
export const ROLE = {
  USER: '일반 사용자',
  DEVELOPER: '개발자',
  VIEWER: '열람자',
}

/** 계정 상태. 백엔드 UserStatus */
export const USER_STATUS = {
  PENDING: '승인 대기',
  ACTIVE: '활성',
  DISABLED: '비활성',
}

/** 작성자 구분. 백엔드 AuthorType */
export const AUTHOR_TYPE = {
  MEMBER: '회원',
  GUEST: '비회원',
}

/** 관리용 목록 정렬 기준. 백엔드 FeedbackSort */
export const FEEDBACK_SORT = {
  PRIORITY: '중요도순',
  LATEST: '최신순',
  OLDEST: '오래된순',
}

/**
 * 칸반 보드가 열로 사용하는 상태 3종. (기획 4-2)
 * REJECTED 는 열을 차지하지 않고 보드 하단의 별도 목록에서 조회한다.
 */
export const BOARD_COLUMN_STATUSES = ['RECEIVED', 'IN_PROGRESS', 'DONE']

/**
 * 라디오·드롭다운에 쓰기 좋은 [{ value, label }] 배열로 바꾼다.
 * 계약이 아니라 편의 함수다. 필요 없으면 쓰지 않아도 된다.
 *
 * @param {Record<string, string>} enumObject 위에서 정의한 enum 객체
 * @returns {{ value: string, label: string }[]}
 */
export function toOptions(enumObject) {
  return Object.entries(enumObject).map(([value, label]) => ({ value, label }))
}

/**
 * enum 값에 해당하는 한국어 label 을 찾는다. 모르는 값이면 값 자체를 그대로 돌려준다.
 * 백엔드에 상수가 추가되었는데 화면이 아직 모를 때 빈칸 대신 원본 값을 보여주기 위한 처리다.
 *
 * @param {Record<string, string>} enumObject 위에서 정의한 enum 객체
 * @param {string | null | undefined} value enum 상수명
 * @returns {string}
 */
export function labelOf(enumObject, value) {
  if (value == null) return ''
  return enumObject[value] ?? value
}
