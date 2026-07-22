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
	public ChatResponseDto saveChat(ChatRequestDto chatRequestDto) {
		try {
			Long userId = chatRequestDto.getSenderId();
			Long targetId = chatRequestDto.getTargetId();
			String targetType = chatRequestDto.getTargetType();

			boolean hasPermission = memberProfileCardRepository.findByUserIdAndRoomId(userId, targetId).isPresent();
			switch (targetType) {
				case "room":
					if(hasPermission == false){
						throw new EntityNotFoundException("해당 유저는 접근 권한이 없습니다."); //TODO: 사용자예외처리필요
					}
				case "space":
					break;
			}
			Chat savedChat = chatRepository.save(chatRequestDto.toEntity());
			ChatResponseDto savedDto = ChatResponseDto.from(savedChat);

			String memberName = memberProfileCardRepository.findNameById(savedChat.getSenderId());
			if(memberName == null)
				throw new EntityNotFoundException("해당 ID를 가진 회원의 이름을 찾을 수 없습니다: " + savedChat.getSenderId());

			savedDto.setSenderName(memberName);
			log.info("채팅 저장 완료. 타입: {}, 번호: {}, 발신자이름: {}, 내용: {}", savedDto.getTargetType(), savedDto.getTargetId(), savedDto.getSenderName(), savedDto.getContent());

			return savedDto;
		} catch (DataAccessException e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.info("채팅 저장에 실패했습니다. 오류: {}", e.getMessage());

			return null;
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

			log.info("서버 내부 오류가 발생했습니다. 오류: {}", e.getMessage());

			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ChatHistoryResponseDto getChatHistory(String targetType, Long targetId, Pageable pageable) {
		Slice<Chat> chatSlice = chatRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId, pageable);

		return ChatHistoryResponseDto.from(chatSlice);
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
