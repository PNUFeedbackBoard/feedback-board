import { useEffect, useState } from 'react'
import { getAdminUsers } from '../../../api/endpoints.js'

/**
 * 담당자로 고를 수 있는 개발자 목록. 등록된 DEVELOPER · ACTIVE 계정 전부다.
 *
 * 계정 목록 API 는 DEVELOPER 만 부를 수 있어(SecurityConfig) 수정 권한이 없으면 받지 않는다.
 * 이름을 매번 손으로 적으면 표기가 갈리므로 목록에서 고르게 한다.
 *
 * @param {boolean} enabled 수정 권한이 있을 때만 true
 * @returns {Array<{ id: number, name: string }>}
 */
export default function useDevelopers(enabled) {
	const [developers, setDevelopers] = useState([])

	useEffect(() => {
		if (!enabled) return undefined
		let cancelled = false

		getAdminUsers()
			.then((users) => {
				if (!cancelled) {
					setDevelopers(users.filter((user) => user.role === 'DEVELOPER' && user.status === 'ACTIVE'))
				}
			})
			.catch(() => !cancelled && setDevelopers([]))

		return () => {
			cancelled = true
		}
	}, [enabled])

	return developers
}
