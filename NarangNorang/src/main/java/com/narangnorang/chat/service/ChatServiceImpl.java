package com.narangnorang.chat.service;

import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.dto.response.ChatHistoryResponseDto;
import com.narangnorang.chat.dto.response.ChatResponseDto;
import com.narangnorang.chat.entity.Chat;
import com.narangnorang.chat.exception.ChatErrorCode;
import com.narangnorang.chat.exception.ChatException;
import com.narangnorang.chat.repository.ChatRepository;
import com.narangnorang.common.ApiResponse;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Optional;

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

		boolean hasPermission = memberProfileCardRepository.findByUserIdAndRoomId(userId, targetId).isPresent();
		switch (targetType) {
			case "room":
				if (hasPermission == false) {
					throw new ChatException(ChatErrorCode.USER_NOT_PERMITTED, "유저ID: " + userId);
				}
				break;
			case "space":
				break;
		}
		Chat savedChat = chatRepository.save(chatRequestDto.toEntity());
		ChatResponseDto savedDto = ChatResponseDto.from(savedChat);
		//Todo: targetType에 맞춰 스페이스나 룸에서 해당 멤버 정보 가져오기
		String memberName = memberProfileCardRepository.findNameByRoomIdAndUserUserId(targetId, savedChat.getSenderId());

		//String memberName = memberProfileCardRepository.findNameBySpaceIdAndUserUserId(savedChat.getSenderId());
		if (memberName == null)
			throw new ChatException(ChatErrorCode.MEMBER_NOT_FOUND, savedChat.getSenderId());

		savedDto.setSenderName(memberName);
		log.info("채팅 저장 완료. 타입: {}, 번호: {}, 발신자이름: {}, 내용: {}", savedDto.getTargetType(), savedDto.getTargetId(), savedDto.getSenderName(), savedDto.getContent());

		return savedDto;
	}

	@Override
	@Transactional(readOnly = true)
	public ChatHistoryResponseDto getChatHistory(String targetType, Long targetId, Pageable pageable) {
		Slice<Chat> chatSlice = chatRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId, pageable);

		return ChatHistoryResponseDto.from(chatSlice);
	}

	@Override
	public boolean checkPermission(Long userId, String targetType, Long targetId) {
		switch (targetType) {
			case "room":
				return memberProfileCardRepository.findByUserIdAndRoomId(userId, targetId).isPresent();
			case "space":
				break;
		}
		return false;
	}
}
