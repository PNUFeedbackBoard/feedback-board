// 경로: /write
// 피드백 작성 화면. 기획 5-2, 5-3 참고.
// TODO(D, 3단계): 사이트·유형·긴급도·제목·내용 입력을 채운다.
//                 ?site=aipms 로 진입하면 사이트를 고정하고, 홈에서 들어온 경우에만 드롭다운을 보인다.
//                 등록은 endpoints.js 의 createFeedback() 을 호출하고 접수 팝업을 띄운다.
export default function WritePage() {
  return (
    <main>
      <h1>피드백 작성</h1>
      <p>3단계 · D 담당</p>
      <p>회원과 비회원이 모두 쓰는 등록 화면이다.</p>
    </main>
  )
}
