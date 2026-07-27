package com.narangnorang.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.narangnorang.auth.config.MyUserDetails;
import com.narangnorang.auth.dto.request.LoginRequestDto;
import com.narangnorang.auth.dto.response.LoginResponseDto;
import com.narangnorang.auth.service.LoginService;
import com.narangnorang.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = loginService.login(loginRequestDto);
        ApiResponse<LoginResponseDto> apiResponse = new ApiResponse<>();
        if(loginResponseDto == null){
            apiResponse.setFail("로그인 실패");
            return apiResponse;
        }
        apiResponse.setSuccess(loginResponseDto);
        return apiResponse;
    }

    @PostMapping("/checkRefreshToken")
    public ApiResponse<LoginResponseDto> checkRefreshToken(String refreshToken){
        LoginResponseDto loginResponseDto = loginService.checkRefreshToken(refreshToken);
        ApiResponse<LoginResponseDto> apiResponse = new ApiResponse<>();
        if(loginResponseDto == null){
            apiResponse.setFail("리프레시토큰 체크 실패");
            return apiResponse;
        }
        apiResponse.setSuccess(loginResponseDto);
        return apiResponse;
    }
    
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal MyUserDetails userDetails) {
    	loginService.logout(userDetails.getId());

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(null);
        return apiResponse;
    }
}
