import { Navigate, Route, Routes } from 'react-router-dom'
import { ROUTES } from './routes.js'
import './App.css'

function EmptyRoute({ eyebrow, title }) {
  return (
    <main className="route-placeholder">
      <p className="route-placeholder__eyebrow">{eyebrow}</p>
      <h1>{title}</h1>
      <p>0-1단계에서 경로만 선언한 화면입니다.</p>
    </main>
  )
}

function App() {
  return (
    <Routes>
      <Route
        path={ROUTES.login}
        element={<EmptyRoute eyebrow="사용자" title="로그인" />}
      />
      <Route
        path={ROUTES.home}
        element={<EmptyRoute eyebrow="사용자" title="내 문의" />}
      />
      <Route
        path={ROUTES.newFeedback}
        element={<EmptyRoute eyebrow="사용자" title="새 질문 적기" />}
      />
      <Route
        path={ROUTES.feedbackDetail}
        element={<EmptyRoute eyebrow="사용자" title="문의 상세" />}
      />
      <Route
        path={ROUTES.adminDashboard}
        element={<EmptyRoute eyebrow="관리" title="전체 현황 대시보드" />}
      />
      <Route
        path={ROUTES.adminBoard}
        element={<EmptyRoute eyebrow="관리" title="개발 보드" />}
      />
      <Route
        path={ROUTES.adminAnswers}
        element={<EmptyRoute eyebrow="관리" title="답변" />}
      />
      <Route
        path={ROUTES.adminAccounts}
        element={<EmptyRoute eyebrow="관리" title="계정 관리" />}
      />
      <Route path="/admin/dashboard" element={<Navigate to={ROUTES.adminDashboard} replace />} />
      <Route path="*" element={<Navigate to={ROUTES.login} replace />} />
    </Routes>
  )
}

export default App
