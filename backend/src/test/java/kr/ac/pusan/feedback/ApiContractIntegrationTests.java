package kr.ac.pusan.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class ApiContractIntegrationTests {

	private static final Set<String> HTTP_METHODS = Set.of(
			"get", "post", "put", "patch", "delete", "head", "options", "trace"
	);

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	void openApiExposesExactlyThirteenProductOperations() throws Exception {
		String body = mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode document = objectMapper.readTree(body);
        assertThat(document.path("components").path("securitySchemes").path("CSRF").path("name").asText())
                .isEqualTo("X-CSRF-TOKEN");
        JsonNode paths = document.path("paths");
		long operationCount = 0;
		for (JsonNode pathItem : paths) {
			for (var fields = pathItem.fieldNames(); fields.hasNext(); ) {
				if (HTTP_METHODS.contains(fields.next())) {
					operationCount++;
				}
			}
		}

		assertThat(operationCount).isEqualTo(13);
		assertThat(paths.has("/api/dev/login")).isFalse();
		assertThat(paths.has("/api/me")).isFalse();
	}

	@Test
	void allThirteenProductOperationsReturnOkWithRequiredRoles() throws Exception {
		MockHttpSession user = login("user");
		MockHttpSession viewer = login("viewer");
		MockHttpSession developer = login("dev");

		mockMvc.perform(get("/api/projects")).andExpect(status().isOk());
		mockMvc.perform(post("/api/feedbacks").with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "projectCode": "aipms",
								  "category": "BUG",
								  "reportedPriority": "HIGH",
								  "title": "검색 오류",
								  "content": "검색 결과가 화면에 표시되지 않습니다."
								}
								"""))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/me/feedbacks").session(user)).andExpect(status().isOk());
		mockMvc.perform(get("/api/me/feedbacks/1").session(user)).andExpect(status().isOk());

		mockMvc.perform(get("/api/admin/dashboard").session(viewer)).andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/feedbacks").session(viewer)).andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/feedbacks/1").session(viewer)).andExpect(status().isOk());
		mockMvc.perform(post("/api/admin/feedbacks/1/priority-request").with(csrf())
						.session(viewer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\":" + feedbackVersion(viewer, 1) + "}"))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/admin/feedbacks/1").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\":" + feedbackVersion(developer, 1)
								+ ",\"status\":\"IN_PROGRESS\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/api/admin/feedbacks/bulk").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"items\":[{\"id\":1,\"version\":" + feedbackVersion(developer, 1)
								+ "}],\"status\":\"IN_PROGRESS\"}"))
				.andExpect(status().isOk());
		JsonNode answerTarget = feedbackDetail(developer, 1);
		String answerVersion = answerTarget.path("answer").isNull()
				? "null"
				: answerTarget.path("answer").path("version").asText();
		mockMvc.perform(put("/api/admin/feedbacks/1/answer").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"feedbackVersion\":" + answerTarget.path("version").asLong()
								+ ",\"answerVersion\":" + answerVersion
								+ ",\"content\":\"확인했습니다.\",\"markDone\":false}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/users").session(developer)).andExpect(status().isOk());
		mockMvc.perform(patch("/api/admin/users/4").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"role\":\"VIEWER\",\"status\":\"ACTIVE\"}"))
				.andExpect(status().isOk());
	}

	@Test
	@Transactional
	void createdFeedbackAndAdminChangesRoundTripThroughTheDatabase() throws Exception {
		MockHttpSession user = login("user");
		MockHttpSession developer = login("dev");
		long developerId = currentUserId(developer);

		String createBody = mockMvc.perform(post("/api/feedbacks").with(csrf())
						.session(user)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "projectCode": "aipms",
								  "category": "ETC",
								  "reportedPriority": "NORMAL",
								  "title": "통합 테스트 문의",
								  "content": "프런트와 백엔드 연결 상태를 확인하는 문의입니다."
								}
								"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(createBody).path("id").asLong();

		JsonNode myItems = objectMapper.readTree(mockMvc.perform(get("/api/me/feedbacks").session(user))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString());
		assertThat(StreamSupport.stream(myItems.spliterator(), false)
				.anyMatch(item -> item.path("id").asLong() == id)).isTrue();

		mockMvc.perform(get("/api/admin/feedbacks")
						.session(developer)
						.param("project", "aipms")
						.param("category", "ETC"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.id == " + id + ")]").exists());

		mockMvc.perform(patch("/api/admin/feedbacks/" + id).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\":" + feedbackVersion(developer, id)
								+ ",\"status\":\"IN_PROGRESS\",\"assigneeId\":" + developerId + "}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.assigneeId").value(developerId))
				.andExpect(jsonPath("$.assigneeName").value("데모 개발자"));

		mockMvc.perform(put("/api/admin/feedbacks/" + id + "/answer").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"feedbackVersion\":" + feedbackVersion(developer, id)
								+ ",\"answerVersion\":null,\"content\":\"연결 상태를 확인했습니다.\",\"markDone\":true}"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/me/feedbacks/" + id).session(user))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DONE"))
				.andExpect(jsonPath("$.answer.content").value("연결 상태를 확인했습니다."));
	}

	@Test
	void viewerCanReadAdminApiButCannotModifyFeedback() throws Exception {
		MockHttpSession session = login("viewer");

		mockMvc.perform(get("/api/admin/feedbacks").session(session))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/api/admin/feedbacks/1").with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"DONE\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
		mockMvc.perform(patch("/api/admin/feedbacks/bulk").with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"items\":[{\"id\":1,\"version\":0}],\"status\":\"DONE\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	@Transactional
	void bulkFeedbackUpdateChangesAllItemsOrNone() throws Exception {
		MockHttpSession developer = login("dev");
		long firstId = createFeedback(developer, "일괄 변경 첫 번째");
		long secondId = createFeedback(developer, "일괄 변경 두 번째");
		long firstVersion = feedbackVersion(developer, firstId);
		long secondVersion = feedbackVersion(developer, secondId);

		mockMvc.perform(patch("/api/admin/feedbacks/bulk").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"items\":[{\"id\":" + firstId + ",\"version\":" + firstVersion
								+ "},{\"id\":" + secondId + ",\"version\":" + secondVersion
								+ "}],\"status\":\"IN_PROGRESS\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.updatedIds.length()").value(2));

		mockMvc.perform(get("/api/admin/feedbacks/" + firstId).session(developer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));
		mockMvc.perform(get("/api/admin/feedbacks/" + secondId).session(developer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		mockMvc.perform(patch("/api/admin/feedbacks/" + firstId).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\":" + feedbackVersion(developer, firstId)
								+ ",\"status\":\"RECEIVED\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/admin/feedbacks/bulk").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"items\":[{\"id\":" + firstId + ",\"version\":"
								+ feedbackVersion(developer, firstId)
								+ "},{\"id\":999999999,\"version\":0}],\"status\":\"DONE\"}"))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/admin/feedbacks/" + firstId).session(developer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("RECEIVED"));
	}

	@Test
	@Transactional
	void staleFeedbackVersionsReturnConflictWithoutOverwritingNewerChanges() throws Exception {
		MockHttpSession developer = login("dev");
		long firstId = createFeedback(developer, "버전 충돌 첫 번째");
		long secondId = createFeedback(developer, "버전 충돌 두 번째");
		long staleFirstVersion = feedbackVersion(developer, firstId);
		long secondVersion = feedbackVersion(developer, secondId);

		mockMvc.perform(patch("/api/admin/feedbacks/" + firstId).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\":" + staleFirstVersion + ",\"category\":\"BUG\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/admin/feedbacks/" + firstId).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\":" + staleFirstVersion + ",\"priority\":\"HIGH\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CONFLICT"));

		mockMvc.perform(patch("/api/admin/feedbacks/bulk").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"items\":[{\"id\":" + firstId + ",\"version\":" + staleFirstVersion
								+ "},{\"id\":" + secondId + ",\"version\":" + secondVersion
								+ "}],\"status\":\"DONE\"}"))
				.andExpect(status().isConflict());

		mockMvc.perform(get("/api/admin/feedbacks/" + firstId).session(developer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.category").value("BUG"))
				.andExpect(jsonPath("$.priority").value("NORMAL"));
		mockMvc.perform(get("/api/admin/feedbacks/" + secondId).session(developer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("RECEIVED"));
	}

	@Test
	@Transactional
	void staleAnswerVersionCannotOverwriteANewerAnswer() throws Exception {
		MockHttpSession developer = login("dev");
		long id = createFeedback(developer, "답변 버전 충돌");
		long feedbackVersion = feedbackVersion(developer, id);

		String firstBody = mockMvc.perform(put("/api/admin/feedbacks/" + id + "/answer").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"feedbackVersion\":" + feedbackVersion
								+ ",\"answerVersion\":null,\"content\":\"첫 답변\",\"markDone\":false}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		JsonNode firstAnswer = objectMapper.readTree(firstBody);

		String secondBody = mockMvc.perform(put("/api/admin/feedbacks/" + id + "/answer").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"feedbackVersion\":" + firstAnswer.path("feedbackVersion").asLong()
								+ ",\"answerVersion\":" + firstAnswer.path("version").asLong()
								+ ",\"content\":\"최신 답변\",\"markDone\":false}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		JsonNode secondAnswer = objectMapper.readTree(secondBody);

		mockMvc.perform(put("/api/admin/feedbacks/" + id + "/answer").with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"feedbackVersion\":" + secondAnswer.path("feedbackVersion").asLong()
								+ ",\"answerVersion\":" + firstAnswer.path("version").asLong()
								+ ",\"content\":\"오래된 답변\",\"markDone\":false}"))
				.andExpect(status().isConflict());

		mockMvc.perform(get("/api/admin/feedbacks/" + id).session(developer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer.content").value("최신 답변"));
	}

	@Test
	@Transactional
	void memberCanReadOnlyOwnFeedbackAndGuestCannotReadMemberApi() throws Exception {
		MockHttpSession member = login("user");
		MockHttpSession anotherMember = login("viewer");

		String createBody = mockMvc.perform(post("/api/feedbacks").with(csrf())
						.session(member)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "projectCode": "aipms",
								  "category": "ETC",
								  "reportedPriority": "NORMAL",
								  "title": "내 문의 접근 범위 테스트",
								  "content": "로그인한 회원 본인만 이 문의를 조회할 수 있어야 합니다."
								}
								"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(createBody).path("id").asLong();

		mockMvc.perform(get("/api/me/feedbacks"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/me/feedbacks/" + id).session(anotherMember))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/me/feedbacks/" + id).session(member))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));
	}

	@Test
	void currentUserResponseComesFromServerSession() throws Exception {
		MockHttpSession session = login("dev");

		mockMvc.perform(get("/api/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("DEVELOPER"));
	}

	@Test
	void developerCannotDemoteOrDisableCurrentAccount() throws Exception {
		MockHttpSession developer = login("dev");
		long developerId = currentUserId(developer);

		mockMvc.perform(patch("/api/admin/users/" + developerId).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"role\":\"USER\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(
						"현재 로그인한 개발자 계정의 권한이나 상태는 직접 변경할 수 없습니다."));

		mockMvc.perform(patch("/api/admin/users/" + developerId).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"DISABLED\"}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/me").session(developer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("DEVELOPER"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	@Transactional
	void roleAndStatusChangesApplyToExistingSessionsOnTheNextRequest() throws Exception {
		MockHttpSession developer = login("dev");
		MockHttpSession viewer = login("viewer");
		MockHttpSession user = login("user");
		long viewerId = currentUserId(viewer);
		long userId = currentUserId(user);

		mockMvc.perform(patch("/api/admin/users/" + viewerId).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"role\":\"USER\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/dashboard").session(viewer))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/admin/users/" + userId).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"role\":\"VIEWER\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/admin/dashboard").session(user))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/admin/users/" + viewerId).with(csrf())
						.session(developer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"DISABLED\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/me").session(viewer))
				.andExpect(status().isUnauthorized());
		assertThat(viewer.isInvalid()).isTrue();
	}

	@Test
	void adminFeedbackListUsesRequestedPageSize() throws Exception {
		MockHttpSession developer = login("dev");

		mockMvc.perform(get("/api/admin/feedbacks")
						.session(developer)
						.param("page", "0")
						.param("size", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(5))
				.andExpect(jsonPath("$.totalCount").isNumber());
	}

	@Test
	@Transactional
	void viewerCanTogglePriorityRequestOnOwnFeedback() throws Exception {
		MockHttpSession viewer = login("viewer");
		String createBody = mockMvc.perform(post("/api/feedbacks").with(csrf())
						.session(viewer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "projectCode": "aipms",
								  "category": "ETC",
								  "reportedPriority": "NORMAL",
								  "title": "우선 처리 요청 테스트",
								  "content": "열람자가 본인 피드백의 우선 처리를 요청합니다."
								}
								"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(createBody).path("id").asLong();

		long version = feedbackVersion(viewer, id);
		String priorityBody = mockMvc.perform(post("/api/admin/feedbacks/" + id + "/priority-request").with(csrf())
						.session(viewer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\":" + version + "}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.priorityRequested").value(true))
				.andExpect(jsonPath("$.priority").value("HIGH"))
				.andReturn().getResponse().getContentAsString();
		assertThat(objectMapper.readTree(priorityBody).path("version").asLong()).isGreaterThan(version);

		mockMvc.perform(post("/api/admin/feedbacks/" + id + "/priority-request").with(csrf())
						.session(viewer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\":" + version + "}"))
				.andExpect(status().isConflict());

		mockMvc.perform(get("/api/admin/feedbacks")
						.session(viewer)
						.param("priorityRequested", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.id == " + id + ")]").exists())
				.andExpect(jsonPath("$.items[?(@.priorityRequested == false)]").doesNotExist());
	}

	private MockHttpSession login(String account) throws Exception {
		return (MockHttpSession) mockMvc.perform(post("/api/dev/login").with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"account\":\"" + account + "\"}"))
				.andExpect(status().isOk())
				.andReturn()
				.getRequest()
				.getSession(false);
	}

	private long currentUserId(MockHttpSession session) throws Exception {
		String body = mockMvc.perform(get("/api/me").session(session))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}

	private JsonNode feedbackDetail(MockHttpSession session, long id) throws Exception {
		String body = mockMvc.perform(get("/api/admin/feedbacks/" + id).session(session))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body);
	}

	private long feedbackVersion(MockHttpSession session, long id) throws Exception {
		return feedbackDetail(session, id).path("version").asLong();
	}

	private long createFeedback(MockHttpSession session, String title) throws Exception {
		String body = mockMvc.perform(post("/api/feedbacks").with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "projectCode": "aipms",
								  "category": "ETC",
								  "reportedPriority": "NORMAL",
								  "title": "%s",
								  "content": "일괄 변경의 트랜잭션 동작을 확인합니다."
								}
								""".formatted(title)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(body).path("id").asLong();
	}
}
