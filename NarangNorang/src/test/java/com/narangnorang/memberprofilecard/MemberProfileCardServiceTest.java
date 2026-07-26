package com.narangnorang.memberprofilecard;

import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardCreateRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardReadRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardUpdateRequestDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardCreateResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardReadResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardUpdateResponseDto;
import com.narangnorang.memberprofilecard.dto.response.RoomsListResponseDto;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.exception.MemberProfileCardErrorCode;
import com.narangnorang.memberprofilecard.exception.MemberProfileCardException;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.memberprofilecard.service.MemberProfileCardServiceImpl;
import com.narangnorang.room.entity.OptionType;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.entity.RoomProfileCustomField;
import com.narangnorang.room.repository.RoomProfileCustomFieldRepository;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberProfileCardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MemberProfileCardRepository memberProfileCardRepository;

    @Mock
    private RoomProfileCustomFieldRepository roomProfileCustomFieldRepository;

    @InjectMocks
    private MemberProfileCardServiceImpl memberProfileCardService;

    // --- createMemberProfileCard 테스트 ---

    @Test
    @DisplayName("createMemberProfileCard - 정상 생성 성공")
    void createMemberProfileCard_Success() {
        Long userId = 1L;
        String roomCode = "ROOM_123";
        User user = User.builder().id(userId).build();
        Room room = Room.builder().id(10L).build();

        MemberProfileCardCreateRequestDto request = MemberProfileCardCreateRequestDto.builder()
                .name("testName")
                .roomCode(roomCode)
                .answers(Map.of())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findByRoomCode(roomCode)).thenReturn(Optional.of(room));
        when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, room.getId())).thenReturn(false);

        MemberProfileCard savedCard = MemberProfileCard.builder()
                .id(100L)
                .name("testName")
                .user(user)
                .room(room)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(memberProfileCardRepository.save(any(MemberProfileCard.class))).thenReturn(savedCard);

        MemberProfileCardCreateResponseDto response = memberProfileCardService.createMemberProfileCard(userId, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        verify(memberProfileCardRepository, times(1)).save(any(MemberProfileCard.class));
    }

    @Test
    @DisplayName("createMemberProfileCard - 유저 없음 실패")
    void createMemberProfileCard_UserNotFound() {
        Long userId = 1L;
        MemberProfileCardCreateRequestDto request = MemberProfileCardCreateRequestDto.builder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.createMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("createMemberProfileCard - 방 없음 실패")
    void createMemberProfileCard_RoomNotFound() {
        Long userId = 1L;
        String roomCode = "INVALID_ROOM";
        User user = User.builder().id(userId).build();
        MemberProfileCardCreateRequestDto request = MemberProfileCardCreateRequestDto.builder()
                .roomCode(roomCode).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findByRoomCode(roomCode)).thenReturn(Optional.empty());

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.createMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("createMemberProfileCard - 이미 프로필카드 존재 실패")
    void createMemberProfileCard_AlreadyExists() {
        Long userId = 1L;
        String roomCode = "ROOM_123";
        User user = User.builder().id(userId).build();
        Room room = Room.builder().id(10L).build();
        MemberProfileCardCreateRequestDto request = MemberProfileCardCreateRequestDto.builder()
                .roomCode(roomCode).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findByRoomCode(roomCode)).thenReturn(Optional.of(room));
        when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, room.getId())).thenReturn(true);

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.createMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.MEMBER_PROFILE_CARD_DUPLICATED, ex.getErrorCode());
    }

    @Test
    @DisplayName("createMemberProfileCard - 필수값 누락 실패")
    void createMemberProfileCard_MissingRequiredField() {
        Long userId = 1L;
        String roomCode = "ROOM_123";
        User user = User.builder().id(userId).build();
        Room room = Room.builder().id(10L).build();
        MemberProfileCardCreateRequestDto request = MemberProfileCardCreateRequestDto.builder()
                .roomCode(roomCode)
                .answers(Map.of()) // 필수값이 없게 만듦
                .build();

        RoomProfileCustomField requiredField = RoomProfileCustomField.builder()
                .id(99L).fieldName("취미").required(true).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findByRoomCode(roomCode)).thenReturn(Optional.of(room));
        when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, room.getId())).thenReturn(false);
        when(roomProfileCustomFieldRepository.findAllByRoomIdAndRequiredTrue(room.getId()))
                .thenReturn(List.of(requiredField));

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.createMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.REQUIRED_FIELD_MISS, ex.getErrorCode());
    }

    @Test
    @DisplayName("createMemberProfileCard - 필수값 빈칸 실패")
    void createMemberProfileCard_BlankRequiredField() {
        Long userId = 1L;
        String roomCode = "ROOM_123";
        User user = User.builder().id(userId).build();
        Room room = Room.builder().id(10L).build();
        MemberProfileCardCreateRequestDto request = MemberProfileCardCreateRequestDto.builder()
                .roomCode(roomCode)
                .answers(Map.of(99L, "   ")) // 공백만 입력
                .build();

        RoomProfileCustomField requiredField = RoomProfileCustomField.builder()
                .id(99L).fieldName("취미").required(true).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findByRoomCode(roomCode)).thenReturn(Optional.of(room));
        when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, room.getId())).thenReturn(false);
        when(roomProfileCustomFieldRepository.findAllById(anySet())).thenReturn(List.of(requiredField));
        when(roomProfileCustomFieldRepository.findAllByRoomIdAndRequiredTrue(room.getId()))
                .thenReturn(List.of(requiredField));
        when(roomProfileCustomFieldRepository.findById(99L)).thenReturn(Optional.of(requiredField));

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.createMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.REQUIRED_FIELD_SPACE, ex.getErrorCode());
    }

    @Test
    @DisplayName("createMemberProfileCard - 선택값 불일치 실패")
    void createMemberProfileCard_InvalidSelectOption() {
        Long userId = 1L;
        String roomCode = "ROOM_123";
        User user = User.builder().id(userId).build();
        Room room = Room.builder().id(10L).build();
        MemberProfileCardCreateRequestDto request = MemberProfileCardCreateRequestDto.builder()
                .roomCode(roomCode)
                .answers(Map.of(99L, "잘못된선택"))
                .build();

        RoomProfileCustomField selectField = RoomProfileCustomField.builder()
                .id(99L).fieldName("성별").required(false).optionType(OptionType.SINGLE_SELECT).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roomRepository.findByRoomCode(roomCode)).thenReturn(Optional.of(room));
        when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, room.getId())).thenReturn(false);
        when(roomProfileCustomFieldRepository.findAllById(anySet())).thenReturn(List.of(selectField));
        when(roomProfileCustomFieldRepository.findAllByRoomIdAndRequiredTrue(room.getId()))
                .thenReturn(List.of());
        when(roomProfileCustomFieldRepository.existsByIdAndOptionTypeIn(eq(99L), anyList()))
                .thenReturn(true);
        when(roomProfileCustomFieldRepository.findSelectTypeOptionsByFieldId(99L))
                .thenReturn(List.of("남", "여")); // 가능한 선택지에는 "잘못된선택"이 없음
        when(roomProfileCustomFieldRepository.findById(99L)).thenReturn(Optional.of(selectField));

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.createMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.CANT_SELECT_FIELD, ex.getErrorCode());
    }

    // --- findMemberProfileCard 테스트 ---

    @Test
    @DisplayName("findMemberProfileCard - 조회 성공")
    void findMemberProfileCard_Success() {
        Long userId = 1L;
        MemberProfileCardReadRequestDto request = MemberProfileCardReadRequestDto.builder()
                .roomId(10L).build();

        when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, 10L)).thenReturn(true);

        MemberProfileCard card = MemberProfileCard.builder()
                .id(100L).name("test").user(User.builder().id(userId).build())
                .room(Room.builder().id(10L).build()).build();
        when(memberProfileCardRepository.searchMemberProfileCards(request)).thenReturn(List.of(card));

        List<MemberProfileCardReadResponseDto> result = memberProfileCardService.findMemberProfileCard(userId, request);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    @DisplayName("findMemberProfileCard - 방 소속 아님 (권한없음) 실패")
    void findMemberProfileCard_NoPermission() {
        Long userId = 1L;
        MemberProfileCardReadRequestDto request = MemberProfileCardReadRequestDto.builder()
                .roomId(10L).build();

        when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, 10L)).thenReturn(false);

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.findMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.USER_NOT_PERMITTED, ex.getErrorCode());
    }

    // --- updateMemberProfileCard 테스트 ---

    @Test
    @DisplayName("updateMemberProfileCard - 정상 수정 성공")
    void updateMemberProfileCard_Success() {
        Long userId = 1L;
        MemberProfileCardUpdateRequestDto request = MemberProfileCardUpdateRequestDto.builder()
                .memberProfileCardId(100L)
                .name("updatedName")
                .answers(Map.of())
                .build();

        User user = User.builder().id(userId).build();
        Room room = Room.builder().id(10L).build();
        MemberProfileCard card = MemberProfileCard.builder()
                .id(100L).user(user).room(room).name("oldName").build();

        when(memberProfileCardRepository.findById(100L)).thenReturn(Optional.of(card));

        MemberProfileCardUpdateResponseDto response = memberProfileCardService.updateMemberProfileCard(userId, request);
        assertNotNull(response);
    }

    @Test
    @DisplayName("updateMemberProfileCard - 카드 없음 실패")
    void updateMemberProfileCard_CardNotFound() {
        Long userId = 1L;
        MemberProfileCardUpdateRequestDto request = MemberProfileCardUpdateRequestDto.builder()
                .memberProfileCardId(100L).build();

        when(memberProfileCardRepository.findById(100L)).thenReturn(Optional.empty());

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.updateMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.MEMBER_PROFILE_CARD_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateMemberProfileCard - 본인 카드 아님 실패")
    void updateMemberProfileCard_NoPermission() {
        Long userId = 1L;
        MemberProfileCardUpdateRequestDto request = MemberProfileCardUpdateRequestDto.builder()
                .memberProfileCardId(100L).build();

        User otherUser = User.builder().id(2L).build(); // 다른 사람
        MemberProfileCard card = MemberProfileCard.builder()
                .id(100L).user(otherUser).build();

        when(memberProfileCardRepository.findById(100L)).thenReturn(Optional.of(card));

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.updateMemberProfileCard(userId, request));
        assertEquals(MemberProfileCardErrorCode.USER_NOT_PERMITTED, ex.getErrorCode());
    }

    // --- deleteMemberProfileCard 테스트 ---

    @Test
    @DisplayName("deleteMemberProfileCard - 삭제 성공")
    void deleteMemberProfileCard_Success() {
        Long userId = 1L;
        Long cardId = 100L;

        User user = User.builder().id(userId).build();
        MemberProfileCard card = MemberProfileCard.builder()
                .id(cardId).user(user).build();

        when(memberProfileCardRepository.findById(cardId)).thenReturn(Optional.of(card));

        memberProfileCardService.deleteMemberProfileCard(userId, cardId);
        verify(memberProfileCardRepository, times(1)).delete(card);
    }

    @Test
    @DisplayName("deleteMemberProfileCard - 카드 없음 실패")
    void deleteMemberProfileCard_CardNotFound() {
        Long userId = 1L;
        Long cardId = 100L;

        when(memberProfileCardRepository.findById(cardId)).thenReturn(Optional.empty());

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.deleteMemberProfileCard(userId, cardId));
        assertEquals(MemberProfileCardErrorCode.MEMBER_PROFILE_CARD_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("deleteMemberProfileCard - 권한 없음 실패")
    void deleteMemberProfileCard_NoPermission() {
        Long userId = 1L;
        Long cardId = 100L;

        User otherUser = User.builder().id(2L).build();
        MemberProfileCard card = MemberProfileCard.builder()
                .id(cardId).user(otherUser).build();

        when(memberProfileCardRepository.findById(cardId)).thenReturn(Optional.of(card));

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.deleteMemberProfileCard(userId, cardId));
        assertEquals(MemberProfileCardErrorCode.USER_NOT_PERMITTED, ex.getErrorCode());
    }

    // --- getRoomsList 테스트 ---

    @Test
    @DisplayName("getRoomsList - 방 목록 조회 성공")
    void getRoomsList_Success() {
        Long userId = 1L;
        User roomOwner = User.builder().id(99L).name("Owner").build();
        Room room1 = Room.builder()
                .id(10L)
                .name("room1")
                .description("desc1")
                .maxMember(10)
                .roomCode("CODE1")
                .createdAt(LocalDateTime.now())
                .owner(roomOwner)
                .customFields(List.of())
                .build();
        Room room2 = Room.builder()
                .id(20L)
                .name("room2")
                .description("desc2")
                .maxMember(20)
                .roomCode("CODE2")
                .createdAt(LocalDateTime.now())
                .owner(roomOwner)
                .customFields(List.of())
                .build();
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(memberProfileCardRepository.findRoomsByUserId(userId)).thenReturn(List.of(room1, room2));
        when(memberProfileCardRepository.countByRoomId(10L)).thenReturn(5L);
        when(memberProfileCardRepository.countByRoomId(20L)).thenReturn(10L);

        RoomsListResponseDto response = memberProfileCardService.getRoomsList(userId);
        assertEquals(2, response.getRooms().size());
        assertEquals(5L, response.getRooms().get(0).getCurrentMember());
        assertEquals(10L, response.getRooms().get(1).getCurrentMember());
    }

    @Test
    @DisplayName("getRoomsList - 유저 없음 실패")
    void getRoomsList_UserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        MemberProfileCardException ex = assertThrows(MemberProfileCardException.class,
                () -> memberProfileCardService.getRoomsList(userId));
        assertEquals(MemberProfileCardErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }
}
