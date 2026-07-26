package com.narangnorang.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.narangnorang.auth.config.MyUserDetails;
import com.narangnorang.common.ApiResponse;
import com.narangnorang.user.dto.request.UserRequestDto;
import com.narangnorang.user.dto.response.UserResponseDto;
import com.narangnorang.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	public ApiResponse<UserResponseDto> insertUser(@RequestBody UserRequestDto userRequestDto) {
		UserResponseDto userResponseDto = userService.insertUser(userRequestDto);
		ApiResponse<UserResponseDto> apiResponse = new ApiResponse<>();
		if(userRequestDto == null){
			apiResponse.setFail("유저 등록 실패");
			return apiResponse;
		}
		apiResponse.setSuccess(userResponseDto);
		return apiResponse;
	}
	
	
	@GetMapping("/details")
	public ApiResponse<UserResponseDto> findUserDetails(
	        @AuthenticationPrincipal MyUserDetails userDetails) {

	    UserResponseDto result = userService.findUserDetails(userDetails.getId());

	    ApiResponse<UserResponseDto> response = new ApiResponse<>();
	    response.setSuccess(result);
	    return response;
	}
	
	@PatchMapping
	public ApiResponse<UserResponseDto> updateUser(
	        @RequestBody UserRequestDto userRequestDto,
	        @AuthenticationPrincipal MyUserDetails userDetails) {

	    UserResponseDto result = userService.updateUser(userRequestDto, userDetails.getId());

	    ApiResponse<UserResponseDto> response = new ApiResponse<>();
	    response.setSuccess(result);
	    return response;
	}

	@DeleteMapping
	public ApiResponse<Void> deleteUser(
			@RequestParam("email") String email,
	        @AuthenticationPrincipal MyUserDetails userDetails) {

	    userService.deleteUser(email, userDetails.getId());

	    ApiResponse<Void> response = new ApiResponse<>();
	    response.setSuccess(null);
	    return response;
	}

}