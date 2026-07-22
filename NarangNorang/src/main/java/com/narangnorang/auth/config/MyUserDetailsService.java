package com.narangnorang.auth.config;


import com.narangnorang.user.entity.User;
import com.narangnorang.user.entity.UserRole;
import com.narangnorang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService{

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		Optional<User> optionalUser = userRepository.findByEmail(email);

		if( optionalUser.isPresent() ) {
			User user = optionalUser.get();
			List<UserRole> listUserRole = user.getUserRoles();

			// _5 기준 ListUserRole -> String[] roleStrArray 만들어서 아래 메소드를 통해 전달
			// 내부적으로 ROLE_CUSTOMER 형식으로 ROLE_ prefix 처리하고 내부적으로 GrandedAuthority Collection 처리
			// org.springframework.security.core.userdetails.User.builder()..roles(roleStrArray)
			// 이제 MyUserDetails 객체를 사용하므로 우리가 직접 GrandedAuthority Collection 을 만들어 한다.
			List<SimpleGrantedAuthority> authorities = listUserRole.stream()
					.map(UserRole::getName)
					.map(name -> "ROLE_" + name)
					.map(SimpleGrantedAuthority::new)
					.toList();
			return MyUserDetails.builder()
					.username(user.getEmail())  	// Spring Security 계약 필드
					.password(user.getPassword()) 	// Spring Security 계약 필드
					.authorities(authorities) 		// Spring Security 계약 필드
					.id(user.getId())
					.build();
		}

		throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
	}



}
