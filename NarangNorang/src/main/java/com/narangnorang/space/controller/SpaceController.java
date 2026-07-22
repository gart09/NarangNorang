package com.narangnorang.space.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.narangnorang.auth.config.MyUserDetails;
import com.narangnorang.common.ApiResponse;
import com.narangnorang.space.dto.request.SpaceCreateRequestDto;
import com.narangnorang.space.dto.request.SpaceUpdateRequestDto;
import com.narangnorang.space.dto.response.SpaceProfileCardResponseDto;
import com.narangnorang.space.dto.response.SpaceSummaryResponseDto;
import com.narangnorang.space.service.SpaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rooms/{roomId}/spaces")
@RequiredArgsConstructor
public class SpaceController {

	private final SpaceService spaceService;

	// 스페이스 목록 조회 (태그 필터링 옵션)
	@GetMapping
	public ApiResponse<List<SpaceSummaryResponseDto>> getSpaceList(
			@PathVariable("roomId") Long roomId,
			@RequestParam(required = false) List<String> tags) {

		ApiResponse<List<SpaceSummaryResponseDto>> response = new ApiResponse<>();
		response.setSuccess(spaceService.getSpaceList(roomId, tags));
		return response;
	}

	// 스페이스 상세 조회 (프로필 카드, 태그 포함)
	@GetMapping("/{spaceId}")
	public ApiResponse<SpaceProfileCardResponseDto> getSpaceDetail(
			@PathVariable("roomId") Long roomId,
			@PathVariable("spaceId") Long spaceId) {

		ApiResponse<SpaceProfileCardResponseDto> response = new ApiResponse<>();
		response.setSuccess(spaceService.getSpaceDetail(spaceId));
		return response;
	}

	// 스페이스 생성
	@PostMapping
	public ApiResponse<SpaceProfileCardResponseDto> createSpace(
			@PathVariable("roomId") Long roomId,
			@AuthenticationPrincipal MyUserDetails userDetails,
			@RequestBody SpaceCreateRequestDto spaceCreateRequestDto) {

		Long ownerId = userDetails.getId();

		ApiResponse<SpaceProfileCardResponseDto> response = new ApiResponse<>();
		response.setSuccess(spaceService.createSpace(roomId, ownerId, spaceCreateRequestDto));
		return response;
	}

	// 스페이스 수정 (기본 정보 + 프로필 카드 + 태그)
	@PatchMapping("/{spaceId}")
	public ApiResponse<SpaceProfileCardResponseDto> updateSpace(
			@PathVariable("roomId") Long roomId,
			@PathVariable("spaceId") Long spaceId,
			@AuthenticationPrincipal MyUserDetails userDetails,
			@RequestBody SpaceUpdateRequestDto spaceUpdateRequestDto) {

		Long userId = userDetails.getId();

		ApiResponse<SpaceProfileCardResponseDto> response = new ApiResponse<>();
		response.setSuccess(spaceService.updateSpaceCard(spaceId, userId, spaceUpdateRequestDto));
		return response;
	}

	// 스페이스 삭제 (스페이스 + 프로필 카드 + 태그 + 스페이스 멤버 함께 삭제)
	@DeleteMapping("/{spaceId}")
	public ApiResponse<Void> deleteSpace(
			@PathVariable("roomId") Long roomId,
			@PathVariable("spaceId") Long spaceId,
			@AuthenticationPrincipal MyUserDetails userDetails) {

		Long userId = userDetails.getId();

		spaceService.deleteSpace(spaceId, userId);

		ApiResponse<Void> response = new ApiResponse<>();
		response.setSuccess(null);
		return response;
	}
}