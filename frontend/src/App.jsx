import { lazy, Suspense } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import RouteFallback from './components/RouteFallback.jsx'
import { routes } from './routes.jsx'

// 라우터는 모듈 로드 시점에 한 번만 만든다. 컴포넌트 안에서 만들면 렌더마다 새로 생겨 화면이 초기화된다.
const router = createBrowserRouter(routes)
// 운영 번들에는 개발용 계정 전환 위젯과 스타일을 포함하지 않는다.
const AccountSwitcher = import.meta.env.DEV
  ? lazy(() => import('./dev/AccountSwitcher.jsx'))
  : null

/**
 * 애플리케이션 껍데기.
 *
 * 여기에 화면 내용을 넣지 않는다. 경로별 화면은 routes.jsx 가 연결한다.
 * 공통 헤더 같은 요소가 필요하면 App 이 아니라 routes.jsx 의 레이아웃 라우트에 붙인다.
 */
export default function App() {
  return (
    <>
      <RouterProvider router={router} fallbackElement={<RouteFallback />} />
      {/* 개발 모드에서만 보이는 계정 전환 위젯. 운영 화면에는 나오지 않는다. */}
      {AccountSwitcher && (
        <Suspense fallback={null}>
          <AccountSwitcher />
        </Suspense>
      )}
    </>
  )
}
