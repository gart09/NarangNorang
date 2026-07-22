package com.narangnorang.user.service;

import com.narangnorang.common.ApiResponse;
import com.narangnorang.user.dto.request.UserRequestDto;
import com.narangnorang.user.dto.response.UserResponseDto;
import com.narangnorang.user.entity.User;

import java.util.Optional;

public interface UserService {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	UserResponseDto insertUser(UserRequestDto userRequestDto);
}
