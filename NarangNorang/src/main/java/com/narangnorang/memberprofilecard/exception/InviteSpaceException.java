package com.narangnorang.memberprofilecard.exception;

import com.narangnorang.common.exception.BusinessException;
import com.narangnorang.common.exception.errorcode.ErrorCode;

public class InviteSpaceException extends BusinessException {

	public InviteSpaceException(ErrorCode errorCode){
		super(errorCode);
	}
}
