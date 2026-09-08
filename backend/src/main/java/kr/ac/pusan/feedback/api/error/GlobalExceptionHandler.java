package kr.ac.pusan.feedback.api.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		List<ApiErrorResponse.FieldErrorDetail> details = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::toFieldError)
				.collect(Collectors.toList());

		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값을 확인해 주세요.", request, details);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	ResponseEntity<ApiErrorResponse> handleMethodValidation(
			HandlerMethodValidationException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값을 확인해 주세요.", request, List.of());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY", "요청 본문을 읽을 수 없습니다.", request, List.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ApiErrorResponse> handleNotFound(
			NoResourceFoundException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", request, List.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ApiErrorResponse> handleConflict(
			DataIntegrityViolationException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.CONFLICT, "DATA_CONFLICT", "요청이 현재 데이터 상태와 충돌합니다.", request, List.of());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiErrorResponse> handleUnexpected(
			Exception exception,
			HttpServletRequest request
	) {
		log.error("Unhandled API exception: {}", request.getRequestURI(), exception);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버에서 오류가 발생했습니다.", request, List.of());
	}

	private ResponseEntity<ApiErrorResponse> error(
			HttpStatus status,
			String code,
			String message,
			HttpServletRequest request,
			List<ApiErrorResponse.FieldErrorDetail> details
	) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				code,
				message,
				request.getRequestURI(),
				details
		);
		return ResponseEntity.status(status).body(response);
	}

	private ApiErrorResponse.FieldErrorDetail toFieldError(FieldError error) {
		return new ApiErrorResponse.FieldErrorDetail(error.getField(), error.getDefaultMessage());
	}
}
