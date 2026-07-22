package com.narangnorang.jwt;

import com.narangnorang.auth.config.MyUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;


@Component
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtUtil {

	private final MyUserDetailsService myUserDetailsService;

	// application.properties 의 myapp.jwt.secret 의 문자열로 현재 이 프로젝트는 관리
	// 실 운영은 절대 비추, github 도 비추, 배포전 배포 서버 환경변수에 등록, 환경변수를 사용하는 코드
	@Value("${narangnorang.jwt.secret}")
	private String secretKeyStr; // HS256 서명, 검증 key 문자열

	private SecretKey secretKey; // HS256 서명, 검증 key

	private final long tokenValidDuration = 1000L * 30;
	private final long refreshTokenValidDuration = 1000L * 60 * 60 * 120;

	// JwtUtil 생성 직후 호출
	@PostConstruct
	protected void init() {
		secretKey = new SecretKeySpec(
				secretKeyStr.getBytes(StandardCharsets.UTF_8),
				Jwts.SIG.HS256.key().build().getAlgorithm()
		);
	}

	// JWT 생성
	public String createToken(Long id, String username, List<String> roles) {
		Date now = new Date();

		return Jwts.builder()
				.subject(username)  // payload : 사용자 식별자 (sub)
				.claim("id", id)
				.claim("roles", roles) // payload: 사용자 역할 목록, 반복적으로 더 많은 데이터 추가 <= 공개 노출된다.
				.issuedAt(now)  // payload: 발급 시각 (iat)
				.expiration(new Date(now.getTime() + tokenValidDuration))
				.signWith(secretKey, Jwts.SIG.HS256)
				.compact();
	}

	public String createRefreshToken(Long id, String username, List<String> roles) {
		Date now = new Date();

		return Jwts.builder()
				.subject(username)  // payload : 사용자 식별자 (sub)
				.claim("id", id)
				.issuedAt(now)  // payload: 발급 시각 (iat)
				.expiration(new Date(now.getTime() + refreshTokenValidDuration))
				.signWith(secretKey, Jwts.SIG.HS256)
				.compact();
	}

	// JWT 에서 사용자 식별자
	public String getUsernameFromToken(String token) {
		return Jwts.parser()
				.verifyWith(secretKey) // 전달되는 token 의 서명 검증
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	public Long getUserIdFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey) // 전달되는 token 의 서명 검증
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims.get("id", Long.class);
	}

	public List<String> getRolesFromToken(String token, SecretKey secretKey) {
		// 1. 토큰 파싱 및 서명 검증
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)          // 서명 검증을 위한 키 설정
				.build()                        // JwtParser 객체 생성
				.parseSignedClaims(token)       // 토큰 파싱 (서명 불일치/만료 시 예외 발생)
				.getPayload();                  // Claims(Payload) 객체 획득

		// 2. "roles" claim 추출
		// JSON 배열 형태로 저장되어 있으므로 List 타입으로 캐스팅하여 가져옵니다.
		@SuppressWarnings("unchecked")
		List<String> roles = claims.get("roles", List.class);

		return roles;
	}

	// 프론트가 전달하는 Token 을 Header 로부터 추출
	// 프론트와 상호 약속된 방식에 따라 프론트가 request 에 저장한 토큰을 꺼내는 작어
	// http header 에 X-AUTH-TOKEN 이름
	public String getTokenFromHeader(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	public String getRefreshTokenFromHeader(HttpServletRequest request) {
		String refreshToken = request.getHeader("Refresh-Token");
		if (StringUtils.hasText(refreshToken)) {
			return refreshToken;
		}
		return null;
	}

	// X-AUTH-TOKEN 대신 Authorization Bearer+빈칸하나 (앞자리 7자리 자르고 얻는 방법)

	// 서명 유효
	// _1 대비 서명의 유효 포함, Claims 리턴하도록 수정
	public Claims validateToken(String token) {
		try {
			// parser 를 통해서 Claims 객체를 얻고, 이를 통해서 검증
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
				log.info("validateToken 중 만료 발생, 기한: {}", claims.getExpiration());
				return null;
			}
			return claims; // 유효

		} catch (Exception e) {
			log.info("validateToken 중 Exception e: {} 발생", e.getMessage());
			return null;
		}
	}

	public Claims validateRefreshToken(String refreshToken) {
		try {
			// parser 를 통해서 Claims 객체를 얻고, 이를 통해서 검증
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(refreshToken)
					.getPayload();

			if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
				// 현재 토큰의 만료일자가 지금보다 이전 => 만료
				// 새로 로그인
				return null;
			}
			return claims; // 유효

		} catch (ExpiredJwtException e) {
			log.warn("만료된 RefreshToken입니다.");
			return null;
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("유효하지 않은 RefreshToken입니다: {}", e.getMessage());
			return null;
		} catch (Exception e) {
			log.warn("몰라");
			return null;
		}
	}
	// DB Access 를 통한 2차 검증
	// token -> username 추출
	// username, 권한 등을 DB Access 확인 <= MyUserDetailsService.loadUserByUsername()
	public UsernamePasswordAuthenticationToken getAuthentication(String token) {
		UserDetails userDetails = myUserDetailsService.loadUserByUsername(getUsernameFromToken(token));
		return new UsernamePasswordAuthenticationToken(
				userDetails,
				"",
				userDetails.getAuthorities());
	}
}
