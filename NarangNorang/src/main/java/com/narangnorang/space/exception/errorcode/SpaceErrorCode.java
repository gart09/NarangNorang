package com.narangnorang.space.exception.errorcode;

import org.springframework.http.HttpStatus;

import com.narangnorang.common.exception.errorcode.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpaceErrorCode implements ErrorCode {

    SPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "SPACE-001", "존재하지 않는 스페이스입니다."),
    SPACE_ROOM_MISMATCH(HttpStatus.BAD_REQUEST, "SPACE-002", "해당 룸의 스페이스가 아닙니다."),
    NOT_ROOM_MEMBER(HttpStatus.FORBIDDEN, "SPACE-003", "룸 멤버만 이용할 수 있습니다."),
    NOT_SPACE_MEMBER(HttpStatus.FORBIDDEN, "SPACE-004", "이 스페이스의 멤버가 아닙니다."),
    OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "SPACE-005", "오너는 탈퇴할 수 없습니다. 먼저 오너를 위임해주세요."),
    ALREADY_OWNER(HttpStatus.BAD_REQUEST, "SPACE-006", "이미 오너입니다."),
    NO_MANAGE_PERMISSION(HttpStatus.FORBIDDEN, "SPACE-007", "스페이스 오너 또는 룸 오너만 수정하거나 삭제할 수 있습니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "SPACE-008", "존재하지 않는 룸입니다.");  // 임시. 추후 RoomErrorCode 생기면 이관

    private final HttpStatus status;
    private final String code;
    private final String message;
}