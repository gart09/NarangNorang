package com.narangnorang.common.exception;

import com.narangnorang.common.exception.dto.ErrorResponseDto;
import com.narangnorang.common.exception.errorcode.ErrorCode;
import org.springframework.http.ResponseEntity;

public abstract class BaseExceptionHandler {
	// 1. 비즈니스 예외용
	protected ResponseEntity<ErrorResponseDto> makeErrorResponse(BusinessException e){
		ErrorCode errorCode = e.getErrorCode();

		ErrorResponseDto responseDto = ErrorResponseDto.builder()
				.status(errorCode.getStatus().value())
				.code(errorCode.getCode())
				.message(e.getMessage())
				.build();
		return new ResponseEntity<>(responseDto, errorCode.getStatus());
	}

	// 2. 스프링 내장 예외나 DB 예외용
	protected ResponseEntity<ErrorResponseDto> makeErrorResponse(ErrorCode errorCode) {
		ErrorResponseDto response = ErrorResponseDto.builder()
				.status(errorCode.getStatus().value())
				.code(errorCode.getCode())
				.message(errorCode.getMessage())
				.build();
		return new ResponseEntity<>(response, errorCode.getStatus());
	}
}
