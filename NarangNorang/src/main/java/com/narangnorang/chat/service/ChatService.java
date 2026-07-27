package com.narangnorang.chat.service;

import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.dto.response.ChatHistoryResponseDto;
import com.narangnorang.chat.dto.response.ChatResponseDto;
import com.narangnorang.common.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatService {
	ChatResponseDto saveChat(ChatRequestDto chatRequestDto);
	ChatHistoryResponseDto getChatHistory(String targetType, Long targetId, Pageable pageable);
	boolean checkPermission(Long userId, String targetType, Long targetId);
}
