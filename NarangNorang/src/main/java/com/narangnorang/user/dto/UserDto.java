package com.narangnorang.user.dto;

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
public class UserDto {
	private Long id;
	private String name;
	private String email;
	private String password;

	private List<String> userRoles;

	public static UserDto toDto(User user) {
		List<String> strRoles = null;
		if (user.getUserRoles() != null) {
			strRoles = user.getUserRoles().stream()
					.map(userRole -> userRole.getName())
					.collect(Collectors.toList());
		}

		return new UserDto(
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
