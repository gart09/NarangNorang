package com.narangnorang.user.dto.request;

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
public class UserRequestDto {
	private String name;
	private String email;
	private String password;

	public User toEntity(List<UserRole> userRoles) {
		return User.builder()
				.name(this.name)
				.email(this.email)
				.password(this.password)
				.userRoles(userRoles)
				.build();
	}
	public User toEntity(String name,String password,List<UserRole> userRoles) {
		return User.builder()
				.name(this.name)
				.email(this.email)
				.password(this.password)
				.userRoles(userRoles)
				.build();
	}
}
