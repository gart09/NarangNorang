package com.narangnorang.memberprofilecard.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.narangnorang.memberprofilecard.dto.request.InviteSpaceRequestDto;
import com.narangnorang.memberprofilecard.dto.response.InviteSpaceResponseDto;
import com.narangnorang.memberprofilecard.service.InviteSpaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rooms/{roomId}/spaces/{spaceId}/invites")
@RequiredArgsConstructor
public class SpaceInviteController {

    private final InviteSpaceService inviteSpaceService;

    // space 신청/권유
    @PostMapping
    public ApiResponse<Void> createInvite(
            @PathVariable("roomId") Long roomId,
            @PathVariable("spaceId") Long spaceId,
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody InviteSpaceRequestDto requestDto) {

        if (requestDto.getTargetUserId() == null) {
            inviteSpaceService.applyToSpace(spaceId, userDetails.getId());
        } else {
            inviteSpaceService.inviteToSpace(spaceId, userDetails.getId(), requestDto.getTargetUserId());
        }

        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(null);
        return response;
    }

    // 스페이스 신청 대기 목록 조회
    @GetMapping
    public ApiResponse<List<InviteSpaceResponseDto>> getPendingInvites(
            @PathVariable("roomId") Long roomId,
            @PathVariable("spaceId") Long spaceId) {

        ApiResponse<List<InviteSpaceResponseDto>> response = new ApiResponse<>();
        response.setSuccess(inviteSpaceService.getPendingInvites(spaceId));
        return response;
    }
    
    // 수락
    @PostMapping("/{inviteId}/accept")
    public ApiResponse<Void> accept(
            @PathVariable("roomId") Long roomId,
            @PathVariable("spaceId") Long spaceId,
            @PathVariable("inviteId") Long inviteId,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        inviteSpaceService.acceptInvite(inviteId, userDetails.getId());

        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(null);
        return response;
    }

    // 거절
    @PostMapping("/{inviteId}/reject")
    public ApiResponse<Void> reject(
            @PathVariable("roomId") Long roomId,
            @PathVariable("spaceId") Long spaceId,
            @PathVariable("inviteId") Long inviteId,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        inviteSpaceService.rejectInvite(inviteId, userDetails.getId());

        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(null);
        return response;
    }
}