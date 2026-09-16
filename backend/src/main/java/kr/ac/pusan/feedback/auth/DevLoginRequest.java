package kr.ac.pusan.feedback.auth;

/**
 * 데모 로그인 요청 본문. POST /api/dev/login
 *
 * <p>account 는 "dev" | "viewer" | "user" | "pending" 중 하나다(기획안 13-2).
 * dev 프로필에서만 쓰는 개발용 요청이라 검증 어노테이션 대신 컨트롤러에서 직접 400 을 만든다.
 */
public record DevLoginRequest(String account) {
}
