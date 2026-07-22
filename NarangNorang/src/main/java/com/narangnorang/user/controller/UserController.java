package com.narangnorang.user.controller;

import com.narangnorang.common.ApiResponse;
import com.narangnorang.user.dto.request.UserRequestDto;
import com.narangnorang.user.dto.response.UserResponseDto;
import com.narangnorang.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	public ApiResponse<UserResponseDto> insertUser(@RequestBody UserRequestDto userRequestDto) {
		return userService.insertUser(userRequestDto);
	}
}