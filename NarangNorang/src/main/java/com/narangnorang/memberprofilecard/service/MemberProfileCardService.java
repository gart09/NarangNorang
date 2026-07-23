package com.narangnorang.memberprofilecard.service;

import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardCreateRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardReadRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardUpdateRequestDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardCreateResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardReadResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardUpdateResponseDto;

import java.util.List;

public interface MemberProfileCardService {
	MemberProfileCardCreateResponseDto createMemberProfileCard(Long userId, MemberProfileCardCreateRequestDto memberProfileCardCreateRequestDto);
	List<MemberProfileCardReadResponseDto> findMemberProfileCard(Long userId, MemberProfileCardReadRequestDto memberProfileCardReadRequestDto);
	MemberProfileCardUpdateResponseDto updateMemberProfileCard(Long userId, MemberProfileCardUpdateRequestDto memberProfileCardUpdateRequestDto);
	void deleteMemberProfileCard(Long userId, Long memberProfileCardId);
}
