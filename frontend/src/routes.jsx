import RouteFrame from './components/RouteFrame.jsx'
import RouteFallback from './components/RouteFallback.jsx'

/** 각 화면 코드는 해당 경로에 들어갈 때만 받는다. 차트와 드래그 라이브러리가 첫 화면을 막지 않게 한다. */
function lazyPage(loader) {
  return async () => {
    const module = await loader()
    return { Component: module.default }
  }
}

/**
 * 라우트 정의 (react-router-dom v7).
 *
 * 이 파일은 0-1단계 공통 기반 산출물이다. 기획 13-4 의 공유 파일 규칙에 따라
 * **모든 경로를 여기에 미리 선언해 두었으므로 담당자가 경로를 추가할 일이 없다.**
 * 각자 자기 화면 파일(pages/user, pages/admin)만 채운다.
 *
 * 권한 검사는 화면이 아니라 API 에서 한다. (기획 2장)
 * 관리용 주소를 직접 입력해도 서버가 데이터를 주지 않으므로, 여기서 경로를 막지 않는다.
 *
 * 관리용 경로는 AdminLayout의 children으로 묶여 공통 상단 탭과 프로젝트 전환을 공유한다.
 */
export const routes = [{
  Component: RouteFrame,
  HydrateFallback: RouteFallback,
  children: [
    // ── 사용자용 ────────────────────────────────────────────────────
    { index: true, lazy: lazyPage(() => import('./pages/user/LoginPage.jsx')) },
    { path: 'write', lazy: lazyPage(() => import('./pages/user/WritePage.jsx')) },
    { path: 'home', lazy: lazyPage(() => import('./pages/user/HomePage.jsx')) },
    { path: 'my/:id', lazy: lazyPage(() => import('./pages/user/MyFeedbackDetailPage.jsx')) },

    // ── 관리용 ──────────────────────────────────────────────────────
    // 승인 대기 안내는 레이아웃 바깥에 둔다. 아직 승인되지 않은 계정에게
    // 상단 탭과 프로젝트 전환을 보여 줄 이유가 없다. (기획 3-2)
    { path: 'admin/pending', lazy: lazyPage(() => import('./pages/admin/PendingPage.jsx')) },
    {
      // 관리 화면이 공통 UI를 공유하도록 추가한 레이아웃 부모 라우트다.
      // **경로 문자열은 하나도 바꾸지 않았고 경로가 늘지도 않았다.** 감싸기만 한다.
      lazy: lazyPage(() => import('./pages/admin/AdminLayout.jsx')),
      children: [
        { path: 'admin', lazy: lazyPage(() => import('./pages/admin/DashboardPage.jsx')) },
        { path: 'admin/accounts', lazy: lazyPage(() => import('./pages/admin/AccountsPage.jsx')) },
        // :projectCode 는 GET /api/projects 응답의 code 값이다.
        // 경로가 2단이라 위의 /admin/pending, /admin/accounts 와 겹치지 않는다.
        //
        // intake 는 0-1단계에 없던 경로다. **팀 확인이 필요한 기획 변경이다.**
        // 기획 6-1 은 상단 탭을 개발 보드·답변 두 개로 정했는데, 접수를 개발 보드에서 떼어
        // 세 번째 탭으로 만들면서 경로가 하나 늘었다. 기존 경로 문자열은 바꾸지 않았다.
        { path: 'admin/:projectCode/intake', lazy: lazyPage(() => import('./pages/admin/IntakePage.jsx')) },
        { path: 'admin/:projectCode/board', lazy: lazyPage(() => import('./pages/admin/BoardPage.jsx')) },
        { path: 'admin/:projectCode/answers', lazy: lazyPage(() => import('./pages/admin/AnswersPage.jsx')) },
      ],
    },

    // ── 나머지 ──────────────────────────────────────────────────────
    { path: '*', lazy: lazyPage(() => import('./pages/NotFoundPage.jsx')) },
  ],
}]
