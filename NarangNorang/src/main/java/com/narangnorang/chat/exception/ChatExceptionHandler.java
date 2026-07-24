package com.narangnorang.chat.exception;

import com.narangnorang.common.exception.BaseExceptionHandler;
import com.narangnorang.common.exception.dto.ErrorResponseDto;
import com.narangnorang.common.exception.errorcode.CommonErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.narangnorang.chat")
public class ChatExceptionHandler extends BaseExceptionHandler {

	@ExceptionHandler(ChatException.class)
	protected ResponseEntity<ErrorResponseDto> handleChatException(ChatException e) {
		log.warn("ChatException Occurred : {}", e.getMessage());
		return makeErrorResponse(e);
	}

	@ExceptionHandler(DataAccessException.class)
	protected ResponseEntity<ErrorResponseDto> handleDataAccessException(DataAccessException e) {
		log.error("채팅 도메인 DB 오류 발생 : ", e);

		return makeErrorResponse(ChatErrorCode.DB_ERROR_OCCURRED);
	}
}
