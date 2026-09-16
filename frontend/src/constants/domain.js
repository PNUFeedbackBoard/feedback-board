export const FEEDBACK_TYPES = Object.freeze([
  'FEATURE_REQUEST',
  'BUG_REPORT',
  'OTHER',
])

export const FEEDBACK_STATUSES = Object.freeze([
  'RECEIVED',
  'IN_PROGRESS',
  'DONE',
  'REJECTED',
])

export const PRIORITIES = Object.freeze(['LOW', 'MEDIUM', 'HIGH'])

export const ROLES = Object.freeze(['DEVELOPER', 'VIEWER', 'USER'])

export const PROJECTS = Object.freeze([
  { code: 'codeplace', name: '코드플레이스' },
  { code: 'aipms', name: 'AIPMS' },
  { code: 'aicms', name: 'AICMS' },
  { code: 'aicap', name: 'AI역량지원시스템' },
  { code: 'srvadm', name: 'pickle (서버관리)' },
])
