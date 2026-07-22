package com.narangnorang.chat.service;

import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.dto.response.ChatHistoryResponseDto;
import com.narangnorang.chat.dto.response.ChatResponseDto;
import com.narangnorang.chat.entity.Chat;
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
	public ApiResponse<ChatResponseDto> saveChat(ChatRequestDto chatRequestDto) {
		try {
			Chat savedChat = chatRepository.save(chatRequestDto.toEntity());
			ChatResponseDto savedDto = ChatResponseDto.from(savedChat);
			String memberName = memberProfileCardRepository.findNameById(savedChat.getSenderId());
			if(memberName == null)
				throw new EntityNotFoundException("해당 ID를 가진 회원의 이름을 찾을 수 없습니다: " + savedChat.getSenderId());

			savedDto.setSenderName(memberName);

			ApiResponse<ChatResponseDto> apiResponse = new ApiResponse<>();
			apiResponse.setSuccess(savedDto);

			log.info("채팅 저장 완료. 타입: {}, 번호: {}, 발신자이름: {}, 내용: {}", savedDto.getTargetType(), savedDto.getTargetId(), savedDto.getSenderName(), savedDto.getContent());

			return apiResponse;
		} catch (DataAccessException e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

			ApiResponse<ChatResponseDto> apiResponse = new ApiResponse<>();
			apiResponse.setFail("채팅 저장에 실패했습니다. 오류: " + e.getMessage());

			return apiResponse;
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

			ApiResponse<ChatResponseDto> apiResponse = new ApiResponse<>();
			apiResponse.setFail("서버 내부 오류가 발생했습니다. 오류: " + e.getMessage());

			return apiResponse;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<ChatHistoryResponseDto> getChatHistory(String targetType, Long targetId, Pageable pageable) {
		Slice<Chat> chatSlice = chatRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId, pageable);

		ChatHistoryResponseDto chatHistoryResponseDto = ChatHistoryResponseDto.from(chatSlice);
		ApiResponse<ChatHistoryResponseDto> apiResponse = new ApiResponse<>();
		apiResponse.setSuccess(chatHistoryResponseDto);
		return apiResponse;
	}

	@Override
	public boolean checkPermission(Long userId, String targetType, Long targetId) {
		try {
			switch (targetType) {
				case "room":
					return memberProfileCardRepository.findByUserIdAndRoomId(userId, targetId).isPresent();
				case "space":
					break;
			}
		} catch (Exception e) {
			log.info("에러 발생. 오류: {}",e.getMessage());
			return false;
		}
		return false;
	}
}
