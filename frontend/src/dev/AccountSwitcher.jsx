import { useState } from 'react'
import { ApiError } from '../api/client.js'
import { devLogin, devLogout } from '../api/endpoints.js'

/**
 * 개발용 계정 전환 위젯. 기획 13-2 참고.
 *
 * - `import.meta.env.DEV` 가 참일 때만 렌더한다. `npm run build` 결과물에는 화면이 나오지 않는다.
 * - POST /api/dev/login 과 /api/dev/logout 을 호출한다. 두 엔드포인트는 백엔드 dev 프로필에서만 등록된다.
 * - 7단계에서 구글 로그인으로 전환할 때 이 폴더(src/dev)만 지우면 된다. 화면 코드는 건드리지 않는다.
 *
 * 스타일은 위치와 가독성에 필요한 최소한만 둔다. 색은 브라우저 시스템 색(Canvas 등)을 써서
 * 0-2단계에서 D가 정할 색상 팔레트와 겹치지 않게 했다.
 */
export default function AccountSwitcher() {
  const [state, setState] = useState({ busy: false, message: '계정을 고르세요' })

  // 훅 호출 뒤에 검사한다. 순서가 바뀌면 rules-of-hooks 위반이다.
  if (!import.meta.env.DEV) return null

  async function run(label, action) {
    setState({ busy: true, message: `${label} 처리 중…` })
    try {
      const result = await action()
      // 로그아웃은 본문이 없어 null 이 온다.
      setState({
        busy: false,
        message: result ? `${result.name} · ${result.role}` : '로그아웃됨',
      })
    } catch (error) {
      const reason =
        error instanceof ApiError && error.status === 404
          ? 'dev 프로필로 백엔드를 켰는지 확인하세요'
          : error.message
      setState({ busy: false, message: `실패: ${reason}` })
    }
  }

  return (
    <div style={PANEL_STYLE}>
      <strong>DEV 계정 전환</strong>
      <div style={ROW_STYLE}>
        {ACCOUNTS.map(({ account, label }) => (
          <button
            key={account}
            type="button"
            disabled={state.busy}
            onClick={() => run(label, () => devLogin(account))}
          >
            {label}
          </button>
        ))}
        <button type="button" disabled={state.busy} onClick={() => run('로그아웃', devLogout)}>
          로그아웃
        </button>
      </div>
      <span>{state.message}</span>
    </div>
  )
}

/**
 * DevLoginRequest 의 account 값과 화면에 보일 이름. 기획 13-2 의 데모 계정 4종이다.
 * 백엔드 DevLoginController.DemoAccount 와 같은 4종을 둔다.
 * `승인 대기` 가 있어야 D 가 /admin/pending 화면을 실제로 열어 볼 수 있다.
 */
const ACCOUNTS = [
  { account: 'dev', label: '개발자' },
  { account: 'viewer', label: '열람자' },
  { account: 'user', label: '사용자' },
  { account: 'pending', label: '승인 대기' },
]

const PANEL_STYLE = {
  position: 'fixed',
  right: '12px',
  bottom: '12px',
  zIndex: 9999,
  display: 'flex',
  flexDirection: 'column',
  gap: '6px',
  padding: '8px 10px',
  fontSize: '12px',
  lineHeight: 1.4,
  background: 'Canvas',
  color: 'CanvasText',
  border: '1px solid GrayText',
  borderRadius: '6px',
}

const ROW_STYLE = { display: 'flex', gap: '4px' }
