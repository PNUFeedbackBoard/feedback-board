package kr.ac.pusan.feedback.admin.dto;

import java.util.List;

/** 일괄 변경이 모두 반영된 피드백 번호. 성공한 경우에만 응답된다. */
public record FeedbackBulkUpdateResponse(List<Long> updatedIds) {
}
