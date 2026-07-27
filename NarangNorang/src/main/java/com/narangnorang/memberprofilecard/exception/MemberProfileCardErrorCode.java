package com.narangnorang.memberprofilecard.exception;

import com.narangnorang.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberProfileCardErrorCode implements ErrorCode {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-001", "존재하지 않는 유저입니다."),
	ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-002", "존재하지 않는 방입니다."),
	MEMBER_PROFILE_CARD_DUPLICATED(HttpStatus.CONFLICT,"MEMBER-003","이미 존재하는 프로필카드입니다."),
	MEMBER_PROFILE_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-004", "해당 프로필 카드가 존재하지 않습니다."),
	USER_NOT_PERMITTED(HttpStatus.FORBIDDEN, "MEMBER-005", "해당 유저는 해당 멤버프로필카드에 대한 권한이 없습니다."),
	REQUIRED_FIELD_MISS(HttpStatus.BAD_REQUEST, "MEMBER-006", "필수 입력 항목이 누락됐습니다. %s"),
	REQUIRED_FIELD_SPACE(HttpStatus.BAD_REQUEST, "MEMBER-007", "필수 항목은 빈 칸 혹은 공백이 될 수 없습니다. %s"),
	CANT_SELECT_FIELD(HttpStatus.BAD_REQUEST, "MEMBER-008", "선택 가능하지 않은 항목입니다. %s"),
	SPACE_OWNER_CANT_DELETE(HttpStatus.BAD_REQUEST, "MEMBER-009", "스페이스 오너는 룸 탈퇴를 할 수 없습니다. 위임 후 탈퇴해주세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
