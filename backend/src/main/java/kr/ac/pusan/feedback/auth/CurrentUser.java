package kr.ac.pusan.feedback.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 컨트롤러가 현재 로그인한 계정을 받는 유일한 방법.
 *
 * <pre>
 * &#64;GetMapping("/api/me")
 * public MeResponse me(&#64;CurrentUser AppUserPrincipal currentUser) { ... }
 * </pre>
 *
 * <p><b>이 어노테이션이 있는 이유(중요).</b>
 * 기획안 13-2 는 "7단계에서 인증 방식을 교체해도 화면과 컨트롤러 코드를 고치지 않는다"를 전제로 한다.
 * 지금은 데모 로그인(POST /api/dev/login)이 세션을 만들고, 7단계에는 구글 OAuth 가 세션을 만든다.
 * 컨트롤러가 세션이나 SecurityContext, 하물며 "데모 로그인"이라는 말을 직접 알고 있으면
 * 7단계에 컨트롤러를 전부 열어야 한다.
 * 그래서 <b>인증 방식을 아는 코드는 auth 패키지 안에만 둔다.</b>
 * 컨트롤러는 이 어노테이션과 {@link AppUserPrincipal} 두 가지만 알면 되고,
 * 7단계의 교체 대상은 "로그인 엔드포인트"와 "사용자 조회" 두 곳으로 줄어든다.
 *
 * <p><b>주의.</b> 로그인하지 않은 요청에서는 값이 null 이다.
 * SecurityConfig 에서 인증을 요구하는 경로(/api/me/**, /api/admin/**)에서는 null 이 들어올 수 없다.
 * 공개 경로(POST /api/feedbacks)에서는 회원이면 값이 있고 비회원이면 null 이므로,
 * 그 null 을 그대로 "비회원(AuthorType.GUEST)" 판단에 쓰면 된다.
 *
 * <p>동작 원리: 스프링 시큐리티의 {@code @AuthenticationPrincipal} 을 감싼 메타 어노테이션이다.
 * 인자 해석은 시큐리티가 등록한 AuthenticationPrincipalArgumentResolver 가 처리하므로
 * 따로 등록할 설정이 없다.
 */
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
