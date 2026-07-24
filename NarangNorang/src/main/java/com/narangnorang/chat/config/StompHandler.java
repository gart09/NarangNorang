package com.narangnorang.chat.config;

import com.narangnorang.chat.entity.Chat;
import com.narangnorang.chat.exception.ChatErrorCode;
import com.narangnorang.chat.exception.ChatException;
import com.narangnorang.chat.service.ChatService;
import com.narangnorang.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

	private final JwtUtil jwtUtil;
	private final ChatService chatService;

	@Override
	public Message<?> preSend(@NonNull Message message, @NonNull MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// 최초 웹소켓 연결(CONNECT) 시에만 토큰을 검사합니다.
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			// 프론트엔드가 STOMP 헤더에 담아 보낸 토큰을 꺼냅니다.
			String token = accessor.getFirstNativeHeader("Authorization");

			if (token != null && token.startsWith("Bearer ")) {
				String jwt = token.substring(7);

				Claims claims = jwtUtil.validateToken(jwt);

				if (claims == null) {
					throw new ChatException(ChatErrorCode.INVALID_TOKEN);
				}

				Long userId = claims.get("id", Long.class);
				Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

				if (sessionAttributes != null) {
					sessionAttributes.put("userId", userId);
				} else {
					Map<String, Object> newAttributes = new HashMap<>();
					newAttributes.put("userId", userId);
					accessor.setSessionAttributes(newAttributes);
				}

				log.info("웹소켓 연결 성공 - JWT 검증 완료, 사용자: {}", claims.getSubject());
			} else {
				log.error("웹소켓 연결 실패 - JWT 토큰이 없습니다.");
				throw new ChatException(ChatErrorCode.INVALID_TOKEN);
			}
		}
		else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())){
			String destination = accessor.getDestination();
			if (destination == null) return message;

			Long userId = null;
			Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
			if (sessionAttributes != null) {
				userId = (Long) sessionAttributes.get("userId");
			}
			if (userId == null) {
				throw new ChatException(ChatErrorCode.USER_NOT_FOUND_IN_SESSION);
			}
			boolean hasPermission = false;
			log.info("구독 요청 - 유저id: {}, 목적지: {}", userId, destination);

			String[] paths = destination.split("/");
			// "/sub/space/105".split("/")의 결과 -> ["", "sub", "space", "105"]
			if (paths.length == 4) {
				try {
					String targetType = paths[2];
					Long targetId = Long.parseLong(paths[3]);

					log.info("입장 요청 - 타입: {}, ID: {}", targetType, targetId);

					hasPermission = chatService.checkPermission(userId, targetType, targetId);
				} catch (NumberFormatException e) {
					log.warn("올바르지 않은 targetId 형식: {}", paths[3]);
					throw new ChatException(ChatErrorCode.INVALID_CHATROOM_TYPE, paths[3]);
				}
			}

			if (!hasPermission) {
				log.error("권한 없는 방 접근 시도: {}", destination);
				throw new ChatException(ChatErrorCode.USER_NOT_PERMITTED, "userId: " + userId + ", 경로: " + destination);
			}
			log.info("입장 허락.");
		}
		return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
	}
}
