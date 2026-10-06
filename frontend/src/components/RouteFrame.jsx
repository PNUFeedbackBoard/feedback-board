import { Outlet, useNavigation } from 'react-router-dom'

/** 화면 전환 중에도 기존 화면을 유지하고 상단의 얇은 진행 표시로 클릭 결과를 즉시 알린다. */
export default function RouteFrame() {
  const navigation = useNavigation()
  const loading = navigation.state !== 'idle'

  return (
    <>
      <div className={loading ? 'route-progress is-active' : 'route-progress'} aria-hidden="true">
        <span />
      </div>
      <Outlet />
    </>
  )
}
