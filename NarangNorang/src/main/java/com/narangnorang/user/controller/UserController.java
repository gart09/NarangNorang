package com.narangnorang.user.controller;

import com.narangnorang.user.dto.UserDto;
import com.narangnorang.user.dto.UserResultDto;
import com.narangnorang.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	public UserResultDto insertUser(UserDto userDto) {
		return userService.insertUser(userDto);
	}
}