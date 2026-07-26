package com.narangnorang.chat.service;

import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.dto.response.ChatHistoryResponseDto;
import com.narangnorang.chat.dto.response.ChatResponseDto;
import com.narangnorang.chat.entity.Chat;
import com.narangnorang.chat.exception.ChatErrorCode;
import com.narangnorang.chat.exception.ChatException;
import com.narangnorang.chat.repository.ChatRepository;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService{

	private final ChatRepository chatRepository;
	private final MemberProfileCardRepository memberProfileCardRepository;

	@Override
	@Transactional
	public ChatResponseDto saveChat(ChatRequestDto chatRequestDto) {
		Long userId = chatRequestDto.getSenderId();
		Long targetId = chatRequestDto.getTargetId();
		String targetType = chatRequestDto.getTargetType();

		boolean hasPermission;
		String memberName = null;
		switch (targetType) {
			case "room":
				hasPermission = memberProfileCardRepository.findByUserIdAndRoomId(userId, targetId).isPresent();
				if (hasPermission == false) {
					throw new ChatException(ChatErrorCode.USER_NOT_PERMITTED, "-> 룸id: " + targetId + ", 유저ID: " + userId);
				}
				memberName = memberProfileCardRepository.findNameByRoomIdAndUserId(targetId, userId);
				break;
			case "space":
				hasPermission = memberProfileCardRepository.findByUserIdAndSpaceId(userId, targetId).isPresent();
				if (hasPermission == false) {
					throw new ChatException(ChatErrorCode.USER_NOT_PERMITTED, "-> 스페이스id: " + targetId + ", 유저ID: " + userId);
				}
				memberName = memberProfileCardRepository.findNameByUserIdAndSpaceId(targetId, userId);
				break;
			default:
				throw new ChatException(ChatErrorCode.INVALID_INPUT_TYPE, targetType);
		}
		Chat savedChat = chatRepository.save(chatRequestDto.toEntity());
		ChatResponseDto savedDto = ChatResponseDto.from(savedChat);
		if (memberName == null)
			throw new ChatException(ChatErrorCode.MEMBER_NOT_FOUND, savedChat.getSenderId());

		savedDto.setSenderName(memberName);
		log.info("채팅 저장 완료. 타입: {}, 번호: {}, 발신자이름: {}, 내용: {}", savedDto.getTargetType(), savedDto.getTargetId(), savedDto.getSenderName(), savedDto.getContent());

		return savedDto;
	}

	@Override
	@Transactional(readOnly = true)
	public ChatHistoryResponseDto getChatHistory(String targetType, Long targetId, Pageable pageable) {
		if (!"room".equals(targetType) && !"space".equals(targetType)) {
			throw new ChatException(ChatErrorCode.INVALID_INPUT_TYPE, targetType);
		}
		Slice<Chat> chatSlice = chatRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId, pageable);

		return ChatHistoryResponseDto.from(chatSlice);
	}

	@Override
	public boolean checkPermission(Long userId, String targetType, Long targetId) {
		return switch (targetType) {
			case "room" -> memberProfileCardRepository.findByUserIdAndRoomId(userId, targetId).isPresent();
			case "space" -> memberProfileCardRepository.findByUserIdAndSpaceId(userId, targetId).isPresent();
			default -> throw new ChatException(ChatErrorCode.INVALID_INPUT_TYPE, targetType);
		};
	}
}
