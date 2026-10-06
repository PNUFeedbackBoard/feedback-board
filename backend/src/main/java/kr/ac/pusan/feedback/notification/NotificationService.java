package kr.ac.pusan.feedback.notification;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import kr.ac.pusan.feedback.domain.Feedback;
import kr.ac.pusan.feedback.domain.User;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 발송. 기획안 7장.
 *
 * <ul>
 *   <li>개발자가 답변을 등록한 때 — 피드백 작성자(회원)에게 메일. 수정 시에는 보내지 않는다.</li>
 *   <li>열람자가 우선 처리를 요청한 때 — 개발자 전원에게 메일. 해제할 때는 보내지 않는다.</li>
 * </ul>
 *
 * <p>비회원은 이메일이 없으므로 보내지 않는다. 본문에는 제목과 조회 링크만 담고 내용 전문은
 * 넣지 않는다. <b>발송 실패(또는 메일 설정 자체가 없는 로컬 개발 환경)는 기록만 남기고 요청은
 * 그대로 성공 처리한다</b> — 알림이 피드백 처리를 막아서는 안 된다.
 *
 * <p>{@code spring.mail.host}를 설정하지 않으면 {@link JavaMailSender} 빈 자체가 안 만들어진다
 * (Spring Boot 메일 자동 설정 조건). 그래서 팀원 전원이 로컬에서 SMTP 계정 없이도 그냥
 * 기동할 수 있도록 {@link ObjectProvider}로 선택적으로 주입받는다 — 없으면 보내는 시도조차
 * 하지 않고 로그만 남긴다.
 */
@Slf4j
@Service
public class NotificationService {

	private final ObjectProvider<JavaMailSender> mailSenders;
	private final String mailFrom;
	private final String frontendUrl;

	public NotificationService(
			ObjectProvider<JavaMailSender> mailSenders,
			@Value("${app.mail-from:}") String mailFrom,
			@Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
		this.mailSenders = mailSenders;
		this.mailFrom = mailFrom;
		this.frontendUrl = frontendUrl;
	}

	/** 답변이 처음 등록됐을 때만 호출한다(수정 시에는 호출하지 않는다 — 호출하는 쪽의 책임). */
	public void notifyAnswerRegistered(Feedback feedback) {
		User author = feedback.getAuthor();
		if (author == null || author.getEmail() == null || author.getEmail().isBlank()) {
			return; // 비회원이거나 이메일이 없는 계정
		}

		String link = frontendUrl + "/my/" + feedback.getId();
		send(author.getEmail(),
				"[피드백 게시판] \"" + feedback.getTitle() + "\"에 답변이 등록되었습니다",
				"등록하신 피드백에 답변이 달렸습니다.\n\n" + feedback.getTitle() + "\n" + link);
	}

	/** 우선 처리 요청이 켜졌을 때만 호출한다(해제 시에는 호출하지 않는다 — 호출하는 쪽의 책임). */
	public void notifyPriorityRequested(Feedback feedback, List<User> developers) {
		if (developers.isEmpty()) {
			return;
		}

		String link = frontendUrl + "/admin/" + feedback.getProject().getCode() + "/board";
		String subject = "[피드백 게시판] 우선 처리 요청 — " + feedback.getTitle();
		String body = "우선 처리가 요청된 피드백이 있습니다.\n\n" + feedback.getTitle() + "\n" + link;

		for (User developer : developers) {
			if (developer.getEmail() != null && !developer.getEmail().isBlank()) {
				send(developer.getEmail(), subject, body);
			}
		}
	}

	private void send(String to, String subject, String body) {
		JavaMailSender mailSender = mailSenders.getIfAvailable();
		if (mailSender == null) {
			log.info("메일 설정이 없어 발송을 생략합니다. to={}, subject={}", to, subject);
			return;
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			if (!mailFrom.isBlank()) {
				message.setFrom(mailFrom);
			}
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
		} catch (RuntimeException e) {
			// 발송 실패는 기록만 남기고 요청 자체는 성공 처리한다(기획안 7장).
			log.warn("메일 발송에 실패했습니다. to={}, subject={}", to, subject, e);
		}
	}
}
