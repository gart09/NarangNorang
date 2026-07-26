package com.narangnorang.memberprofilecard.exception;

import com.narangnorang.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InviteSpaceErrorCode implements ErrorCode {

	SPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITESPACE-001", "존재하지 않는 스페이스입니다."),
	USER_NOT_SPACE_OWNER(HttpStatus.FORBIDDEN, "INVITESPACE-002", "스페이스 오너만 가능한 작업입니다."),
	INVITE_NOT_FOUND(HttpStatus.NOT_FOUND,"INVITESPACE-003","존재하지 않는 신청/초대입니다."),
	NO_LONGER_ROOM_MEMBER(HttpStatus.FORBIDDEN, "INVITESPACE-004", "더 이상 룸 멤버가 아닙니다."),
	SPACE_MEMBER_DUPLICATED(HttpStatus.CONFLICT, "INVITESPACE-005", "이미 스페이스 멤버입니다."),
	USER_NOT_ROOM_MEMBER(HttpStatus.FORBIDDEN, "INVITESPACE-006", "룸 멤버만 가능한 작업입니다."),
	INVITE_DUPLICATED(HttpStatus.CONFLICT, "INVITESPACE-007", "이미 처리 대기 중인 신청/초대가 있습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
