package com.narangnorang.memberprofilecard.controller;


import com.narangnorang.auth.config.MyUserDetails;
import com.narangnorang.common.ApiResponse;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardCreateRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardReadRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardUpdateRequestDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardCreateResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardReadResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardUpdateResponseDto;
import com.narangnorang.memberprofilecard.service.MemberProfileCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memberProfileCard")
@RequiredArgsConstructor
public class MemberProfileCardController {
	private final MemberProfileCardService memberProfileCardService;

	@PostMapping("/create")
	public ApiResponse<MemberProfileCardCreateResponseDto> createMemberProfileCard(
			@RequestBody MemberProfileCardCreateRequestDto memberProfileCardRequestDto,
			@AuthenticationPrincipal MyUserDetails userDetails) {

		Long userId = userDetails.getId();
		MemberProfileCardCreateResponseDto responseDto = memberProfileCardService.createMemberProfileCard(userId, memberProfileCardRequestDto);
		return success(responseDto);
	}

	@GetMapping("/filter")
	public ApiResponse<List<MemberProfileCardReadResponseDto>> getMemberProfileCard(
			@RequestBody MemberProfileCardReadRequestDto requestDto,
			@AuthenticationPrincipal MyUserDetails userDetails){
		Long userId = userDetails.getId();
		List<MemberProfileCardReadResponseDto> responseDtoList = memberProfileCardService.findMemberProfileCard(userId, requestDto);
		return success(responseDtoList);
	}

	@PatchMapping("/update")
	public ApiResponse<MemberProfileCardUpdateResponseDto> updateMemberProfileCard(
			@RequestBody MemberProfileCardUpdateRequestDto requestDto,
			@AuthenticationPrincipal MyUserDetails userDetails){
		Long userId = userDetails.getId();
		MemberProfileCardUpdateResponseDto responseDto = memberProfileCardService.updateMemberProfileCard(userId, requestDto);
		return success(responseDto);
	}

	@DeleteMapping("/{memberProfileCardId}")
	public ApiResponse<Void> deleteMemberProfileCard(
			@PathVariable Long memberProfileCardId,
			@AuthenticationPrincipal MyUserDetails userDetails){
		Long userId = userDetails.getId();

		memberProfileCardService.deleteMemberProfileCard(userId, memberProfileCardId);
		return success(null);
	}



	private <T> ApiResponse<T> success(T result) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setSuccess(result);
		return response;
	}
}
