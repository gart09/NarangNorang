package com.narangnorang.chat.config;

import com.narangnorang.chat.exception.ChatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CustomStompErrorHandler extends StompSubProtocolErrorHandler {

	@Override
	public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
		// ex.getCause()를 통해 인터셉터에서 던진 실제 ChatException을 꺼냅니다.
		Throwable cause = ex.getCause();

		if (cause instanceof ChatException chatException) {
			return prepareErrorMessage(chatException);
		}

		return super.handleClientMessageProcessingError(clientMessage, ex);
	}

	private Message<byte[]> prepareErrorMessage(ChatException e) {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

		// STOMP ERROR 헤더에 커스텀 에러 정보 설정
		accessor.setMessage(e.getErrorCode().getMessage()); // 또는 e.getMessage()
		accessor.setLeaveMutable(true);

		// 필요하다면 e.getChatErrorCode().getCode() 등을 Body(JSON)로 전달 가능
		String responseBody = "{\"code\": \"" + e.getErrorCode().getCode()+ "\", \"message\": \"" + e.getMessage() + "\"}";

		return MessageBuilder.createMessage(
				responseBody.getBytes(StandardCharsets.UTF_8),
				accessor.getMessageHeaders()
		);
	}
}