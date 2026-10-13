import './current-user-badge.css'

/** 사용자 화면 우측 상단에 현재 로그인 계정을 표시한다. */
export default function CurrentUserBadge({ user }) {
  if (!user) return null

  const initial = user.name?.trim().charAt(0) || 'U'

  return (
    <div className="current-user-badge" title={user.email} aria-label={`${user.name} 계정으로 로그인 중`}>
      <span className="current-user-badge__avatar" aria-hidden="true">{initial}</span>
      <span className="current-user-badge__copy">
        <span className="current-user-badge__label">현재 사용자</span>
        <strong>{user.name}</strong>
        <span className="current-user-badge__email">{user.email}</span>
      </span>
    </div>
  )
}
