package kr.ac.pusan.feedback.auth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=test-client",
    "spring.security.oauth2.client.registration.google.client-secret=test-secret"
})
@AutoConfigureMockMvc
class GoogleRedirectTests {
    @Autowired MockMvc mvc;
    @Test void configuredClientStartsAuthorizationWithStateAndCorrectCallback() throws Exception {
        mvc.perform(get("/api/auth/config")).andExpect(jsonPath("googleEnabled").value(true));
        var session = new MockHttpSession();
        mvc.perform(get("/api/auth/login?intent=admin").session(session))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/oauth2/authorization/google"));
        var result = mvc.perform(get("/oauth2/authorization/google").session(session))
                .andExpect(status().isFound()).andReturn();
        assertThat(result.getResponse().getRedirectedUrl()).startsWith("https://accounts.google.com/")
                .contains("client_id=test-client", "state=", "redirect_uri=");
        assertThat(session.getAttribute(AuthController.LOGIN_INTENT)).isEqualTo("admin");
    }
    @Test void invalidCallbackDoesNotAuthenticateAndReturnsReadableFailure() throws Exception {
        mvc.perform(get("/login/oauth2/code/google?error=access_denied&state=unknown"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:5173/?authError=google_failed"));
    }
}
