package com.narangnorang.chat.dto.response;

import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.entity.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
	private Long id;
	private String targetType;
	private Long targetId;
	private Long senderId;
	private String senderName;
	private String content;
	private LocalDateTime createdAt;

	public static ChatResponseDto from(Chat chat){
		return new ChatResponseDto(
				chat.getId(),
				chat.getTargetType(),
				chat.getTargetId(),
				chat.getSenderId(),
				null,
				chat.getContent(),
				chat.getCreatedAt()
		);
	}
}
