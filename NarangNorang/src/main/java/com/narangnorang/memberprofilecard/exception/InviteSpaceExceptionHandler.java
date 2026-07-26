package com.narangnorang.memberprofilecard.exception;

import com.narangnorang.common.exception.BaseExceptionHandler;
import com.narangnorang.common.exception.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.narangnorang.memberprofilecard")
public class InviteSpaceExceptionHandler extends BaseExceptionHandler {

	@ExceptionHandler(InviteSpaceException.class)
	protected ResponseEntity<ErrorResponseDto> handleInviteSpaceException(InviteSpaceException e) {
		log.warn("InviteSpaceException Occurred : {}", e.getMessage());
		return makeErrorResponse(e);
	}
}
