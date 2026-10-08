import './login-buttons.css'

/** 로그인 시안의 두 버튼. 인증과 이동은 페이지에서 주입한다. */
export default function LoginButtons({ onGoogleLogin, onGuestLogin, busy = false, googleDisabled = false }) {
  return (
    <div className="login-buttons" aria-busy={busy}>
      <button className="login-button" type="button" onClick={onGoogleLogin} disabled={busy || googleDisabled}>
        <svg className="login-button__google" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
          <path fill="#4285F4" d="M21.6 12.23c0-.71-.06-1.39-.18-2.05H12v3.88h5.38a4.6 4.6 0 0 1-2 3.02v2.51h3.24c1.9-1.75 2.98-4.33 2.98-7.36Z" />
          <path fill="#34A853" d="M12 22c2.7 0 4.96-.9 6.62-2.41l-3.24-2.51c-.9.6-2.05.97-3.38.97-2.6 0-4.81-1.76-5.6-4.12H3.05v2.59A10 10 0 0 0 12 22Z" />
          <path fill="#FBBC05" d="M6.4 13.93a6 6 0 0 1 0-3.86V7.48H3.05a10 10 0 0 0 0 9.04l3.35-2.59Z" />
          <path fill="#EA4335" d="M12 5.95c1.47 0 2.79.5 3.82 1.5l2.87-2.87A9.6 9.6 0 0 0 12 2a10 10 0 0 0-8.95 5.48l3.35 2.59C7.19 7.71 9.4 5.95 12 5.95Z" />
        </svg>
        <span>{busy ? '로그인 중…' : 'Google 로그인'}</span>
      </button>
      <div className="login-buttons__guest">
        <button className="login-button" type="button" onClick={onGuestLogin} disabled={busy} aria-describedby="guest-login-warning">
          비회원 로그인
        </button>
        <p id="guest-login-warning" className="login-buttons__warning">*비회원 로그인 시 개발자의 응답이 어렵습니다.</p>
      </div>
    </div>
  )
}
