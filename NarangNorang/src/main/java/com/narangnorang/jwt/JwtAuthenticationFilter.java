package com.narangnorang.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter : 요청 한 개 당 한 번만 수행
// JWT 인증 <= JwtUtil 필요
//   방식 1 : Token 유효성 판단을 sign, 만료일자 로 판별  (낙관적, 소극적)
//   방식 2 : Token 유효성 판단을 sign, 만료일자 로 판별 + DB Access 체크 (비관적, 적극적)
//           Token 발급 이후, 사용자 탈퇴 한 상황 등 항상 DB 에서 최신화, 현행화 작업 진행
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	private final com.narangnorang.jwt.JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		// #1. 사용자의 요청 request 에 포함된 JWT 문자열 추출
		String token = jwtUtil.getTokenFromHeader(request);

		// #2. 서명, 만료 검증
		Claims claims = (token != null ) ? jwtUtil.validateToken(token) : null;

		// 위 JWT 이증 방식 2
		if( claims != null ) {
			UsernamePasswordAuthenticationToken authenticationToken =
					jwtUtil.getAuthentication(token);

			authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 권고 코드
			// session 아니라 Filter Chain 처리 동안 사용하는 공용 공간
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		}
		
		filterChain.doFilter(request, response);
	}
}
