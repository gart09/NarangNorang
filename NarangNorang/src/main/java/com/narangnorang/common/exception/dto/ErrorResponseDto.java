package com.narangnorang.common.exception.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ErrorResponseDto {
	private int status;
	private String code;
	private String message;


	//회원가입 시 이메일 양식, 비밀번호 양식, 룸 / 스페이스 / 멤버 관련 로직 처리 시 유효한 입력값이 아닐 때 사용할 항목들입니다. 미리 만들어뒀어요.
	private final List<ValidationError> errors;

	@Getter
	@Builder
	public static class ValidationError {
		private final String field;
		private final String message;
	}
}
