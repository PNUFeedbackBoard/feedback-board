package kr.ac.pusan.feedback.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI feedbackOpenAPI() {
		return new OpenAPI()
                .components(new Components().addSecuritySchemes("CSRF", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-CSRF-TOKEN")
                        .description("같은 브라우저에서 /api/auth/csrf의 token을 가져와 입력합니다. 로그인·로그아웃 후 재발급합니다.")))
                .addSecurityItem(new SecurityRequirement().addList("CSRF"))
				.info(new Info()
						.title("통합 피드백 게시판 API")
						.description("부산대 AI융합교육원 시스템 피드백 API")
						.version("v1"));
	}
}
