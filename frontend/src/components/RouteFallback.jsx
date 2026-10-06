export default function RouteFallback() {
  return (
    <main className="route-fallback" aria-live="polite" aria-busy="true">
      <span className="route-fallback__bar" />
      <span className="route-fallback__label">화면을 준비하는 중…</span>
    </main>
  )
}
