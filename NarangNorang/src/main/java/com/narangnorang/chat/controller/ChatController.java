package com.narangnorang.chat.controller;

import com.narangnorang.auth.config.MyUserDetails;
import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.dto.response.ChatHistoryResponseDto;
import com.narangnorang.chat.dto.response.ChatResponseDto;
import com.narangnorang.chat.service.ChatService;
import com.narangnorang.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatController {

	private final int PAGESIZE = 50;
	private final ChatService chatService;
	private final SimpMessageSendingOperations messagingTemplate;

	@PostMapping("/send")
	public ApiResponse<ChatResponseDto> sendMessage(
			@RequestBody ChatRequestDto chatRequestDto,
			@AuthenticationPrincipal MyUserDetails myUserDetails){

		Long userId = myUserDetails.getId();
		chatRequestDto.setSenderId(userId);

		log.info("메시지 들어옴: {}", chatRequestDto);

		ChatResponseDto chatResponseDto = chatService.saveChat(chatRequestDto);
		ApiResponse<ChatResponseDto> apiResponse = new ApiResponse<>();
		if(chatResponseDto == null) {
			apiResponse.setFail("채팅 저장 실패");
			return apiResponse;
		}
		apiResponse.setSuccess(chatResponseDto);

		String destination = "";
		if ("room".equals(chatRequestDto.getTargetType())) {
			destination = "/sub/room/" + chatRequestDto.getTargetId();
		} else if ("space".equals(chatRequestDto.getTargetType())) {
			destination = "/sub/space/" + chatRequestDto.getTargetId();
		}

		if (!destination.isEmpty() && apiResponse.getResult() != null) {
			messagingTemplate.convertAndSend(destination, apiResponse.getResult());
		} else {
			log.warn("채팅 저장 실패 혹은 Result가 null이어서 브로드캐스트 생략");
		}

		return apiResponse;
	}

	@GetMapping("/{targetType}/{targetId}")
	public ApiResponse<ChatHistoryResponseDto> getChatHistory(
			@PathVariable("targetType") String targetType,
			@PathVariable("targetId") Long targetId,
			@PageableDefault(size = PAGESIZE) Pageable pageable,
			@AuthenticationPrincipal MyUserDetails myUserDetails){

		ApiResponse<ChatHistoryResponseDto> apiResponse = new ApiResponse<>();

		Long userId = myUserDetails.getId();

		if(chatService.checkPermission(userId, targetType, targetId) == false){
			apiResponse.setFail("권한이 없는 방/스페이스의 목록을 가져오려 했습니다.");
			return apiResponse;
		}

		ChatHistoryResponseDto chatHistoryResponseDto = chatService.getChatHistory(targetType, targetId, pageable);
		if(chatHistoryResponseDto == null){
			apiResponse.setFail("채팅 조회 실패");
			return apiResponse;
		}
		apiResponse.setSuccess(chatHistoryResponseDto);
		return apiResponse;
	}
}
