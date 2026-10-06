import { useState } from 'react'
import { logout } from '../../api/endpoints.js'
export default function LogoutButton() {
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  async function handleLogout() {
    setBusy(true)
    try {
      await logout()
      window.location.assign('/')
    } catch (error) {
      setMessage(error.message)
      setBusy(false)
    }
  }
  return <>
    <button className="app-button app-button--secondary" type="button" onClick={handleLogout} disabled={busy}>{busy ? '로그아웃 중…' : '로그아웃'}</button>
    {message && <span role="alert">{message}</span>}
  </>
}
