package com.narangnorang.auth.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Spring Security 에서 인가되지 않은 요청에 대해 AuthenticationException 예외 발생시키고 이 처리를 호출
@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint{

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		// data 요청에 대한 로그인이 필요한 상황을 프론트에게 전달
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		String jsonStr = """
            {
              "success": false,
              "code": 401,
              "message": "인증이 필요하거나 토큰이 만료되었습니다."
            }
            """;
		response.getWriter().write(jsonStr);

	}

}
