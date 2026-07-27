package com.narangnorang.chat;

import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.dto.response.ChatHistoryResponseDto;
import com.narangnorang.chat.dto.response.ChatResponseDto;
import com.narangnorang.chat.entity.Chat;
import com.narangnorang.chat.exception.ChatErrorCode;
import com.narangnorang.chat.exception.ChatException;
import com.narangnorang.chat.repository.ChatRepository;
import com.narangnorang.chat.service.ChatServiceImpl;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatTest {

	@Mock
	private ChatRepository chatRepository;

	@Mock
	private MemberProfileCardRepository memberProfileCardRepository;

	@InjectMocks
	private ChatServiceImpl chatService;

	@Test
	@DisplayName("saveChat - Room 권한이 있는 경우 성공")
	void saveChat_Room_Success() {
		// given
		ChatRequestDto request = ChatRequestDto.builder()
				.senderId(1L)
				.targetId(2L)
				.targetType("room")
				.content("hello")
				.build();

		when(memberProfileCardRepository.findByUserIdAndRoomId(1L, 2L))
				.thenReturn(Optional.of(new MemberProfileCard()));
		when(memberProfileCardRepository.findNameByRoomIdAndUserId(2L, 1L)).thenReturn("TestUser");

		Chat chatEntity = Chat.builder()
				.id(1L)
				.senderId(1L)
				.targetId(2L)
				.targetType("room")
				.content("hello")
				.createdAt(LocalDateTime.now())
				.build();

		when(chatRepository.save(any(Chat.class))).thenReturn(chatEntity);

		// when
		ChatResponseDto response = chatService.saveChat(request);

		// then
		assertNotNull(response);
		assertEquals("TestUser", response.getSenderName());
		assertEquals("hello", response.getContent());
	}

	@Test
	@DisplayName("saveChat - Room 권한이 없는 경우 예외 발생")
	void saveChat_Room_NoPermission_ThrowsException() {
		// given
		ChatRequestDto request = ChatRequestDto.builder()
				.senderId(1L)
				.targetId(2L)
				.targetType("room")
				.content("hello")
				.build();

		when(memberProfileCardRepository.findByUserIdAndRoomId(1L, 2L)).thenReturn(Optional.empty());

		// when & then
		ChatException exception = assertThrows(ChatException.class, () -> chatService.saveChat(request));
		assertEquals(ChatErrorCode.USER_NOT_PERMITTED, exception.getErrorCode());
	}

	@Test
	@DisplayName("saveChat - Space 권한이 있는 경우 성공")
	void saveChat_Space_Success() {
		// given
		ChatRequestDto request = ChatRequestDto.builder()
				.senderId(1L)
				.targetId(3L)
				.targetType("space")
				.content("hello space")
				.build();

		when(memberProfileCardRepository.findByUserIdAndSpaceId(1L, 3L))
				.thenReturn(Optional.of(new MemberProfileCard()));
		when(memberProfileCardRepository.findNameByUserIdAndSpaceId(3L, 1L)).thenReturn("SpaceUser");

		Chat chatEntity = Chat.builder()
				.id(2L)
				.senderId(1L)
				.targetId(3L)
				.targetType("space")
				.content("hello space")
				.createdAt(LocalDateTime.now())
				.build();

		when(chatRepository.save(any(Chat.class))).thenReturn(chatEntity);

		// when
		ChatResponseDto response = chatService.saveChat(request);

		// then
		assertNotNull(response);
		assertEquals("SpaceUser", response.getSenderName());
		assertEquals("hello space", response.getContent());
	}

	@Test
	@DisplayName("saveChat - Space 권한이 없는 경우 예외 발생")
	void saveChat_Space_NoPermission_ThrowsException() {
		// given
		ChatRequestDto request = ChatRequestDto.builder()
				.senderId(1L)
				.targetId(3L)
				.targetType("space")
				.content("hello space")
				.build();

		when(memberProfileCardRepository.findByUserIdAndSpaceId(1L, 3L)).thenReturn(Optional.empty());

		// when & then
		ChatException exception = assertThrows(ChatException.class, () -> chatService.saveChat(request));
		assertEquals(ChatErrorCode.USER_NOT_PERMITTED, exception.getErrorCode());
	}

	@Test
	@DisplayName("saveChat - 잘못된 targetType인 경우 INVALID_INPUT_TYPE 예외 발생")
	void saveChat_InvalidTargetType_ThrowsException() {
		// given
		ChatRequestDto request = ChatRequestDto.builder()
				.senderId(1L)
				.targetId(4L)
				.targetType("unknown")
				.content("unknown message")
				.build();

		// when & then
		ChatException exception = assertThrows(ChatException.class, () -> chatService.saveChat(request));
		assertEquals(ChatErrorCode.INVALID_INPUT_TYPE, exception.getErrorCode());
	}

	@Test
	@DisplayName("getChatHistory - Room 채팅 내역 조회 성공")
	void getChatHistory_Room_Success() {
		// given
		String targetType = "room";
		Long targetId = 2L;
		Pageable pageable = PageRequest.of(0, 10);

		Chat chatEntity = Chat.builder()
				.id(1L)
				.senderId(1L)
				.targetId(2L)
				.targetType("room")
				.content("room history")
				.createdAt(LocalDateTime.now())
				.build();

		Slice<Chat> chatSlice = new SliceImpl<>(List.of(chatEntity), pageable, false);

		when(chatRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId, pageable))
				.thenReturn(chatSlice);

		// when
		ChatHistoryResponseDto response = chatService.getChatHistory(targetType, targetId, pageable);

		// then
		assertNotNull(response);
		assertEquals(1, response.getChats().size());
		assertEquals("room history", response.getChats().get(0).getContent());
		assertFalse(response.isHasNext());
	}

	@Test
	@DisplayName("getChatHistory - Space 채팅 내역 조회 성공")
	void getChatHistory_Space_Success() {
		// given
		String targetType = "space";
		Long targetId = 3L;
		Pageable pageable = PageRequest.of(0, 10);

		Chat chatEntity = Chat.builder()
				.id(2L)
				.senderId(1L)
				.targetId(3L)
				.targetType("space")
				.content("space history")
				.createdAt(LocalDateTime.now())
				.build();

		Slice<Chat> chatSlice = new SliceImpl<>(List.of(chatEntity), pageable, false);

		when(chatRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId, pageable))
				.thenReturn(chatSlice);

		// when
		ChatHistoryResponseDto response = chatService.getChatHistory(targetType, targetId, pageable);

		// then
		assertNotNull(response);
		assertEquals(1, response.getChats().size());
		assertEquals("space history", response.getChats().get(0).getContent());
		assertFalse(response.isHasNext());
	}

	@Test
	@DisplayName("getChatHistory - 잘못된 targetType인 경우 INVALID_INPUT_TYPE 예외 발생")
	void getChatHistory_InvalidTargetType_ThrowsException() {
		// given
		String targetType = "unknown";
		Long targetId = 4L;
		Pageable pageable = PageRequest.of(0, 10);

		// when & then
		ChatException exception = assertThrows(ChatException.class,
				() -> chatService.getChatHistory(targetType, targetId, pageable));
		assertEquals(ChatErrorCode.INVALID_INPUT_TYPE, exception.getErrorCode());
	}

}
