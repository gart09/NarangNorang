package com.narangnorang.common.querydsl;

import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardReadRequestDto;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;

import java.util.List;

public interface MemberProfileCardQuerydslRepository {
	List<MemberProfileCard> searchMemberProfileCards(MemberProfileCardReadRequestDto requestDto);
}
