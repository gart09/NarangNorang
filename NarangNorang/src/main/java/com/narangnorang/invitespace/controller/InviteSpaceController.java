package com.narangnorang.invitespace.controller;

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
import com.narangnorang.invitespace.dto.request.InviteSpaceRequestDto;
import com.narangnorang.invitespace.dto.response.InviteSpaceResponseDto;
import com.narangnorang.invitespace.service.InviteSpaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
public class InviteSpaceController {

    private final InviteSpaceService inviteSpaceService;

    // 멤버 -> 오너 지원
    @PostMapping("/apply")
    public ApiResponse<Void> apply(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody InviteSpaceRequestDto requestDto) {

        inviteSpaceService.applyToSpace(requestDto.getSpaceId(), userDetails.getId());

        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(null);
        return response;
    }

    // 오너-> 멤버 권유
    @PostMapping("/invite")
    public ApiResponse<Void> invite(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody InviteSpaceRequestDto requestDto) {

        inviteSpaceService.inviteToSpace(requestDto.getSpaceId(), userDetails.getId(), requestDto.getTargetUserId());

        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(null);
        return response;
    }

    // 스페이스 요청 대기 목록
    @GetMapping("/spaces/{spaceId}")
    public ApiResponse<List<InviteSpaceResponseDto>> getPendingInvites(
            @PathVariable("spaceId") Long spaceId,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        ApiResponse<List<InviteSpaceResponseDto>> response = new ApiResponse<>();
        response.setSuccess(inviteSpaceService.getPendingInvites(spaceId, userDetails.getId()));
        return response;
    }
    
    // 유저 요청 대기 목록
    @GetMapping("/members")
    public ApiResponse<List<InviteSpaceResponseDto>> getInvites(
            @AuthenticationPrincipal MyUserDetails userDetails) {

        ApiResponse<List<InviteSpaceResponseDto>> response = new ApiResponse<>();
        response.setSuccess(inviteSpaceService.getInvites(userDetails.getId()));
        return response;
    }

    // 수락
    @PostMapping("/{inviteId}/accept")
    public ApiResponse<Void> accept(
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
            @PathVariable("inviteId") Long inviteId,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        inviteSpaceService.rejectInvite(inviteId, userDetails.getId());

        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(null);
        return response;
    }
}