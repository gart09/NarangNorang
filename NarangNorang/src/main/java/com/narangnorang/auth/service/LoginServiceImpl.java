package com.narangnorang.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.narangnorang.auth.Repository.RefreshTokenRepository;
import com.narangnorang.auth.dto.request.LoginRequestDto;
import com.narangnorang.auth.dto.response.LoginResponseDto;
import com.narangnorang.auth.entity.RefreshToken;
import com.narangnorang.jwt.JwtUtil;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.entity.UserRole;
import com.narangnorang.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final Long refreshTokenValidDurationSeconds = 60L * 60 * 120;

	@Override
	public LoginResponseDto login(LoginRequestDto loginRequestDto) {
		try {
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginRequestDto.getEmail(),
							loginRequestDto.getPassword()
					)
			);

			LocalDateTime newExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenValidDurationSeconds);
			String email = authentication.getName();
			User user = userRepository.findByEmail(email).orElseThrow();
			Long id = user.getId();
			Optional<RefreshToken> optRefreshToken = refreshTokenRepository.findByUserId(id);

			List<String> roles = authentication.getAuthorities()
					.stream()
					.map(GrantedAuthority::getAuthority)
					.toList();

			String token = jwtUtil.createToken(id, email, user.getName(), roles);
			String newRefreshTokenKey = jwtUtil.createRefreshToken(id, email);
			RefreshToken refreshToken = null;
			if (optRefreshToken.isPresent()) {
				refreshToken = optRefreshToken.get();
				refreshToken.updateToken(newRefreshTokenKey, newExpiredAt);
			} else {
				refreshToken = RefreshToken.builder()
						.userId(id)
						.tokenKey(newRefreshTokenKey)
						.expiredAt(newExpiredAt)
						.build();
			}
			refreshTokenRepository.save(refreshToken);

			log.info("Login succeeded for {}", email);

			LoginResponseDto loginResponseDto = LoginResponseDto.builder()
					.token(token)
					.refreshToken(refreshToken.getTokenKey())
					.build();

			return loginResponseDto;
		} catch (AuthenticationException e) {
			log.info("Login failed for {}", loginRequestDto.getEmail());
			return null;
		}
	}

	@Override
	public LoginResponseDto checkRefreshToken(String refreshToken) {
		try {
			if (jwtUtil.validateRefreshToken(refreshToken) == null) {
				log.info("Invalid refresh token");

				return null;
			}

			Long userId = jwtUtil.getUserIdFromToken(refreshToken);

			Optional<RefreshToken> optRefreshToken = refreshTokenRepository.findByUserId(userId);

			if (optRefreshToken.isPresent() && optRefreshToken.get().getTokenKey().equals(refreshToken)) {


				User user = userRepository.findById(userId)
						.orElseThrow(() -> new UsernameNotFoundException("User not found"));

				List<String> roles = user
						.getUserRoles().stream()
						.map(UserRole::getName)
						.map(name -> "ROLE_" + name)
						.toList();

				Long id = user.getId();
				String email = user.getEmail();
				String name = user.getName();

				String newAccessToken = jwtUtil.createToken(id, email, name, roles);
				log.info("Using RefreshToken, Get AccessToken Success for {}", email);

				LoginResponseDto loginResponseDto = LoginResponseDto.builder()
						.token(newAccessToken)
						.refreshToken(refreshToken)
						.build();

				return loginResponseDto;

			} else {
				log.info("RefreshToken mismatch or not found for {}", refreshToken);
				return null;
			}
		} catch (Exception e) {
			log.info("Get AccessToken failed: {}", e.getMessage());
			return null;
		}
	}

	@Override
	@Transactional
	public void logout(Long userId) {
		refreshTokenRepository.deleteByUserId(userId);
	    log.info("Logout succeeded for userId={}", userId);
	}
}
