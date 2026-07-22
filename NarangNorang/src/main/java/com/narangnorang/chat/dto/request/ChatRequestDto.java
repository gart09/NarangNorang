package com.narangnorang.chat.dto.request;

import com.narangnorang.chat.dto.response.ChatResponseDto;
import com.narangnorang.chat.entity.Chat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatRequestDto {
	private String targetType;
	private Long targetId;
	private Long senderId;
	private String content;

	public Chat toEntity(){
		return Chat.builder()
				.targetType(this.targetType)
				.targetId(this.targetId)
				.senderId(this.senderId)
				.content(this.content)
				.createdAt(LocalDateTime.now())
				.build();
	}
}
