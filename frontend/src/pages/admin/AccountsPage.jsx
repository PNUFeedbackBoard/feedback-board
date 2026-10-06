import { useEffect, useState } from 'react'
import { getAdminUsers, updateAdminUser } from '../../api/endpoints.js'
import { ROLE, USER_STATUS, labelOf, toOptions } from '../../constants/enums.js'
import '../page-shell.css'

export default function AccountsPage() {
	const [users, setUsers] = useState(null)
	const [busyId, setBusyId] = useState(null)
	const [message, setMessage] = useState('')

	useEffect(() => {
		let cancelled = false
		getAdminUsers()
			.then((found) => !cancelled && setUsers(found))
			.catch((error) => !cancelled && setMessage(error.message))
		return () => {
			cancelled = true
		}
	}, [])

	async function update(id, changes) {
		setBusyId(id)
		setMessage('')
		try {
			const changed = await updateAdminUser(id, changes)
			setUsers((current) => current.map((user) => (user.id === id ? changed : user)))
		} catch (error) {
			setMessage(error.message)
		} finally {
			setBusyId(null)
		}
	}

  return (
		<section className="app-page">
			<div className="app-page__inner app-stack">
				<header>
					<p className="app-page__eyebrow">ACCOUNT CONTROL</p>
					<h1>계정 관리</h1>
					<p className="app-page__muted">가입 승인을 처리하고 서비스 역할과 계정 상태를 관리합니다.</p>
				</header>
				{message && <p className="app-alert">{message}</p>}
				{users === null && !message && <p className="app-card">불러오는 중…</p>}
				{users && (
					<div className="app-card" style={{ overflowX: 'auto' }}>
						<table className="account-table">
							<thead>
								<tr><th>계정</th><th>가입일</th><th>역할</th><th>상태</th><th>처리</th></tr>
							</thead>
							<tbody>
								{users.map((user) => (
									<tr key={user.id}>
										<td><strong>{user.name}</strong><br /><span className="app-page__muted">{user.email}</span></td>
										<td>{formatDay(user.createdAt)}</td>
										<td>
											<select value={user.role} onChange={(event) => update(user.id, { role: event.target.value })} disabled={busyId === user.id} aria-label={`${user.name} 역할`}>
												{toOptions(ROLE).map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
											</select>
										</td>
										<td>{labelOf(USER_STATUS, user.status)}</td>
										<td>
											<div className="app-actions">
												{user.status !== 'ACTIVE' && <button className="app-button" type="button" disabled={busyId === user.id} onClick={() => update(user.id, { status: 'ACTIVE' })}>승인/활성화</button>}
												{user.status !== 'DISABLED' && <button className="app-button app-button--danger" type="button" disabled={busyId === user.id} onClick={() => update(user.id, { status: 'DISABLED' })}>비활성화</button>}
											</div>
										</td>
									</tr>
								))}
							</tbody>
						</table>
					</div>
				)}
			</div>
		</section>
  )
}

function formatDay(value) {
	return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(value))
}
