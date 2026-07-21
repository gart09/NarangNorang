package com.narangnorang.user.service;

import com.narangnorang.user.dto.UserDto;
import com.narangnorang.user.dto.UserResultDto;
import com.narangnorang.user.entity.User;

import java.util.Optional;

public interface UserService {
	Optional<User> findByEmail(String email);
	UserResultDto insertUser(UserDto userDto);
}
