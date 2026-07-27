package com.narangnorang.space.service;

import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.space.dto.request.SpaceCreateRequestDto;
import com.narangnorang.space.dto.request.SpaceUpdateRequestDto;
import com.narangnorang.space.dto.response.SpaceProfileCardResponseDto;
import com.narangnorang.space.dto.response.SpaceSummaryResponseDto;
import com.narangnorang.space.entity.Space;
import com.narangnorang.space.entity.SpaceMember;
import com.narangnorang.space.entity.SpaceProfileCard;
import com.narangnorang.space.exception.errorcode.SpaceErrorCode;
import com.narangnorang.space.exception.SpaceException;
import com.narangnorang.space.repository.SpaceMemberRepository;
import com.narangnorang.space.repository.SpaceRepository;
import com.narangnorang.space.repository.TagRepository;
import com.narangnorang.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MemberProfileCardRepository memberProfileCardRepository;

    @Mock
    private SpaceMemberRepository spaceMemberRepository;

    @InjectMocks
    private SpaceServiceImpl spaceService;

    @Nested
    @DisplayName("getSpaceList")
    class GetSpaceList {

        @Test
        @DisplayName("태그 필터 없이 조회 성공")
        void success_NoTags() {
            Long roomId = 10L;
            Long userId = 1L;

            Space space = Space.builder().id(1L).name("space1").roomId(roomId).ownerId(userId).build();

            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId)).thenReturn(true);
            when(spaceRepository.findByRoomIdWithTags(roomId)).thenReturn(List.of(space));

            List<SpaceSummaryResponseDto> result = spaceService.getSpaceList(roomId, userId, null);

            assertEquals(1, result.size());
            verify(spaceRepository, times(1)).findByRoomIdWithTags(roomId);
            verify(spaceRepository, never()).findByRoomIdAndTagNamesWithTags(any(), any());
        }

        @Test
        @DisplayName("태그 필터를 걸면 태그 조건 조회가 호출된다")
        void success_WithTags() {
            Long roomId = 10L;
            Long userId = 1L;
            List<String> tags = List.of("java", "spring");

            Space space = Space.builder().id(1L).name("space1").roomId(roomId).ownerId(userId).build();

            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId)).thenReturn(true);
            when(spaceRepository.findByRoomIdAndTagNamesWithTags(roomId, tags)).thenReturn(List.of(space));

            List<SpaceSummaryResponseDto> result = spaceService.getSpaceList(roomId, userId, tags);

            assertEquals(1, result.size());
            verify(spaceRepository, times(1)).findByRoomIdAndTagNamesWithTags(roomId, tags);
            verify(spaceRepository, never()).findByRoomIdWithTags(any());
        }

        @Test
        @DisplayName("룸 멤버 아님 실패")
        void notRoomMember() {
            Long roomId = 10L;
            Long userId = 1L;

            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId)).thenReturn(false);

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.getSpaceList(roomId, userId, null));
            assertEquals(SpaceErrorCode.NOT_ROOM_MEMBER, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("getSpaceDetail")
    class GetSpaceDetail {

        @Test
        @DisplayName("룸 멤버 아님 실패")
        void notRoomMember() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId)).thenReturn(false);

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.getSpaceDetail(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.NOT_ROOM_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("존재하지 않는 스페이스 실패")
        void spaceNotFound() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId)).thenReturn(true);
            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.getSpaceDetail(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.SPACE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("다른 룸 소속 스페이스 실패")
        void roomMismatch() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(999L).ownerId(userId).build();

            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId)).thenReturn(true);
            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.of(space));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.getSpaceDetail(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.SPACE_ROOM_MISMATCH, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 조회 성공")
        void success() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long ownerId = 1L;

            Space space = Space.builder()
                    .id(spaceId).roomId(roomId).ownerId(ownerId)
                    .currentMemberCount(1L).maxMemberCount(5L)
                    .build();

            SpaceProfileCard profileCard = mock(SpaceProfileCard.class);
            space.assignProfileCard(profileCard);

            MemberProfileCard ownerCard = mock(MemberProfileCard.class);
            when(ownerCard.getName()).thenReturn("현빈");
            SpaceMember ownerSpaceMember = SpaceMember.toSpaceMemberEntity(space, ownerId, ownerCard);

            when(memberProfileCardRepository.existsByUserIdAndRoomId(ownerId, roomId)).thenReturn(true);
            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.of(space));
            when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, ownerId)).thenReturn(Optional.of(ownerSpaceMember));

            SpaceProfileCardResponseDto result = spaceService.getSpaceDetail(roomId, spaceId, ownerId);

            assertNotNull(result);
            assertEquals("현빈", result.getOwner());
        }
    }

    @Nested
    @DisplayName("createSpace")
    class CreateSpace {

        @Test
        @DisplayName("룸 멤버 아님 실패")
        void notRoomMember() {
            Long roomId = 10L;
            Long ownerId = 1L;
            SpaceCreateRequestDto request = SpaceCreateRequestDto.builder().name("space1").build();

            when(memberProfileCardRepository.findByUserIdAndRoomId(ownerId, roomId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.createSpace(roomId, ownerId, request));
            assertEquals(SpaceErrorCode.NOT_ROOM_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 생성 성공")
        void success() {
            Long roomId = 10L;
            Long ownerId = 1L;

            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);
            when(memberProfileCard.getName()).thenReturn("현빈");

            Space newSpace = Space.builder().id(2L).roomId(roomId).ownerId(ownerId).maxMemberCount(5L).build();
            SpaceProfileCard profileCard = mock(SpaceProfileCard.class);

            SpaceCreateRequestDto request = mock(SpaceCreateRequestDto.class);
            when(request.getTags()).thenReturn(List.of("java"));
            when(request.toSpaceEntity(roomId, ownerId)).thenReturn(newSpace);
            when(request.toProfileCardEntity(newSpace)).thenReturn(profileCard);

            when(memberProfileCardRepository.findByUserIdAndRoomId(ownerId, roomId))
                    .thenReturn(Optional.of(memberProfileCard));

            SpaceProfileCardResponseDto result = spaceService.createSpace(roomId, ownerId, request);

            assertNotNull(result);
            assertEquals("현빈", result.getOwner());
            verify(spaceRepository, times(1)).save(newSpace);
        }
    }

    @Nested
    @DisplayName("deleteSpace")
    class DeleteSpace {

        @Test
        @DisplayName("존재하지 않는 스페이스 실패")
        void spaceNotFound() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.deleteSpace(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.SPACE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("다른 룸 소속 스페이스 실패")
        void roomMismatch() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(999L).ownerId(userId).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.deleteSpace(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.SPACE_ROOM_MISMATCH, ex.getErrorCode());
        }

        @Test
        @DisplayName("존재하지 않는 룸 실패")
        void roomNotFound() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(2L).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.deleteSpace(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("관리 권한 없음 실패")
        void noManagePermission() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(2L).build();
            User otherOwner = User.builder().id(999L).build();
            Room room = Room.builder().id(roomId).owner(otherOwner).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.deleteSpace(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.NO_MANAGE_PERMISSION, ex.getErrorCode());
        }

        @Test
        @DisplayName("스페이스 오너면 삭제 성공")
        void success_SpaceOwner() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(userId).build();
            User otherOwner = User.builder().id(999L).build();
            Room room = Room.builder().id(roomId).owner(otherOwner).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            spaceService.deleteSpace(roomId, spaceId, userId);

            verify(spaceRepository, times(1)).delete(space);
        }

        @Test
        @DisplayName("룸 오너면 삭제 성공")
        void success_RoomOwner() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(999L).build();
            User roomOwner = User.builder().id(userId).build();
            Room room = Room.builder().id(roomId).owner(roomOwner).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            spaceService.deleteSpace(roomId, spaceId, userId);

            verify(spaceRepository, times(1)).delete(space);
        }
    }

    @Nested
    @DisplayName("updateSpaceCard")
    class UpdateSpaceCard {

        @Test
        @DisplayName("존재하지 않는 스페이스 실패")
        void spaceNotFound() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;
            SpaceUpdateRequestDto request = mock(SpaceUpdateRequestDto.class);

            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.updateSpaceCard(roomId, spaceId, userId, request));
            assertEquals(SpaceErrorCode.SPACE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("다른 룸 소속 스페이스 실패")
        void roomMismatch() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;
            SpaceUpdateRequestDto request = mock(SpaceUpdateRequestDto.class);

            Space space = Space.builder().id(spaceId).roomId(999L).ownerId(userId).build();

            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.of(space));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.updateSpaceCard(roomId, spaceId, userId, request));
            assertEquals(SpaceErrorCode.SPACE_ROOM_MISMATCH, ex.getErrorCode());
        }

        @Test
        @DisplayName("존재하지 않는 룸 실패")
        void roomNotFound() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;
            SpaceUpdateRequestDto request = mock(SpaceUpdateRequestDto.class);

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(2L).build();

            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.updateSpaceCard(roomId, spaceId, userId, request));
            assertEquals(SpaceErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("관리 권한 없음 실패")
        void noManagePermission() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;
            SpaceUpdateRequestDto request = mock(SpaceUpdateRequestDto.class);

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(2L).build();
            User otherOwner = User.builder().id(999L).build();
            Room room = Room.builder().id(roomId).owner(otherOwner).build();

            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.updateSpaceCard(roomId, spaceId, userId, request));
            assertEquals(SpaceErrorCode.NO_MANAGE_PERMISSION, ex.getErrorCode());
        }

        @Test
        @DisplayName("태그 수정 요청이 있으면 태그를 재등록한다")
        void success_WithNewTags() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(userId).build();
            SpaceProfileCard profileCard = mock(SpaceProfileCard.class);
            space.assignProfileCard(profileCard);

            MemberProfileCard ownerCard = mock(MemberProfileCard.class);
            when(ownerCard.getName()).thenReturn("현빈");
            SpaceMember ownerSpaceMember = SpaceMember.toSpaceMemberEntity(space, userId, ownerCard);

            User roomOwner = User.builder().id(userId).build();
            Room room = Room.builder().id(roomId).owner(roomOwner).build();

            SpaceUpdateRequestDto request = mock(SpaceUpdateRequestDto.class);
            when(request.getTags()).thenReturn(List.of("newTag"));

            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)).thenReturn(Optional.of(ownerSpaceMember));

            SpaceProfileCardResponseDto result = spaceService.updateSpaceCard(roomId, spaceId, userId, request);

            assertNotNull(result);
            verify(tagRepository, times(1)).deleteBySpaceId(spaceId);
            verify(tagRepository, times(1)).saveAll(any());
        }

        @Test
        @DisplayName("태그 수정 요청이 없으면 기존 태그를 유지한다")
        void success_KeepExistingTags() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(userId).build();
            SpaceProfileCard profileCard = mock(SpaceProfileCard.class);
            space.assignProfileCard(profileCard);
            space.addTag("java");

            MemberProfileCard ownerCard = mock(MemberProfileCard.class);
            when(ownerCard.getName()).thenReturn("현빈");
            SpaceMember ownerSpaceMember = SpaceMember.toSpaceMemberEntity(space, userId, ownerCard);

            User roomOwner = User.builder().id(userId).build();
            Room room = Room.builder().id(roomId).owner(roomOwner).build();

            SpaceUpdateRequestDto request = mock(SpaceUpdateRequestDto.class);
            when(request.getTags()).thenReturn(null);

            when(spaceRepository.findByIdWithProfileCard(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)).thenReturn(Optional.of(ownerSpaceMember));

            SpaceProfileCardResponseDto result = spaceService.updateSpaceCard(roomId, spaceId, userId, request);

            assertNotNull(result);
            verify(tagRepository, never()).deleteBySpaceId(any());
            verify(tagRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("getRoomTagNames")
    class GetRoomTagNames {

        @Test
        @DisplayName("정상적으로 태그 이름 목록이 조회된다")
        void success() {
            Long roomId = 10L;

            when(tagRepository.findTagNamesByRoomId(roomId)).thenReturn(List.of("java", "spring"));

            List<String> result = spaceService.getRoomTagNames(roomId);

            assertEquals(List.of("java", "spring"), result);
        }
    }

    @Nested
    @DisplayName("leaveSpace")
    class LeaveSpace {

        @Test
        @DisplayName("존재하지 않는 스페이스 실패")
        void spaceNotFound() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.leaveSpace(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.SPACE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("다른 룸 소속 스페이스 실패")
        void roomMismatch() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(999L).ownerId(2L).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.leaveSpace(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.SPACE_ROOM_MISMATCH, ex.getErrorCode());
        }

        @Test
        @DisplayName("오너는 탈퇴 불가 실패")
        void ownerCannotLeave() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(userId).build();
            SpaceMember spaceMember = SpaceMember.builder().id(1L).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)).thenReturn(Optional.of(spaceMember));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.leaveSpace(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.OWNER_CANNOT_LEAVE, ex.getErrorCode());
        }

        @Test
        @DisplayName("스페이스 멤버 아님 실패")
        void notSpaceMember() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 2L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(1L).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.leaveSpace(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.NOT_SPACE_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 탈퇴 성공")
        void success() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 2L;

            Space space = Space.builder()
                    .id(spaceId).roomId(roomId).ownerId(1L)
                    .currentMemberCount(2L).maxMemberCount(5L)
                    .build();
            SpaceMember spaceMember = SpaceMember.builder().id(1L).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)).thenReturn(Optional.of(spaceMember));

            spaceService.leaveSpace(roomId, spaceId, userId);

            verify(spaceMemberRepository, times(1)).delete(spaceMember);
            assertEquals(1L, space.getCurrentMemberCount());
        }
    }

    @Nested
    @DisplayName("transferOwner")
    class TransferOwner {

        @Test
        @DisplayName("존재하지 않는 스페이스 실패")
        void spaceNotFound() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long currentOwnerId = 1L;
            Long newOwnerId = 2L;

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.transferOwner(roomId, spaceId, currentOwnerId, newOwnerId));
            assertEquals(SpaceErrorCode.SPACE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("다른 룸 소속 스페이스 실패")
        void roomMismatch() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long currentOwnerId = 1L;
            Long newOwnerId = 2L;

            Space space = Space.builder().id(spaceId).roomId(999L).ownerId(currentOwnerId).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.transferOwner(roomId, spaceId, currentOwnerId, newOwnerId));
            assertEquals(SpaceErrorCode.SPACE_ROOM_MISMATCH, ex.getErrorCode());
        }

        @Test
        @DisplayName("존재하지 않는 룸 실패")
        void roomNotFound() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long currentOwnerId = 1L;
            Long newOwnerId = 2L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(currentOwnerId).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.transferOwner(roomId, spaceId, currentOwnerId, newOwnerId));
            assertEquals(SpaceErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("관리 권한 없음 실패")
        void noManagePermission() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long currentOwnerId = 1L;
            Long newOwnerId = 2L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(999L).build();
            User otherRoomOwner = User.builder().id(888L).build();
            Room room = Room.builder().id(roomId).owner(otherRoomOwner).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.transferOwner(roomId, spaceId, currentOwnerId, newOwnerId));
            assertEquals(SpaceErrorCode.NO_MANAGE_PERMISSION, ex.getErrorCode());
        }

        @Test
        @DisplayName("새 오너가 스페이스 멤버 아님 실패")
        void newOwnerNotMember() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long currentOwnerId = 1L;
            Long newOwnerId = 2L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(currentOwnerId).build();
            User roomOwner = User.builder().id(currentOwnerId).build();
            Room room = Room.builder().id(roomId).owner(roomOwner).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, newOwnerId)).thenReturn(false);

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.transferOwner(roomId, spaceId, currentOwnerId, newOwnerId));
            assertEquals(SpaceErrorCode.NOT_SPACE_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("자기 자신에게 위임 실패")
        void alreadyOwner() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long currentOwnerId = 1L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(currentOwnerId).build();
            User roomOwner = User.builder().id(currentOwnerId).build();
            Room room = Room.builder().id(roomId).owner(roomOwner).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, currentOwnerId)).thenReturn(true);

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.transferOwner(roomId, spaceId, currentOwnerId, currentOwnerId));
            assertEquals(SpaceErrorCode.ALREADY_OWNER, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 위임 성공")
        void success() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long currentOwnerId = 1L;
            Long newOwnerId = 2L;

            Space space = Space.builder().id(spaceId).roomId(roomId).ownerId(currentOwnerId).build();
            User roomOwner = User.builder().id(currentOwnerId).build();
            Room room = Room.builder().id(roomId).owner(roomOwner).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, newOwnerId)).thenReturn(true);

            spaceService.transferOwner(roomId, spaceId, currentOwnerId, newOwnerId);

            assertEquals(newOwnerId, space.getOwnerId());
        }
    }

    @Nested
    @DisplayName("getSpaceMemberList")
    class GetSpaceMemberList {

        @Test
        @DisplayName("룸 멤버 아님 실패")
        void notRoomMember() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId)).thenReturn(false);

            SpaceException ex = assertThrows(SpaceException.class,
                    () -> spaceService.getSpaceMemberList(roomId, spaceId, userId));
            assertEquals(SpaceErrorCode.NOT_ROOM_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 조회 성공")
        void success() {
            Long roomId = 10L;
            Long spaceId = 1L;
            Long userId = 1L;

            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);
            User user = User.builder().id(userId).build();
            Room room = Room.builder().id(roomId).build();
            when(memberProfileCard.getId()).thenReturn(1L);
            when(memberProfileCard.getUser()).thenReturn(user);
            when(memberProfileCard.getRoom()).thenReturn(room);
            when(memberProfileCard.getAnswers()).thenReturn(List.of());

            SpaceMember spaceMember = SpaceMember.builder().id(1L).memberProfileCard(memberProfileCard).build();

            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId)).thenReturn(true);
            when(spaceMemberRepository.findBySpaceId(spaceId)).thenReturn(List.of(spaceMember));

            var result = spaceService.getSpaceMemberList(roomId, spaceId, userId);

            assertEquals(1, result.size());
        }
    }
}
