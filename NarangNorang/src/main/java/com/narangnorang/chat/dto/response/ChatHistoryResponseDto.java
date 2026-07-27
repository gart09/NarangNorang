package com.narangnorang.chat.dto.response;

import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.entity.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponseDto {
	private List<ChatResponseDto> chats;
	private boolean hasNext;
	private int pageNumber;
	private int pageSize;

	public static ChatHistoryResponseDto from(Slice<Chat> chatSlice) {
		List<ChatResponseDto> chatResponseDtos = chatSlice.getContent().stream()
				.map(ChatResponseDto::from)
				.toList();

		return ChatHistoryResponseDto.builder()
				.chats(chatResponseDtos)
				.hasNext(chatSlice.hasNext())
				.pageNumber(chatSlice.getNumber())
				.pageSize(chatSlice.getSize())
				.build();
	}
}
