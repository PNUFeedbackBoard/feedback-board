import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import AccountSwitcher from './dev/AccountSwitcher.jsx'
import { routes } from './routes.jsx'

// 라우터는 모듈 로드 시점에 한 번만 만든다. 컴포넌트 안에서 만들면 렌더마다 새로 생겨 화면이 초기화된다.
const router = createBrowserRouter(routes)

/**
 * 애플리케이션 껍데기.
 *
 * 여기에 화면 내용을 넣지 않는다. 경로별 화면은 routes.jsx 가 연결한다.
 * 공통 헤더 같은 요소가 필요하면 App 이 아니라 routes.jsx 의 레이아웃 라우트에 붙인다.
 */
export default function App() {
  return (
    <>
      <RouterProvider router={router} />
      {/* 개발 모드에서만 보이는 계정 전환 위젯. 운영 화면에는 나오지 않는다. */}
      <AccountSwitcher />
    </>
  )
}
