import AccountsPage from './pages/admin/AccountsPage.jsx'
import AdminLayout from './pages/admin/AdminLayout.jsx'
import AnswersPage from './pages/admin/AnswersPage.jsx'
import BoardPage from './pages/admin/BoardPage.jsx'
import DashboardPage from './pages/admin/DashboardPage.jsx'
import PendingPage from './pages/admin/PendingPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import HomePage from './pages/user/HomePage.jsx'
import LoginPage from './pages/user/LoginPage.jsx'
import MyFeedbackDetailPage from './pages/user/MyFeedbackDetailPage.jsx'
import WritePage from './pages/user/WritePage.jsx'

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
 * TODO(C, 1단계): 관리용 공통 레이아웃(상단 탭 · 하단 디스크)이 필요하면
 *                 /admin 계열 4개를 children 으로 묶은 부모 라우트를 이 파일에 하나 추가한다.
 *                 부모 element 안에서 <Outlet /> 을 렌더하면 된다. 경로 문자열은 바꾸지 않는다.
 */
export const routes = [
  // ── 사용자용 ────────────────────────────────────────────────────
  { path: '/', element: <LoginPage /> },
  { path: '/write', element: <WritePage /> },
  { path: '/home', element: <HomePage /> },
  { path: '/my/:id', element: <MyFeedbackDetailPage /> },

  // ── 관리용 ──────────────────────────────────────────────────────
  // 승인 대기 안내는 레이아웃 바깥에 둔다. 아직 승인되지 않은 계정에게
  // 상단 탭과 프로젝트 전환을 보여 줄 이유가 없다. (기획 3-2)
  { path: '/admin/pending', element: <PendingPage /> },
  {
    // 위 TODO(C, 1단계) 대로 추가한 레이아웃 부모 라우트다.
    // **경로 문자열은 하나도 바꾸지 않았고 경로가 늘지도 않았다.** 감싸기만 한다.
    element: <AdminLayout />,
    children: [
      { path: '/admin', element: <DashboardPage /> },
      { path: '/admin/accounts', element: <AccountsPage /> },
      // :projectCode 는 projects 의 code 값이다. codeplace | aipms | aicms | aicap | srvadm
      // 경로가 2단이라 위의 /admin/pending, /admin/accounts 와 겹치지 않는다.
      { path: '/admin/:projectCode/board', element: <BoardPage /> },
      { path: '/admin/:projectCode/answers', element: <AnswersPage /> },
    ],
  },

  // ── 나머지 ──────────────────────────────────────────────────────
  { path: '*', element: <NotFoundPage /> },
]
