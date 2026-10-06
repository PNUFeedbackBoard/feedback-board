package kr.ac.pusan.feedback.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import kr.ac.pusan.feedback.common.enums.*;
import kr.ac.pusan.feedback.domain.User;
import kr.ac.pusan.feedback.domain.repository.UserRepository;

@ActiveProfiles("dev")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GoogleLoginTests {
    @Autowired GoogleOAuth2SuccessHandler handler;
    @Autowired UserRepository users;
    @Autowired MockMvc mvc;
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    private OAuth2AuthenticationToken google(String sub, String email, boolean verified) {
        var authorities = List.of(new SimpleGrantedAuthority("OIDC_USER"));
        var principal = new DefaultOAuth2User(authorities,
                Map.of("sub", sub, "email", email, "email_verified", verified, "name", "테스트"), "sub");
        return new OAuth2AuthenticationToken(principal, authorities, "google");
    }
    private User existing(String email, String sub, UserStatus status) {
        return users.saveAndFlush(User.builder().email(email).name("기존 계정").googleSub(sub)
                .role(Role.VIEWER).status(status).createdAt(LocalDateTime.now()).build());
    }
    @Test void regularSignupCreatesActiveUserAndUsableSession() throws Exception {
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthController.LOGIN_SITE, "aipms");
        var response = new MockHttpServletResponse();
        String email = UUID.randomUUID() + "@gmail.com";
        handler.onAuthenticationSuccess(request, response, google("new-sub", email, true));
        User found = users.findByEmail(email).orElseThrow();
        assertThat(found.getRole()).isEqualTo(Role.USER);
        assertThat(found.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/write?site=aipms");
        mvc.perform(get("/api/me").session((MockHttpSession) request.getSession()))
                .andExpect(status().isOk()).andExpect(jsonPath("email").value(email));
        mvc.perform(post("/api/auth/logout").with(csrf()).session((MockHttpSession) request.getSession()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }
    @Test void adminSignupWaitsForApprovalAndCannotReadAdminData() throws Exception {
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute(AuthController.LOGIN_INTENT, "admin");
        var response = new MockHttpServletResponse();
        String email = UUID.randomUUID() + "@gmail.com";
        handler.onAuthenticationSuccess(request, response, google("admin-sub", email, true));
        var user = users.findByEmail(email).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(response.getRedirectedUrl()).endsWith("/admin/pending");
        mvc.perform(get("/api/admin/dashboard").session((MockHttpSession) request.getSession()))
                .andExpect(status().isForbidden());
        user.update(Role.VIEWER, UserStatus.ACTIVE);
        users.saveAndFlush(user);
        org.springframework.security.test.context.TestSecurityContextHolder.clearContext();
        var approved = new MockHttpServletRequest();
        handler.onAuthenticationSuccess(approved, new MockHttpServletResponse(), google("admin-sub", email, true));
        org.springframework.security.test.context.TestSecurityContextHolder.clearContext();
        mvc.perform(get("/api/admin/dashboard").session((MockHttpSession) approved.getSession()))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/admin/feedbacks/1").with(csrf()).session((MockHttpSession) approved.getSession())
                .contentType("application/json").content("{\"status\":\"DONE\"}"))
                .andExpect(status().isForbidden());
    }
    @Test void existingVerifiedEmailLinksOnceWithoutChangingRole() throws Exception {
        var user = existing("link@gmail.com", null, UserStatus.ACTIVE);
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), new MockHttpServletResponse(),
                google("linked-sub", user.getEmail(), true));
        assertThat(user.getGoogleSub()).isEqualTo("linked-sub");
        assertThat(user.getRole()).isEqualTo(Role.VIEWER);
        assertThat(users.findByGoogleSub("linked-sub")).isPresent();
    }
    @Test void differentSubCannotOverwriteLinkedAccount() throws Exception {
        var user = existing("conflict@gmail.com", "original", UserStatus.ACTIVE);
        var request = new MockHttpServletRequest(); request.getSession();
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, google("other", user.getEmail(), true));
        assertThat(response.getRedirectedUrl()).endsWith("authError=account_conflict");
        assertThat(user.getGoogleSub()).isEqualTo("original");
        assertThat(request.getSession(false)).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
    @Test void disabledAccountCannotCreateSession() throws Exception {
        var user = existing("disabled@gmail.com", "disabled-sub", UserStatus.DISABLED);
        var request = new MockHttpServletRequest(); request.getSession();
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, google("disabled-sub", user.getEmail(), true));
        assertThat(response.getRedirectedUrl()).endsWith("authError=disabled");
        assertThat(request.getSession(false)).isNull();
    }
    @Test void unverifiedEmailIsRejectedWithoutCreatingUser() throws Exception {
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, google("unverified", "no@gmail.com", false));
        assertThat(response.getRedirectedUrl()).endsWith("authError=invalid_google_account");
        assertThat(users.findByEmail("no@gmail.com")).isEmpty();
    }
    @Test void bootstrapAppliesOnlyToNewAccount() throws Exception {
        var bootstrap = new GoogleOAuth2SuccessHandler(users, "owner@gmail.com", "http://localhost:5173");
        var response = new MockHttpServletResponse();
        bootstrap.onAuthenticationSuccess(new MockHttpServletRequest(), response, google("owner-sub", "owner@gmail.com", true));
        var user = users.findByEmail("owner@gmail.com").orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.DEVELOPER);
        user.update(Role.USER, UserStatus.ACTIVE);
        bootstrap.onAuthenticationSuccess(new MockHttpServletRequest(), new MockHttpServletResponse(), google("owner-sub", user.getEmail(), true));
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }
    @Test void noCredentialsStillAllowsPreviewButNotGoogle() throws Exception {
        mvc.perform(get("/api/auth/config")).andExpect(status().isOk())
                .andExpect(jsonPath("googleEnabled").value(false)).andExpect(jsonPath("previewEnabled").value(true));
        mvc.perform(get("/api/auth/login")).andExpect(status().isServiceUnavailable());
        mvc.perform(get("/api/auth/login?intent=https://evil.example")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("token").isNotEmpty());
        mvc.perform(post("/api/dev/login").contentType("application/json").content("{\"account\":\"dev\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/dev/login").with(csrf()).contentType("application/json").content("{\"account\":\"dev\"}"))
                .andExpect(status().isOk());
    }
}
