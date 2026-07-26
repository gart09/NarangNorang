package com.narangnorang.user.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.narangnorang.common.exception.BaseExceptionHandler;
import com.narangnorang.common.exception.dto.ErrorResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.narangnorang.User")
public class UserExceptionHandler extends BaseExceptionHandler {

	@ExceptionHandler(UserException.class)
	protected ResponseEntity<ErrorResponseDto> handleUserException(UserException e) {
		log.warn("UserException Occurred : {}", e.getMessage());
		return makeErrorResponse(e);
	}
}