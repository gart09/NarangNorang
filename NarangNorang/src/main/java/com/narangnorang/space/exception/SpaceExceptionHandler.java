package com.narangnorang.space.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.narangnorang.common.exception.BaseExceptionHandler;
import com.narangnorang.common.exception.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.narangnorang.space")
public class SpaceExceptionHandler extends BaseExceptionHandler {

	@ExceptionHandler(SpaceException.class)
	protected ResponseEntity<ErrorResponseDto> handleSpaceException(SpaceException e) {
		log.warn("SpaceException Occurred : {}", e.getMessage());
		return makeErrorResponse(e);
	}
}