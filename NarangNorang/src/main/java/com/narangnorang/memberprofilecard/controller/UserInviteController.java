package com.narangnorang.memberprofilecard.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.narangnorang.auth.config.MyUserDetails;
import com.narangnorang.common.ApiResponse;
import com.narangnorang.memberprofilecard.dto.response.InviteSpaceResponseDto;
import com.narangnorang.memberprofilecard.service.InviteSpaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/invites")
@RequiredArgsConstructor
public class UserInviteController {

	private final InviteSpaceService inviteSpaceService;

	// 유저 신청 대기 목록 조회
    @GetMapping
    public ApiResponse<List<InviteSpaceResponseDto>> getMyInvites(@AuthenticationPrincipal MyUserDetails userDetails) {
        ApiResponse<List<InviteSpaceResponseDto>> response = new ApiResponse<>();
        response.setSuccess(inviteSpaceService.getInvites(userDetails.getId()));
        return response;
    }
}
