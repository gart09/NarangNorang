package com.narangnorang.memberprofilecard.exception;

import com.narangnorang.common.exception.BusinessException;
import com.narangnorang.common.exception.errorcode.ErrorCode;

public class MemberProfileCardException extends BusinessException {

	public MemberProfileCardException(ErrorCode errorCode){
		super(errorCode);
	}

	public MemberProfileCardException(ErrorCode errorCode, Object... args) {
		super(errorCode, args);
	}
}
