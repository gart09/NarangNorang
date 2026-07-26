package com.narangnorang.user.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.narangnorang.common.exception.BaseExceptionHandler;
import com.narangnorang.common.exception.dto.ErrorResponseDto;
import com.narangnorang.invitespace.exception.InviteSpaceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.narangnorang.User")
public class UserExceptionHandler extends BaseExceptionHandler {

	@ExceptionHandler(InviteSpaceException.class)
	protected ResponseEntity<ErrorResponseDto> handleInviteSpaceException(UserException e) {
		log.warn("InviteSpaceException Occurred : {}", e.getMessage());
		return makeErrorResponse(e);
	}
}