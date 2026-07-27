package com.narangnorang.room.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.memberprofilecard.repository.MemberProfileCustomAnswerRepository;
import com.narangnorang.room.dto.request.RoomCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldBulkUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldOptionUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldsUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomUpdateRequestDto;
import com.narangnorang.room.dto.response.RoomJoinResponseDto;
import com.narangnorang.room.dto.response.RoomProfileCustomFieldResponseDto;
import com.narangnorang.room.dto.response.RoomResponseDto;
import com.narangnorang.room.entity.OptionType;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.entity.RoomProfileCustomField;
import com.narangnorang.room.entity.RoomProfileCustomFieldOption;
import com.narangnorang.room.repository.RoomProfileCustomFieldRepository;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.space.repository.SpaceRepository;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomProfileCustomFieldRepository customFieldRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberProfileCardRepository memberProfileCardRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private MemberProfileCustomAnswerRepository memberProfileCustomAnswerRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Nested
    @DisplayName("룸 기본 기능")
    class RoomFeatureTest {

    	@Test
    	@DisplayName("룸 생성에 성공한다")
    	void createRoom_success() {
    	    // given
    	    Long userId = 1L;

    	    User owner = User.builder()
    	            .id(userId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    RoomCreateRequestDto requestDto = RoomCreateRequestDto.builder()
    	            .name("테스트 룸")
    	            .description("룸 생성 테스트")
    	            .maxMember(10)
    	            .customFields(List.of())
    	            .build();

    	    Room savedRoom = Room.builder()
    	            .id(10L)
    	            .name("테스트 룸")
    	            .description("룸 생성 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(owner));

    	    when(roomRepository.existsByRoomCode(anyString()))
    	            .thenReturn(false);

    	    when(roomRepository.save(any(Room.class)))
    	            .thenReturn(savedRoom);

    	    when(memberProfileCardRepository.countByRoomId(10L))
    	            .thenReturn(0L);

    	    // when
    	    RoomResponseDto result = roomService.createRoom(requestDto, userId);

    	    // then
    	    assertEquals(10L, result.getId());
    	    assertEquals("테스트 룸", result.getName());
    	    assertEquals("룸 생성 테스트", result.getDescription());
    	    assertEquals(10, result.getMaxMember());
    	    assertEquals("ABC123", result.getRoomCode());
    	    assertEquals(0L, result.getCurrentMember());
    	    assertEquals(userId, result.getOwnerId());
    	    assertEquals("owner", result.getOwnerName());

    	    verify(roomRepository).save(any(Room.class));
    	}

    	@Test
    	@DisplayName("최대 인원이 1명 미만이면 룸 생성에 실패한다")
    	void createRoom_fail_invalidMaxMember() {
    	    // given
    	    Long userId = 1L;

    	    User owner = User.builder()
    	            .id(userId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    RoomCreateRequestDto requestDto = RoomCreateRequestDto.builder()
    	            .name("테스트 룸")
    	            .description("룸 생성 테스트")
    	            .maxMember(0)
    	            .customFields(List.of())
    	            .build();

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(owner));

    	    // when
    	    IllegalArgumentException exception = assertThrows(
    	            IllegalArgumentException.class,
    	            () -> roomService.createRoom(requestDto, userId)
    	    );

    	    // then
    	    assertEquals(
    	            "최대 인원은 1명 이상이어야 합니다.",
    	            exception.getMessage()
    	    );

    	    verify(roomRepository, never()).save(any(Room.class));
    	}

    	@Test
    	@DisplayName("룸 상세 조회에 성공한다")
    	void getRoom_success() {
    	    // given
    	    Long roomId = 10L;
    	    Long userId = 1L;

    	    User user = User.builder()
    	            .id(userId)
    	            .name("member")
    	            .email("member@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("룸 상세 조회 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(user)
    	            .build();

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(user));

    	    when(memberProfileCardRepository
    	            .existsByUserIdAndRoomId(userId, roomId))
    	            .thenReturn(true);

    	    when(memberProfileCardRepository.countByRoomId(roomId))
    	            .thenReturn(3L);

    	    // when
    	    RoomResponseDto result = roomService.getRoom(roomId, userId);

    	    // then
    	    assertEquals(roomId, result.getId());
    	    assertEquals("테스트 룸", result.getName());
    	    assertEquals("룸 상세 조회 테스트", result.getDescription());
    	    assertEquals(10, result.getMaxMember());
    	    assertEquals(3L, result.getCurrentMember());
    	    assertEquals("ABC123", result.getRoomCode());
    	    assertEquals(userId, result.getOwnerId());
    	    assertEquals("member", result.getOwnerName());

    	    verify(roomRepository).findById(roomId);
    	    verify(userRepository).findById(userId);
    	    verify(memberProfileCardRepository)
    	            .existsByUserIdAndRoomId(userId, roomId);
    	    verify(memberProfileCardRepository).countByRoomId(roomId);
    	}

    	@Test
    	@DisplayName("룸 멤버가 아니면 룸 상세 조회에 실패한다")
    	void getRoom_fail_notMember() {
    	    // given
    	    Long roomId = 10L;
    	    Long userId = 2L;

    	    User owner = User.builder()
    	            .id(1L)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    User user = User.builder()
    	            .id(userId)
    	            .name("nonMember")
    	            .email("nonmember@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("룸 상세 조회 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(user));

    	    when(memberProfileCardRepository
    	            .existsByUserIdAndRoomId(userId, roomId))
    	            .thenReturn(false);

    	    // when
    	    IllegalStateException exception = assertThrows(
    	            IllegalStateException.class,
    	            () -> roomService.getRoom(roomId, userId)
    	    );

    	    // then
    	    assertEquals(
    	            "룸 멤버만 상세 정보를 조회할 수 있습니다.",
    	            exception.getMessage()
    	    );

    	    verify(memberProfileCardRepository, never())
    	            .countByRoomId(roomId);
    	}

    	@Test
    	@DisplayName("룸 코드로 참여할 룸 조회에 성공한다")
    	void getRoomForJoin_success() {
    	    // given
    	    String roomCode = "ABC123";
    	    Long roomId = 10L;
    	    Long userId = 2L;

    	    User owner = User.builder()
    	            .id(1L)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    User user = User.builder()
    	            .id(userId)
    	            .name("member")
    	            .email("member@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("룸 참여 조회 테스트")
    	            .maxMember(10)
    	            .roomCode(roomCode)
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(user));

    	    when(roomRepository.findByRoomCode(roomCode))
    	            .thenReturn(Optional.of(room));

    	    when(memberProfileCardRepository
    	            .existsByUserIdAndRoomId(userId, roomId))
    	            .thenReturn(false);

    	    when(memberProfileCardRepository.countByRoomId(roomId))
    	            .thenReturn(3L);

    	    // when
    	    RoomJoinResponseDto result =
    	            roomService.getRoomForJoin(roomCode, userId);

    	    // then
    	    assertFalse(result.isMember());
    	    assertEquals(roomId, result.getRoom().getId());
    	    assertEquals("테스트 룸", result.getRoom().getName());
    	    assertEquals(3L, result.getRoom().getCurrentMember());
    	    assertEquals(roomCode, result.getRoom().getRoomCode());

    	    verify(userRepository).findById(userId);
    	    verify(roomRepository).findByRoomCode(roomCode);
    	    verify(memberProfileCardRepository)
    	            .existsByUserIdAndRoomId(userId, roomId);
    	    verify(memberProfileCardRepository).countByRoomId(roomId);
    	}
    	
    	@Test
    	@DisplayName("존재하지 않는 룸 코드이면 참여할 룸 조회에 실패한다")
    	void getRoomForJoin_fail_roomNotFound() {
    	    // given
    	    String roomCode = "WRONG1";
    	    Long userId = 2L;

    	    User user = User.builder()
    	            .id(userId)
    	            .name("member")
    	            .email("member@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(user));

    	    when(roomRepository.findByRoomCode(roomCode))
    	            .thenReturn(Optional.empty());

    	    // when
    	    IllegalArgumentException exception = assertThrows(
    	            IllegalArgumentException.class,
    	            () -> roomService.getRoomForJoin(roomCode, userId)
    	    );

    	    // then
    	    assertEquals(
    	            "룸 코드를 확인해 주세요.",
    	            exception.getMessage()
    	    );

    	    verify(userRepository).findById(userId);
    	    verify(roomRepository).findByRoomCode(roomCode);

    	    verify(memberProfileCardRepository, never())
    	            .existsByUserIdAndRoomId(anyLong(), anyLong());

    	    verify(memberProfileCardRepository, never())
    	            .countByRoomId(anyLong());
    	}

    	@Test
    	@DisplayName("룸 오너는 룸 정보 수정에 성공한다")
    	void updateRoom_success() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("수정 전 룸")
    	            .description("수정 전 설명")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    RoomUpdateRequestDto requestDto =
    	            RoomUpdateRequestDto.builder()
    	                    .name("수정된 룸")
    	                    .description("수정된 설명")
    	                    .maxMember(20)
    	                    .build();

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    when(memberProfileCardRepository.countByRoomId(roomId))
    	            .thenReturn(3L);

    	    // when
    	    RoomResponseDto result =
    	            roomService.updateRoom(roomId, requestDto, ownerId);

    	    // then
    	    assertEquals("수정된 룸", result.getName());
    	    assertEquals("수정된 설명", result.getDescription());
    	    assertEquals(20, result.getMaxMember());
    	    assertEquals(3L, result.getCurrentMember());

    	    // 실제 Room 엔티티의 값도 변경됐는지 확인
    	    assertEquals("수정된 룸", room.getName());
    	    assertEquals("수정된 설명", room.getDescription());
    	    assertEquals(20, room.getMaxMember());

    	    verify(roomRepository).findById(roomId);
    	    verify(userRepository).findById(ownerId);
    	    verify(memberProfileCardRepository).countByRoomId(roomId);
    	}

    	@Test
    	@DisplayName("룸 오너가 아니면 룸 정보 수정에 실패한다")
    	void updateRoom_fail_notOwner() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;
    	    Long userId = 2L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    User user = User.builder()
    	            .id(userId)
    	            .name("member")
    	            .email("member@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("수정 전 룸")
    	            .description("수정 전 설명")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    RoomUpdateRequestDto requestDto =
    	            RoomUpdateRequestDto.builder()
    	                    .name("수정된 룸")
    	                    .description("수정된 설명")
    	                    .maxMember(20)
    	                    .build();

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(user));

    	    // when
    	    IllegalStateException exception = assertThrows(
    	            IllegalStateException.class,
    	            () -> roomService.updateRoom(roomId, requestDto, userId)
    	    );

    	    // then
    	    assertEquals(
    	            "룸 오너만 수행할 수 있습니다.",
    	            exception.getMessage()
    	    );

    	    // 룸 정보가 변경되지 않았는지 확인
    	    assertEquals("수정 전 룸", room.getName());
    	    assertEquals("수정 전 설명", room.getDescription());
    	    assertEquals(10, room.getMaxMember());

    	    // 권한 검증에서 실패하므로 현재 인원도 조회하지 않음
    	    verify(memberProfileCardRepository, never())
    	            .countByRoomId(roomId);
    	}

    	@Test
    	@DisplayName("최대 인원이 현재 인원보다 작으면 룸 정보 수정에 실패한다")
    	void updateRoom_fail_maxMemberLessThanCurrentMembers() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("수정 전 룸")
    	            .description("수정 전 설명")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    // 현재 인원은 5명이지만 최대 인원을 3명으로 변경
    	    RoomUpdateRequestDto requestDto =
    	            RoomUpdateRequestDto.builder()
    	                    .name("수정된 룸")
    	                    .description("수정된 설명")
    	                    .maxMember(3)
    	                    .build();

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    when(memberProfileCardRepository.countByRoomId(roomId))
    	            .thenReturn(5L);

    	    // when
    	    IllegalArgumentException exception = assertThrows(
    	            IllegalArgumentException.class,
    	            () -> roomService.updateRoom(roomId, requestDto, ownerId)
    	    );

    	    // then
    	    assertEquals(
    	            "최대 인원은 현재 인원보다 작을 수 없습니다.",
    	            exception.getMessage()
    	    );

    	    // 수정에 실패했으므로 기존 정보가 유지되는지 확인
    	    assertEquals("수정 전 룸", room.getName());
    	    assertEquals("수정 전 설명", room.getDescription());
    	    assertEquals(10, room.getMaxMember());

    	    verify(memberProfileCardRepository).countByRoomId(roomId);
    	}

    	@Test
    	@DisplayName("룸 오너는 룸 삭제에 성공한다")
    	void deleteRoom_success() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("삭제할 룸")
    	            .description("룸 삭제 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    when(spaceRepository.findByRoomId(roomId))
    	            .thenReturn(List.of());

    	    // when
    	    roomService.deleteRoom(roomId, ownerId);

    	    // then
    	    verify(roomRepository).findById(roomId);
    	    verify(userRepository).findById(ownerId);

    	    verify(spaceRepository).findByRoomId(roomId);
    	    verify(spaceRepository).deleteAll(List.of());
    	    verify(spaceRepository).flush();

    	    verify(roomRepository).delete(room);
    	}

    	@Test
    	@DisplayName("룸 오너가 아니면 룸 삭제에 실패한다")
    	void deleteRoom_fail_notOwner() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;
    	    Long userId = 2L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    User user = User.builder()
    	            .id(userId)
    	            .name("member")
    	            .email("member@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("삭제할 룸")
    	            .description("룸 삭제 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(user));

    	    // when
    	    IllegalStateException exception = assertThrows(
    	            IllegalStateException.class,
    	            () -> roomService.deleteRoom(roomId, userId)
    	    );

    	    // then
    	    assertEquals(
    	            "룸 오너만 수행할 수 있습니다.",
    	            exception.getMessage()
    	    );

    	    verify(spaceRepository, never()).findByRoomId(roomId);
    	    verify(spaceRepository, never()).deleteAll(anyList());
    	    verify(spaceRepository, never()).flush();

    	    verify(roomRepository, never()).delete(any(Room.class));
    	}
    }

    @Nested
    @DisplayName("커스텀 필드 기능")
    class CustomFieldFeatureTest {

    	@Test
    	@DisplayName("룸 오너는 신규 커스텀 필드 추가에 성공한다")
    	void updateCustomFields_success_addField() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("커스텀 필드 추가 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    // id=null이면 신규 필드로 처리됨
    	    RoomProfileCustomFieldBulkUpdateRequestDto addRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(null)
    	                    .fieldName("닉네임")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .options(List.of())
    	                    .build();

    	    RoomProfileCustomFieldsUpdateRequestDto requestDto =
    	            new RoomProfileCustomFieldsUpdateRequestDto(
    	                    List.of(addRequest)
    	            );

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    // when
    	    List<RoomProfileCustomFieldResponseDto> result =
    	            roomService.updateCustomFields(
    	                    roomId,
    	                    requestDto,
    	                    ownerId
    	            );

    	    // then
    	    assertEquals(1, result.size());
    	    assertEquals("닉네임", result.get(0).getFieldName());
    	    assertEquals(true, result.get(0).isRequired());
    	    assertEquals(OptionType.TEXT, result.get(0).getOptionType());
    	    assertEquals(0, result.get(0).getOptions().size());

    	    // 실제 Room 객체에도 필드가 추가됐는지 확인
    	    assertEquals(1, room.getCustomFields().size());
    	    assertEquals(
    	            "닉네임",
    	            room.getCustomFields().get(0).getFieldName()
    	    );

    	    verify(customFieldRepository).flush();
    	}

    	@Test
    	@DisplayName("룸 오너가 아니면 신규 커스텀 필드 추가에 실패한다")
    	void updateCustomFields_fail_notOwner() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;
    	    Long userId = 2L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    User user = User.builder()
    	            .id(userId)
    	            .name("member")
    	            .email("member@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("커스텀 필드 권한 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    RoomProfileCustomFieldBulkUpdateRequestDto addRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(null)
    	                    .fieldName("닉네임")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .options(List.of())
    	                    .build();

    	    RoomProfileCustomFieldsUpdateRequestDto requestDto =
    	            new RoomProfileCustomFieldsUpdateRequestDto(
    	                    List.of(addRequest)
    	            );

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(userId))
    	            .thenReturn(Optional.of(user));

    	    // when
    	    IllegalStateException exception = assertThrows(
    	            IllegalStateException.class,
    	            () -> roomService.updateCustomFields(
    	                    roomId,
    	                    requestDto,
    	                    userId
    	            )
    	    );

    	    // then
    	    assertEquals(
    	            "룸 오너만 수행할 수 있습니다.",
    	            exception.getMessage()
    	    );

    	    // 필드가 추가되지 않았는지 확인
    	    assertEquals(0, room.getCustomFields().size());

    	    // 권한 검증에서 실패했으므로 DB 반영도 실행되지 않음
    	    verify(customFieldRepository, never()).flush();

    	    verify(memberProfileCustomAnswerRepository, never())
    	            .deleteByRoomProfileCustomFieldId(anyLong());
    	}
    	
    	@Test
    	@DisplayName("기존 커스텀 필드를 유지하고 요청에서 빠진 필드는 삭제한다")
    	void updateCustomFields_success_keepAndDelete() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("커스텀 필드 삭제 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    RoomProfileCustomField keepField =
    	            RoomProfileCustomField.builder()
    	                    .id(11L)
    	                    .fieldName("닉네임")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .build();

    	    RoomProfileCustomField deleteField =
    	            RoomProfileCustomField.builder()
    	                    .id(12L)
    	                    .fieldName("개발 경력")
    	                    .required(false)
    	                    .optionType(OptionType.TEXT)
    	                    .build();

    	    room.addCustomField(keepField);
    	    room.addCustomField(deleteField);

    	    // 유지할 11번 필드만 요청에 포함
    	    RoomProfileCustomFieldBulkUpdateRequestDto keepRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(11L)
    	                    .fieldName("닉네임")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .options(List.of())
    	                    .build();

    	    RoomProfileCustomFieldsUpdateRequestDto requestDto =
    	            new RoomProfileCustomFieldsUpdateRequestDto(
    	                    List.of(keepRequest)
    	            );

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    when(customFieldRepository.findById(11L))
    	            .thenReturn(Optional.of(keepField));

    	    // when
    	    List<RoomProfileCustomFieldResponseDto> result =
    	            roomService.updateCustomFields(
    	                    roomId,
    	                    requestDto,
    	                    ownerId
    	            );

    	    // then
    	    assertEquals(1, result.size());
    	    assertEquals(11L, result.get(0).getId());
    	    assertEquals("닉네임", result.get(0).getFieldName());

    	    // Room 내부에도 유지할 필드만 남아 있는지 확인
    	    assertEquals(1, room.getCustomFields().size());
    	    assertEquals(keepField, room.getCustomFields().get(0));
    	    assertFalse(room.getCustomFields().contains(deleteField));

    	    // 삭제된 필드에 연결된 멤버 답변을 삭제했는지 확인
    	    verify(memberProfileCustomAnswerRepository)
    	            .deleteByRoomProfileCustomFieldId(12L);

    	    verify(customFieldRepository).flush();
    	}
    	
    	@Test
    	@DisplayName("기존 커스텀 필드 내용을 변경하면 실패한다")
    	void updateCustomFields_fail_changedExistingField() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;
    	    Long fieldId = 11L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("커스텀 필드 수정 방지 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    RoomProfileCustomField existingField =
    	            RoomProfileCustomField.builder()
    	                    .id(fieldId)
    	                    .fieldName("희망 직무")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .build();

    	    room.addCustomField(existingField);

    	    // ID는 같지만 필드 이름을 변경함
    	    RoomProfileCustomFieldBulkUpdateRequestDto changedRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(fieldId)
    	                    .fieldName("희망 운동")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .options(List.of())
    	                    .build();

    	    RoomProfileCustomFieldsUpdateRequestDto requestDto =
    	            new RoomProfileCustomFieldsUpdateRequestDto(
    	                    List.of(changedRequest)
    	            );

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    when(customFieldRepository.findById(fieldId))
    	            .thenReturn(Optional.of(existingField));

    	    // when
    	    IllegalArgumentException exception = assertThrows(
    	            IllegalArgumentException.class,
    	            () -> roomService.updateCustomFields(
    	                    roomId,
    	                    requestDto,
    	                    ownerId
    	            )
    	    );

    	    // then
    	    assertEquals(
    	            "기존 커스텀 필드는 수정할 수 없습니다. 삭제 후 새로 추가해 주세요.",
    	            exception.getMessage()
    	    );

    	    // 기존 필드가 변경되지 않았는지 확인
    	    assertEquals("희망 직무", existingField.getFieldName());
    	    assertEquals(1, room.getCustomFields().size());

    	    // 검증에서 실패했으므로 삭제나 DB 반영이 실행되지 않음
    	    verify(memberProfileCustomAnswerRepository, never())
    	            .deleteByRoomProfileCustomFieldId(anyLong());

    	    verify(customFieldRepository, never()).flush();
    	}
    	
    	@Test
    	@DisplayName("기존 커스텀 필드의 선택지를 변경하면 실패한다")
    	void updateCustomFields_fail_changedExistingOption() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;
    	    Long fieldId = 11L;
    	    Long optionId = 21L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("선택지 수정 방지 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    RoomProfileCustomField existingField =
    	            RoomProfileCustomField.builder()
    	                    .id(fieldId)
    	                    .fieldName("희망 직무")
    	                    .required(true)
    	                    .optionType(OptionType.SINGLE_SELECT)
    	                    .build();

    	    RoomProfileCustomFieldOption existingOption =
    	            RoomProfileCustomFieldOption.builder()
    	                    .id(optionId)
    	                    .optionValue("백엔드")
    	                    .displayOrder(1)
    	                    .build();

    	    existingField.addOption(existingOption);
    	    room.addCustomField(existingField);

    	    // 선택지 ID는 같지만 값을 변경
    	    RoomProfileCustomFieldOptionUpdateRequestDto changedOption =
    	            RoomProfileCustomFieldOptionUpdateRequestDto.builder()
    	                    .id(optionId)
    	                    .optionValue("프론트엔드")
    	                    .displayOrder(1)
    	                    .build();

    	    RoomProfileCustomFieldBulkUpdateRequestDto changedRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(fieldId)
    	                    .fieldName("희망 직무")
    	                    .required(true)
    	                    .optionType(OptionType.SINGLE_SELECT)
    	                    .options(List.of(changedOption))
    	                    .build();

    	    RoomProfileCustomFieldsUpdateRequestDto requestDto =
    	            new RoomProfileCustomFieldsUpdateRequestDto(
    	                    List.of(changedRequest)
    	            );

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    when(customFieldRepository.findById(fieldId))
    	            .thenReturn(Optional.of(existingField));

    	    // when
    	    IllegalArgumentException exception = assertThrows(
    	            IllegalArgumentException.class,
    	            () -> roomService.updateCustomFields(
    	                    roomId,
    	                    requestDto,
    	                    ownerId
    	            )
    	    );

    	    // then
    	    assertEquals(
    	            "기존 커스텀 필드는 수정할 수 없습니다. 삭제 후 새로 추가해 주세요.",
    	            exception.getMessage()
    	    );

    	    // 기존 선택지 값이 그대로인지 확인
    	    assertEquals(
    	            "백엔드",
    	            existingField.getOptions().get(0).getOptionValue()
    	    );

    	    verify(memberProfileCustomAnswerRepository, never())
    	            .deleteByRoomProfileCustomFieldId(anyLong());

    	    verify(customFieldRepository, never()).flush();
    	}
    	
    	@Test
    	@DisplayName("동일한 커스텀 필드 ID가 중복되면 실패한다")
    	void updateCustomFields_fail_duplicateFieldId() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;
    	    Long fieldId = 11L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("중복 필드 ID 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    RoomProfileCustomField existingField =
    	            RoomProfileCustomField.builder()
    	                    .id(fieldId)
    	                    .fieldName("닉네임")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .build();

    	    room.addCustomField(existingField);

    	    RoomProfileCustomFieldBulkUpdateRequestDto firstRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(fieldId)
    	                    .fieldName("닉네임")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .options(List.of())
    	                    .build();

    	    // 동일한 fieldId를 한 번 더 전송
    	    RoomProfileCustomFieldBulkUpdateRequestDto duplicateRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(fieldId)
    	                    .fieldName("닉네임")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .options(List.of())
    	                    .build();

    	    RoomProfileCustomFieldsUpdateRequestDto requestDto =
    	            new RoomProfileCustomFieldsUpdateRequestDto(
    	                    List.of(firstRequest, duplicateRequest)
    	            );

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    when(customFieldRepository.findById(fieldId))
    	            .thenReturn(Optional.of(existingField));

    	    // when
    	    IllegalArgumentException exception = assertThrows(
    	            IllegalArgumentException.class,
    	            () -> roomService.updateCustomFields(
    	                    roomId,
    	                    requestDto,
    	                    ownerId
    	            )
    	    );

    	    // then
    	    assertEquals(
    	            "중복된 커스텀 필드 ID가 존재합니다.",
    	            exception.getMessage()
    	    );

    	    // 중복 검증에서 실패했으므로 기존 필드는 그대로 유지
    	    assertEquals(1, room.getCustomFields().size());
    	    assertEquals(existingField, room.getCustomFields().get(0));

    	    verify(memberProfileCustomAnswerRepository, never())
    	            .deleteByRoomProfileCustomFieldId(anyLong());

    	    verify(customFieldRepository, never()).flush();
    	}
    	
    	@Test
    	@DisplayName("다른 룸의 커스텀 필드를 요청하면 실패한다")
    	void updateCustomFields_fail_fieldBelongsToOtherRoom() {
    	    // given
    	    Long roomId = 10L;
    	    Long otherRoomId = 20L;
    	    Long ownerId = 1L;
    	    Long fieldId = 11L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    // 현재 수정하려는 룸
    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("현재 룸")
    	            .description("현재 룸 설명")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    // 커스텀 필드가 실제로 속한 다른 룸
    	    Room otherRoom = Room.builder()
    	            .id(otherRoomId)
    	            .name("다른 룸")
    	            .description("다른 룸 설명")
    	            .maxMember(10)
    	            .roomCode("DEF456")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    RoomProfileCustomField otherRoomField =
    	            RoomProfileCustomField.builder()
    	                    .id(fieldId)
    	                    .fieldName("다른 룸 필드")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .build();

    	    // 필드의 room을 otherRoom으로 설정
    	    otherRoom.addCustomField(otherRoomField);

    	    RoomProfileCustomFieldBulkUpdateRequestDto fieldRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(fieldId)
    	                    .fieldName("다른 룸 필드")
    	                    .required(true)
    	                    .optionType(OptionType.TEXT)
    	                    .options(List.of())
    	                    .build();

    	    RoomProfileCustomFieldsUpdateRequestDto requestDto =
    	            new RoomProfileCustomFieldsUpdateRequestDto(
    	                    List.of(fieldRequest)
    	            );

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    when(customFieldRepository.findById(fieldId))
    	            .thenReturn(Optional.of(otherRoomField));

    	    // when
    	    IllegalArgumentException exception = assertThrows(
    	            IllegalArgumentException.class,
    	            () -> roomService.updateCustomFields(
    	                    roomId,
    	                    requestDto,
    	                    ownerId
    	            )
    	    );

    	    // then
    	    assertEquals(
    	            "해당 룸의 커스텀 필드가 아닙니다.",
    	            exception.getMessage()
    	    );

    	    verify(memberProfileCustomAnswerRepository, never())
    	            .deleteByRoomProfileCustomFieldId(anyLong());

    	    verify(customFieldRepository, never()).flush();
    	}
    	
    	@Test
    	@DisplayName("선택형 커스텀 필드에 선택지가 없으면 실패한다")
    	void updateCustomFields_fail_invalidOptions() {
    	    // given
    	    Long roomId = 10L;
    	    Long ownerId = 1L;

    	    User owner = User.builder()
    	            .id(ownerId)
    	            .name("owner")
    	            .email("owner@test.com")
    	            .password("1234")
    	            .userRoles(List.of())
    	            .build();

    	    Room room = Room.builder()
    	            .id(roomId)
    	            .name("테스트 룸")
    	            .description("선택지 검증 테스트")
    	            .maxMember(10)
    	            .roomCode("ABC123")
    	            .createdAt(LocalDateTime.now())
    	            .owner(owner)
    	            .build();

    	    // SINGLE_SELECT인데 선택지가 비어 있음
    	    RoomProfileCustomFieldBulkUpdateRequestDto invalidRequest =
    	            RoomProfileCustomFieldBulkUpdateRequestDto.builder()
    	                    .id(null)
    	                    .fieldName("희망 직무")
    	                    .required(true)
    	                    .optionType(OptionType.SINGLE_SELECT)
    	                    .options(List.of())
    	                    .build();

    	    RoomProfileCustomFieldsUpdateRequestDto requestDto =
    	            new RoomProfileCustomFieldsUpdateRequestDto(
    	                    List.of(invalidRequest)
    	            );

    	    when(roomRepository.findById(roomId))
    	            .thenReturn(Optional.of(room));

    	    when(userRepository.findById(ownerId))
    	            .thenReturn(Optional.of(owner));

    	    // when
    	    IllegalArgumentException exception = assertThrows(
    	            IllegalArgumentException.class,
    	            () -> roomService.updateCustomFields(
    	                    roomId,
    	                    requestDto,
    	                    ownerId
    	            )
    	    );

    	    // then
    	    assertEquals(
    	            "SINGLE_SELECT와 MULTI_SELECT 타입은 선택지가 최소 1개 필요합니다.",
    	            exception.getMessage()
    	    );

    	    // 검증 실패로 필드가 추가되지 않음
    	    assertEquals(0, room.getCustomFields().size());

    	    verify(customFieldRepository, never()).flush();

    	    verify(memberProfileCustomAnswerRepository, never())
    	            .deleteByRoomProfileCustomFieldId(anyLong());
    	}
    }
}
