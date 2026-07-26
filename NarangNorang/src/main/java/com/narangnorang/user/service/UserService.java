package com.narangnorang.user.service;

import com.narangnorang.user.dto.request.UserRequestDto;
import com.narangnorang.user.dto.response.UserResponseDto;

public interface UserService {
	UserResponseDto findUserDetails(Long userId);
	
	boolean existsByEmail(String email);
	
	UserResponseDto insertUser(UserRequestDto userRequestDto);
	
	UserResponseDto updateUser(UserRequestDto userRequestDto, Long userId);
	
	void deleteUser(String email, Long userId);
}
