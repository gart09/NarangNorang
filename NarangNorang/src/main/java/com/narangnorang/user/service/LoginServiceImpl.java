package com.narangnorang.user.service;

import com.narangnorang.jwt.JwtUtil;
import com.narangnorang.user.dto.LoginRequestDto;
import com.narangnorang.user.dto.LoginResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResultDto login(LoginRequestDto loginRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getEmail(),
                            loginRequestDto.getPassword()
                    )
            );

            String email = authentication.getName();

            List<String> roles = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtUtil.createToken(email, roles);
            String refreshToken = jwtUtil.createRefreshToken(email, roles);

            log.info("Login succeeded for {}", email);

            return LoginResultDto.builder()
                    .result("success")
                    .token(token)
                    .refreshToken(refreshToken)
                    .build();
        } catch (AuthenticationException e) {
            log.warn("Login failed for {}", loginRequestDto.getEmail());

            return LoginResultDto.builder()
                    .result("fail")
                    .build();
        }
    }
}
