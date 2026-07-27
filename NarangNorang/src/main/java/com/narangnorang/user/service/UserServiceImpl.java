package com.narangnorang.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.narangnorang.user.dto.request.UserRequestDto;
import com.narangnorang.user.dto.response.UserResponseDto;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.entity.UserRole;
import com.narangnorang.user.exception.UserException;
import com.narangnorang.user.exception.errorcode.UserErrorCode;
import com.narangnorang.user.repository.UserRepository;
import com.narangnorang.user.repository.UserRoleRepository;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.space.repository.SpaceRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final MemberProfileCardRepository memberProfileCardRepository;
	private final SpaceRepository spaceRepository;
	private final RoomRepository roomRepository;

	// 사용자 입력 패스워드 (일반 텍스트) 암호화 후 저장
	// 내맘대로 암호화가 아니라 현재 프로젝트에 설정된 암호화 객체를 이용
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserResponseDto insertUser(UserRequestDto userRequestDto) {
		try {
			List<UserRole> userRoles = List.of(userRoleRepository.findByName("NORMAL"));


			// 이메일 중복 검사 로직
			if(userRepository.existsByEmail(userRequestDto.getEmail())){
				log.info("이메일이 중복되었습니다.");
				return null;
			}

			User user = User.builder()
					.name(userRequestDto.getName())
					.email(userRequestDto.getEmail())
					.password(passwordEncoder.encode(userRequestDto.getPassword()))
					.userRoles(userRoles)
					.build();

			User savedUser = userRepository.save(user);
			UserResponseDto userResponseDto = UserResponseDto.from(savedUser);
			return userResponseDto;
		} catch(Exception e) {
			e.printStackTrace();
			// 현재 insert 후 예외가 발생할 확률 없으나 습관. 패턴 기준으로 rollback 처리
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.info("Exception Occurred");
			return null;
		}
	}
	
	
	@Override
	public UserResponseDto findUserDetails(Long userId) {
		User user = getUser(userId);
		UserResponseDto userResponseDto = UserResponseDto.from(user);
		return userResponseDto;
	}

	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}
	
	@Override
	@Transactional
	public UserResponseDto updateUser(UserRequestDto userRequestDto, Long userId) {

	    User user = getUser(userId);
	    validateOwner(user, userRequestDto.getEmail());

	    String password = resolvePassword(userRequestDto.getPassword(), user.getPassword());
	    String name = resolveName(userRequestDto.getName(), user.getName());

	    user.updateName(name);
	    user.updatePassword(password);

	    return UserResponseDto.from(user);
	}

	@Override
	@Transactional
	public void deleteUser(String email, Long userId) {

	    User user = getUser(userId);
	    validateOwner(user, email);

	    if (spaceRepository.existsByOwnerId(userId)) {
	    	throw new UserException(UserErrorCode.SPACE_OWNER_EXISTS);
	    }

	    if (roomRepository.existsByOwner_Id(userId)) {
	    	throw new UserException(UserErrorCode.ROOM_OWNER_EXISTS);
	    }

	    List<MemberProfileCard> profileCards = memberProfileCardRepository.findAllByUserId(userId);
	    memberProfileCardRepository.deleteAll(profileCards);
	    memberProfileCardRepository.flush();

	    userRepository.delete(user);
	}

	// 유저 존재 확인 + 조회
	private User getUser(Long userId) {
	    return userRepository.findById(userId)
	            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	// 본인 계정인지 검증
	private void validateOwner(User user, String email) {
	    if (!user.getEmail().equals(email)) {
	        throw new UserException(UserErrorCode.NO_PERMISSION);
	    }
	}

	// 비밀번호 미입력 시 기존 값 유지, 입력 시 새로 인코딩
	private String resolvePassword(String rawPassword, String currentPassword) {
	    return rawPassword == null ? currentPassword : passwordEncoder.encode(rawPassword);
	}

	// 이름 미입력 시 기존 값 유지
	private String resolveName(String newName, String currentName) {
	    return newName == null ? currentName : newName;
	}
	
	
}
