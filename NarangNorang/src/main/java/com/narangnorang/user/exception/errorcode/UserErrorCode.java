package com.narangnorang.user.exception.errorcode;

import org.springframework.http.HttpStatus;

import com.narangnorang.common.exception.errorcode.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "존재하지 않는 유저입니다."),
    NO_PERMISSION(HttpStatus.FORBIDDEN, "USER-002", "변경 권한이 없습니다."),
    SPACE_OWNER_EXISTS(HttpStatus.CONFLICT, "USER-003", "스페이스 오너입니다. 스페이스를 삭제하거나 다른 멤버에게 오너를 위임한 후 회원 탈퇴할 수 있습니다."),
    ROOM_OWNER_EXISTS(HttpStatus.CONFLICT, "USER-004", "룸 오너입니다. 룸을 삭제한 후 회원 탈퇴할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
