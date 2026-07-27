package com.narangnorang.chat.exception;

import com.narangnorang.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

	USER_NOT_PERMITTED(HttpStatus.FORBIDDEN, "CHAT-001", "해당 유저는 접근 권한이 없습니다. %s"),
	MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "CHAT-002", "Member를 찾을 수 없습니다. (송신자ID: %s)"),
	DB_ERROR_OCCURRED(HttpStatus.INTERNAL_SERVER_ERROR,"CHAT-003","DB 에러가 발생했습니다. 콘솔 로그를 참고하세요."),
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, "CHAT-004", "유효하지 않은 토큰입니다."),
	USER_NOT_FOUND_IN_SESSION(HttpStatus.NOT_FOUND, "CHAT-005", "세션에 유저 ID가 존재하지 않습니다."),
	INVALID_CHATROOM_TYPE(HttpStatus.BAD_REQUEST, "CHAT-006", "채팅방 번호 형식이 올바르지 않습니다. 입력된 값: %s"),
	INVALID_INPUT_TYPE(HttpStatus.BAD_REQUEST, "CHAT-007", "TargetType이 올바르지 않습니다. 입력된 값: %s");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
