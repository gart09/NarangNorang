package com.narangnorang.invitespace.exception.errorcode;

import org.springframework.http.HttpStatus;

import com.narangnorang.common.exception.errorcode.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InviteErrorCode implements ErrorCode {

    SPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITE-001", "존재하지 않는 스페이스입니다."),
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITE-002", "존재하지 않는 신청/초대입니다."),
    NOT_ROOM_MEMBER(HttpStatus.FORBIDDEN, "INVITE-003", "룸 멤버만 가능한 작업입니다."),
    ALREADY_SPACE_MEMBER(HttpStatus.BAD_REQUEST, "INVITE-004", "이미 스페이스 멤버입니다."),
    ALREADY_PENDING_INVITE(HttpStatus.BAD_REQUEST, "INVITE-005", "이미 처리 대기 중인 신청/초대가 있습니다."),
    NOT_SPACE_OWNER(HttpStatus.FORBIDDEN, "INVITE-006", "스페이스 오너만 가능한 작업입니다."),
    NOT_APPROVER(HttpStatus.FORBIDDEN, "INVITE-007", "본인에게 온 요청만 처리할 수 있습니다."),
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "INVITE-008", "이미 처리 완료된 가입/초대 요청입니다."),
    LEFT_ROOM(HttpStatus.BAD_REQUEST, "INVITE-009", "더 이상 룸 멤버가 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}