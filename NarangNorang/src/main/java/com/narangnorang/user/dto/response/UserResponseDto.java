package com.narangnorang.user.dto.response;

import com.narangnorang.user.dto.request.UserRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
	private String result;
	private UserRequestDto userRequestDto;
}
