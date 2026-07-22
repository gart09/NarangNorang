package com.narangnorang.user.dto.response;

import com.narangnorang.user.dto.request.UserRequestDto;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
	private Long id;
	private String name;
	private String email;
	private String password;

	private List<String> userRoles;

	public static UserResponseDto from(User user) {
		List<String> strRoles = null;
		if (user.getUserRoles() != null) {
			strRoles = user.getUserRoles().stream()
					.map(UserRole::getName)
					.collect(Collectors.toList());
		}

		return new UserResponseDto(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getPassword(),
				strRoles
		);
	}

	public User toEntity(List<UserRole> userRoles) {
		return User.builder()
				.id(this.id)
				.name(this.name)
				.email(this.email)
				.password(this.password)
				.userRoles(userRoles)
				.build();
	}
}
