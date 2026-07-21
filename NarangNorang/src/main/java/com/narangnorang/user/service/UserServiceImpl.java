package com.narangnorang.user.service;

import com.narangnorang.user.dto.UserDto;
import com.narangnorang.user.dto.UserResultDto;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.entity.UserRole;
import com.narangnorang.user.repository.UserRepository;
import com.narangnorang.user.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;

	// 사용자 입력 패스워드 (일반 텍스트) 암호화 후 저장
	// 내맘대로 암호화가 아니라 현재 프로젝트에 설정된 암호화 객체를 이용
	private final PasswordEncoder passwordEncoder;


	@Override
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}



	@Override
	@Transactional
	public UserResultDto insertUser(UserDto userDto) {
		UserResultDto userResultDto = new UserResultDto();
		try {
			List<UserRole> userRoles = List.of(userRoleRepository.findByName("NORMAL"));


			// 이메일 중복 검사 로직
			if(userRepository.existsByEmail(userDto.getEmail())){
				userResultDto.setResult("duplicatedEmail");
				return userResultDto;
			}

			User user = User.builder()
					.name(userDto.getName())
					.email(userDto.getEmail())
					.password(passwordEncoder.encode(userDto.getPassword()))
					.userRoles(userRoles)
					.build();

			User savedUser = userRepository.save(user); // 영속화된 savedUser 리턴
			UserDto dto = UserDto.from(savedUser);
			userResultDto.setResult("success");
			userResultDto.setUserDto(dto);

		} catch(Exception e) {
			e.printStackTrace();
			// 현재 insert 후 예외가 발생할 확률 없으나 습관. 패턴 기준으로 rollback 처리
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			userResultDto.setResult("fail");
		}
		return userResultDto;
	}
}
