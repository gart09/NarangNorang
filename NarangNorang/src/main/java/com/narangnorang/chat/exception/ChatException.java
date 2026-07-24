package com.narangnorang.chat.exception;

import com.narangnorang.common.exception.BusinessException;
import com.narangnorang.common.exception.errorcode.ErrorCode;

public class ChatException extends BusinessException {

	public ChatException(ErrorCode errorCode){
		super(errorCode);
	}

	public ChatException(ErrorCode errorCode, Object... args) {
		super(errorCode, args);
	}
}
