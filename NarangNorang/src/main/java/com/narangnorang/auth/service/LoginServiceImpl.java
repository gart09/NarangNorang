package com.narangnorang.auth.service;

import com.narangnorang.auth.Repository.RefreshTokenRepository;
import com.narangnorang.auth.entity.RefreshToken;
import com.narangnorang.common.ApiResponse;
import com.narangnorang.jwt.JwtUtil;
import com.narangnorang.auth.dto.LoginRequestDto;
import com.narangnorang.auth.dto.LoginResultDto;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.entity.UserRole;
import com.narangnorang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
	public ApiResponse<LoginResultDto> login(LoginRequestDto loginRequestDto) {
		ApiResponse<LoginResultDto> apiResponse = new ApiResponse<>();
		try {
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String encryptedPassword = passwordEncoder.encode(loginRequestDto.getPassword());

			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginRequestDto.getEmail(),
							loginRequestDto.getPassword()
					)
			);

			LocalDateTime newExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenValidDurationSeconds);
			String email = authentication.getName();
			Long id = userRepository.findByEmail(email).orElseThrow().getId();
			Optional<RefreshToken> optRefreshToken = refreshTokenRepository.findByUserId(id);

			List<String> roles = authentication.getAuthorities()
					.stream()
					.map(GrantedAuthority::getAuthority)
					.toList();

			String token = jwtUtil.createToken(id, email, roles);
			String newRefreshTokenKey = jwtUtil.createRefreshToken(id, email, roles);
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

			LoginResultDto loginResultDto = LoginResultDto.builder()
					.result("success")
					.token(token)
					.refreshToken(refreshToken.getTokenKey())
					.build();
			apiResponse.setSuccess(loginResultDto);

			return apiResponse;
		} catch (AuthenticationException e) {
			apiResponse.setFail("Login failed for " + loginRequestDto.getEmail());
			return apiResponse;
		}
	}

	@Override
	public ApiResponse<LoginResultDto> checkRefreshToken(String refreshToken) {
		ApiResponse<LoginResultDto> apiResponse = new ApiResponse<>();
		try {
			if (jwtUtil.validateRefreshToken(refreshToken) == null) {
				apiResponse.setFail("Invalid refresh token");
				return apiResponse;
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

				String newAccessToken = jwtUtil.createToken(id, email, roles);
				log.info("Using RefreshToken, Get AccessToken Success for {}", email);

				LoginResultDto loginResultDto = LoginResultDto.builder()
						.result("success")
						.token(newAccessToken)
						.refreshToken(refreshToken)
						.build();

				apiResponse.setSuccess(loginResultDto);

				return apiResponse;

			} else {
				apiResponse.setFail("RefreshToken mismatch or not found for " + refreshToken);
				return apiResponse;
			}
		} catch (Exception e) {
			apiResponse.setFail("Get AccessToken failed: " + e.getMessage());
			return apiResponse;
		}
	}
}
