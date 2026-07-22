package com.narangnorang.config;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// UserDetails 인터페이스의 3개 추상 메소드 구현은 나머지 필드와 함께 @Getter 로 처리
@Builder
@Getter
public class MyUserDetails implements UserDetails{

	private static final long serialVersionUID = 1L;

	private final String username;

	private final String password;

	private final Collection<? extends GrantedAuthority> authorities;


	//사용자 정의 필드

	private final Long id;
}










