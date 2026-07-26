package com.narangnorang.space.exception;

import com.narangnorang.common.exception.BusinessException;
import com.narangnorang.common.exception.errorcode.ErrorCode;

public class SpaceException extends BusinessException {

    public SpaceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SpaceException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}