package com.narangnorang.common.exception;

import com.narangnorang.common.exception.dto.ErrorResponseDto;
import com.narangnorang.common.exception.errorcode.CommonErrorCode;
import com.narangnorang.common.exception.errorcode.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 모든 예외를 GlobalExceptionHandler에서 통합 처리할 수 있음에도,
 * 도메인별로 전역 핸들러를 분리하여 관리하는 이유는 다음과 같습니다.
 *
 * 1. 도메인 특화 처리 대비
 *    - 향후 채팅 서비스에서 발생하는 특정 에러(예: 결제 연동 실패, 보안 이슈 등)에 대해
 *      Slack 긴급 알림 전송, 별도의 감사 로그 기록 등 도메인만의 특수한 요구사항이
 *      추가될 때를 위한 확장 포인트입니다.
 *
 * 2. 높은 응집도 및 MSA 전환 고려
 *    - 각 도메인과 관련된 에러 정의(Enum), 예외(Exception), 그리고 처리(Handler) 로직을
 *      하나의 패키지 내에 모아두어 응집도를 높입니다.
 *    - 추후 트래픽 증가로 인해 채팅 도메인이 별도의 MSA로 분리되더라도
 *      코드 수정 없이 패키지째로 쉽게 분리할 수 있습니다.
 *
 * 3. 협업 시 병합 충돌 방지
 *    - 모든 개발자가 GlobalExceptionHandler 하나의 파일을 수정하는 것을 방지하여,
 *      다수 개발자가 각자의 도메인을 개발할 때 발생하는 깃 충돌을 최소화합니다.
 *
 * @implNote 현재는 부모 클래스의 makeErrorResponse()를 통해 공통 응답 규격만 내려주고 있으나,
 *           도메인 특화 예외 처리 로직이 필요해지면 해당 핸들러 메서드 내부에 추가해 주세요.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler extends BaseExceptionHandler{

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e) {
		log.warn("Validation Error : {}", e.getMessage());
		return makeErrorResponse(CommonErrorCode.INVALID_INPUT_VALUE);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<ErrorResponseDto> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
		log.warn("Method Not Supported : {}", e.getMessage());
		return makeErrorResponse(CommonErrorCode.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponseDto> handleException(Exception e) {
		log.error("Unhandled Exception : ", e);
		return makeErrorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
	}
}
