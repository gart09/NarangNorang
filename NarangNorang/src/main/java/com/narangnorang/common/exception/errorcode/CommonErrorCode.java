package com.narangnorang.common.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode{

	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-001", "지원하지 않는 HTTP 메서드입니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-002", "입력값"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"COMMON-003","알 수 없는 에러가 발생했습니다. 콘솔 로그를 참조하세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
