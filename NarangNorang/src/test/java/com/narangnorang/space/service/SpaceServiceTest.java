//package com.narangnorang.space.service;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
//import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
//import com.narangnorang.room.entity.Room;
//import com.narangnorang.room.repository.RoomRepository;
//import com.narangnorang.space.dto.request.SpaceCreateRequestDto;
//import com.narangnorang.space.dto.request.SpaceUpdateRequestDto;
//import com.narangnorang.space.entity.Space;
//import com.narangnorang.space.entity.SpaceMember;
//import com.narangnorang.space.entity.SpaceProfileCard;
//import com.narangnorang.space.entity.Tag;
//import com.narangnorang.space.exception.SpaceException;
//import com.narangnorang.space.exception.errorcode.SpaceErrorCode;
//import com.narangnorang.space.repository.SpaceMemberRepository;
//import com.narangnorang.space.repository.SpaceRepository;
//import com.narangnorang.space.repository.TagRepository;
//import com.narangnorang.user.entity.User;
//
//@ExtendWith(MockitoExtension.class)
//class SpaceServiceTest {
//
//    @Mock private SpaceRepository spaceRepository;
//    @Mock private TagRepository tagRepository;
//    @Mock private RoomRepository roomRepository;
//    @Mock private MemberProfileCardRepository memberProfileCardRepository;
//    @Mock private SpaceMemberRepository spaceMemberRepository;
//
//    @InjectMocks
//    private SpaceService spaceService;
//
//    private Space space;
//
//    @BeforeEach
//    void setUp() {
//        space = Space.builder()
//                .id(1L)
//                .roomId(10L)
//                .ownerId(100L)
//                .name("백엔드 스터디")
//                .currentMemberCount(1L)
//                .maxMemberCount(5L)
//                .build();
//    }
//
//    @Nested
//    @DisplayName("스페이스 목록 조회")
//    class GetSpaceList {
//
//        @Test
//        @DisplayName("룸 멤버가 아니면 예외 발생")
//        void notRoomMember_throwsException() {
//            // given
//            when(memberProfileCardRepository.existsByUserIdAndRoomId(200L, 10L)).thenReturn(false);
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.getSpaceList(10L, 200L, null))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.NOT_ROOM_MEMBER);
//        }
//
//        @Test
//        @DisplayName("태그 필터 없이 정상 조회된다")
//        void noTags_success() {
//            // given
//            when(memberProfileCardRepository.existsByUserIdAndRoomId(200L, 10L)).thenReturn(true);
//            when(spaceRepository.findByRoomIdWithTags(10L)).thenReturn(List.of(space));
//
//            // when
//            var result = spaceService.getSpaceList(10L, 200L, null);
//
//            // then
//            assertThat(result).hasSize(1);
//            verify(spaceRepository).findByRoomIdWithTags(10L);
//            verify(spaceRepository, never()).findByRoomIdAndTagNamesWithTags(any(), any());
//        }
//
//        @Test
//        @DisplayName("태그 필터를 걸면 태그 조건 조회가 호출된다")
//        void withTags_success() {
//            // given
//            List<String> tags = List.of("java", "spring");
//            when(memberProfileCardRepository.existsByUserIdAndRoomId(200L, 10L)).thenReturn(true);
//            when(spaceRepository.findByRoomIdAndTagNamesWithTags(10L, tags)).thenReturn(List.of(space));
//
//            // when
//            var result = spaceService.getSpaceList(10L, 200L, tags);
//
//            // then
//            assertThat(result).hasSize(1);
//            verify(spaceRepository).findByRoomIdAndTagNamesWithTags(10L, tags);
//            verify(spaceRepository, never()).findByRoomIdWithTags(any());
//        }
//    }
//
//    @Nested
//    @DisplayName("스페이스 상세 조회")
//    class GetSpaceDetail {
//
//        @Test
//        @DisplayName("존재하지 않는 스페이스면 예외 발생")
//        void spaceNotFound_throwsException() {
//            // given
//            when(memberProfileCardRepository.existsByUserIdAndRoomId(200L, 10L)).thenReturn(true);
//            when(spaceRepository.findByIdWithProfileCard(1L)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.getSpaceDetail(10L, 1L, 200L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.SPACE_NOT_FOUND);
//        }
//
//        @Test
//        @DisplayName("스페이스가 해당 룸 소속이 아니면 예외 발생")
//        void roomMismatch_throwsException() {
//            // given
//            when(memberProfileCardRepository.existsByUserIdAndRoomId(200L, 999L)).thenReturn(true);
//            when(spaceRepository.findByIdWithProfileCard(1L)).thenReturn(Optional.of(space));
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.getSpaceDetail(999L, 1L, 200L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.SPACE_ROOM_MISMATCH);
//        }
//
//        @Test
//        @DisplayName("정상적으로 상세 정보가 조회된다")
//        void success() {
//            // given
//            var profileCard = mock(SpaceProfileCard.class);
//            var ownerMember = mock(SpaceMember.class);
//            var ownerProfileCard = mock(MemberProfileCard.class);
//
//            space.assignProfileCard(profileCard);
//
//            when(memberProfileCardRepository.existsByUserIdAndRoomId(100L, 10L)).thenReturn(true);
//            when(spaceRepository.findByIdWithProfileCard(1L)).thenReturn(Optional.of(space));
//            when(spaceMemberRepository.findBySpaceIdAndUserId(1L, 100L)).thenReturn(Optional.of(ownerMember));
//            when(ownerMember.getMemberProfileCard()).thenReturn(ownerProfileCard);
//            when(ownerProfileCard.getName()).thenReturn("현빈");
//
//            // when
//            var result = spaceService.getSpaceDetail(10L, 1L, 100L);
//
//            // then
//            assertThat(result).isNotNull();
//            assertThat(result.getOwner()).isEqualTo("현빈");
//        }
//    }
//
//    @Nested
//    @DisplayName("스페이스 생성")
//    class CreateSpace {
//
//        @Test
//        @DisplayName("룸 멤버가 아니면 예외 발생")
//        void notRoomMember_throwsException() {
//            // given
//            when(memberProfileCardRepository.findByUserIdAndRoomId(200L, 10L)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.createSpace(10L, 200L, mock(SpaceCreateRequestDto.class)))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.NOT_ROOM_MEMBER);
//        }
//
//        @Test
//        @DisplayName("정상적으로 스페이스가 생성된다")
//        void success() {
//            // given
//            var memberProfileCard = mock(MemberProfileCard.class);
//            var requestDto = mock(SpaceCreateRequestDto.class);
//            var newSpace = Space.builder().id(2L).roomId(10L).ownerId(100L).build();
//            var profileCard = mock(SpaceProfileCard.class);
//
//            when(memberProfileCardRepository.findByUserIdAndRoomId(100L, 10L))
//                    .thenReturn(Optional.of(memberProfileCard));
//            when(memberProfileCard.getName()).thenReturn("현빈");
//            when(requestDto.toSpaceEntity(10L, 100L)).thenReturn(newSpace);
//            when(requestDto.toProfileCardEntity(newSpace)).thenReturn(profileCard);
//            when(requestDto.getTags()).thenReturn(List.of("java"));
//
//            // when
//            var result = spaceService.createSpace(10L, 100L, requestDto);
//
//            // then
//            assertThat(result).isNotNull();
//            assertThat(result.getOwner()).isEqualTo("현빈");
//            verify(spaceRepository).save(newSpace);
//        }
//    }
//
//    @Nested
//    @DisplayName("스페이스 삭제")
//    class DeleteSpace {
//
//        @Test
//        @DisplayName("존재하지 않는 스페이스면 예외 발생")
//        void spaceNotFound_throwsException() {
//            // given
//            when(spaceRepository.findById(1L)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.deleteSpace(10L, 1L, 100L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.SPACE_NOT_FOUND);
//        }
//
//        @Test
//        @DisplayName("스페이스 오너도 룸 오너도 아니면 예외 발생")
//        void noManagePermission_throwsException() {
//            // given
//            var room = mock(Room.class);
//            var otherUser = mock(User.class);
//
//            when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));
//            when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
//            when(room.getOwner()).thenReturn(otherUser);
//            when(otherUser.getId()).thenReturn(999L);
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.deleteSpace(10L, 1L, 200L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.NO_MANAGE_PERMISSION);
//        }
//
//        @Test
//        @DisplayName("스페이스 오너면 정상 삭제된다")
//        void success() {
//            // given
//            when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));
//            when(roomRepository.findById(10L)).thenReturn(Optional.of(mock(Room.class)));
//
//            // when
//            spaceService.deleteSpace(10L, 1L, 100L);
//
//            // then
//            verify(spaceRepository).delete(space);
//        }
//    }
//
//    @Nested
//    @DisplayName("스페이스 수정")
//    class UpdateSpaceCard {
//
//        @Test
//        @DisplayName("존재하지 않는 스페이스면 예외 발생")
//        void spaceNotFound_throwsException() {
//            // given
//            when(spaceRepository.findByIdWithProfileCard(1L)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.updateSpaceCard(10L, 1L, 100L, mock(SpaceUpdateRequestDto.class)))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.SPACE_NOT_FOUND);
//        }
//
//        @Test
//        @DisplayName("태그 수정 요청이 있으면 태그를 재등록한다")
//        void withNewTags_success() {
//            // given
//            var profileCard = mock(SpaceProfileCard.class);
//            var requestDto = mock(SpaceUpdateRequestDto.class);
//            var ownerMember = mock(SpaceMember.class);
//            var ownerProfileCard = mock(MemberProfileCard.class);
//
//            space.assignProfileCard(profileCard);
//
//            when(spaceRepository.findByIdWithProfileCard(1L)).thenReturn(Optional.of(space));
//            when(roomRepository.findById(10L)).thenReturn(Optional.of(mock(Room.class)));
//            when(requestDto.getTags()).thenReturn(List.of("newTag"));
//            when(spaceMemberRepository.findBySpaceIdAndUserId(1L, 100L)).thenReturn(Optional.of(ownerMember));
//            when(ownerMember.getMemberProfileCard()).thenReturn(ownerProfileCard);
//            when(ownerProfileCard.getName()).thenReturn("현빈");
//
//            // when
//            var result = spaceService.updateSpaceCard(10L, 1L, 100L, requestDto);
//
//            // then
//            assertThat(result).isNotNull();
//            verify(tagRepository).deleteBySpaceId(1L);
//            verify(tagRepository).saveAll(any());
//        }
//
//        @Test
//        @DisplayName("태그 수정 요청이 없으면 태그를 재등록하지 않는다")
//        void noTagsChange_keepsExistingTags() {
//            // given
//            var profileCard = mock(SpaceProfileCard.class);
//            var requestDto = mock(SpaceUpdateRequestDto.class);
//            var ownerMember = mock(SpaceMember.class);
//            var ownerProfileCard = mock(MemberProfileCard.class);
//
//            space.assignProfileCard(profileCard);
//
//            when(spaceRepository.findByIdWithProfileCard(1L)).thenReturn(Optional.of(space));
//            when(roomRepository.findById(10L)).thenReturn(Optional.of(mock(Room.class)));
//            when(requestDto.getTags()).thenReturn(null);
//            when(spaceMemberRepository.findBySpaceIdAndUserId(1L, 100L)).thenReturn(Optional.of(ownerMember));
//            when(ownerMember.getMemberProfileCard()).thenReturn(ownerProfileCard);
//            when(ownerProfileCard.getName()).thenReturn("현빈");
//
//            // when
//            spaceService.updateSpaceCard(10L, 1L, 100L, requestDto);
//
//            // then
//            verify(tagRepository, never()).deleteBySpaceId(any());
//            verify(tagRepository, never()).saveAll(any());
//        }
//    }
//
//    @Nested
//    @DisplayName("룸 내 태그 목록 조회")
//    class GetRoomTagNames {
//
//        @Test
//        @DisplayName("정상적으로 태그 이름 목록이 조회된다")
//        void success() {
//            // given
//            when(tagRepository.findTagNamesByRoomId(10L)).thenReturn(List.of("java", "spring"));
//
//            // when
//            var result = spaceService.getRoomTagNames(10L);
//
//            // then
//            assertThat(result).containsExactly("java", "spring");
//        }
//    }
//
//    @Nested
//    @DisplayName("스페이스 탈퇴")
//    class LeaveSpace {
//
//        @Test
//        @DisplayName("존재하지 않는 스페이스면 예외 발생")
//        void spaceNotFound_throwsException() {
//            // given
//            when(spaceRepository.findById(1L)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.leaveSpace(10L, 1L, 200L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.SPACE_NOT_FOUND);
//        }
//
//        @Test
//        @DisplayName("스페이스 멤버가 아니면 예외 발생")
//        void notSpaceMember_throwsException() {
//            // given
//            when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));
//            when(spaceMemberRepository.findBySpaceIdAndUserId(1L, 200L)).thenReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.leaveSpace(10L, 1L, 200L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.NOT_SPACE_MEMBER);
//        }
//
//        @Test
//        @DisplayName("오너는 탈퇴할 수 없다")
//        void owner_cannotLeave() {
//            // given
//            when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));
//            when(spaceMemberRepository.findBySpaceIdAndUserId(1L, 100L)).thenReturn(Optional.of(mock(SpaceMember.class)));
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.leaveSpace(10L, 1L, 100L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.OWNER_CANNOT_LEAVE);
//        }
//
//        @Test
//        @DisplayName("정상적으로 탈퇴 처리된다")
//        void success() {
//            // given
//            var spaceMember = mock(SpaceMember.class);
//            when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));
//            when(spaceMemberRepository.findBySpaceIdAndUserId(1L, 200L)).thenReturn(Optional.of(spaceMember));
//
//            // when
//            spaceService.leaveSpace(10L, 1L, 200L);
//
//            // then
//            verify(spaceMemberRepository).delete(spaceMember);
//            assertThat(space.getCurrentMemberCount()).isEqualTo(0L);
//        }
//    }
//
//    @Nested
//    @DisplayName("스페이스 오너 위임")
//    class TransferOwner {
//
//        @Test
//        @DisplayName("새 오너가 스페이스 멤버가 아니면 예외 발생")
//        void newOwnerNotMember_throwsException() {
//            // given
//            when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));
//            when(roomRepository.findById(10L)).thenReturn(Optional.of(mock(Room.class)));
//            when(spaceMemberRepository.existsBySpaceIdAndUserId(1L, 300L)).thenReturn(false);
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.transferOwner(10L, 1L, 100L, 300L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.NOT_SPACE_MEMBER);
//        }
//
//        @Test
//        @DisplayName("자기 자신에게 위임하면 예외 발생")
//        void selfTransfer_throwsException() {
//            // given
//            when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));
//            when(roomRepository.findById(10L)).thenReturn(Optional.of(mock(Room.class)));
//            when(spaceMemberRepository.existsBySpaceIdAndUserId(1L, 100L)).thenReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.transferOwner(10L, 1L, 100L, 100L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.ALREADY_OWNER);
//        }
//
//        @Test
//        @DisplayName("정상적으로 오너가 위임된다")
//        void success() {
//            // given
//            when(spaceRepository.findById(1L)).thenReturn(Optional.of(space));
//            when(roomRepository.findById(10L)).thenReturn(Optional.of(mock(Room.class)));
//            when(spaceMemberRepository.existsBySpaceIdAndUserId(1L, 300L)).thenReturn(true);
//
//            // when
//            spaceService.transferOwner(10L, 1L, 100L, 300L);
//
//            // then
//            assertThat(space.getOwnerId()).isEqualTo(300L);
//        }
//    }
//
//    @Nested
//    @DisplayName("스페이스 멤버 목록 조회")
//    class GetSpaceMemberList {
//
//        @Test
//        @DisplayName("룸 멤버가 아니면 예외 발생")
//        void notRoomMember_throwsException() {
//            // given
//            when(memberProfileCardRepository.existsByUserIdAndRoomId(200L, 10L)).thenReturn(false);
//
//            // when & then
//            assertThatThrownBy(() -> spaceService.getSpaceMemberList(10L, 1L, 200L))
//                    .isInstanceOf(SpaceException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", SpaceErrorCode.NOT_ROOM_MEMBER);
//        }
//
//        @Test
//        @DisplayName("정상적으로 멤버 목록이 조회된다")
//        void success() {
//            // given
//            var spaceMember = mock(SpaceMember.class);
//            var memberProfileCard = mock(MemberProfileCard.class);
//
//            when(memberProfileCardRepository.existsByUserIdAndRoomId(100L, 10L)).thenReturn(true);
//            when(spaceMemberRepository.findBySpaceId(1L)).thenReturn(List.of(spaceMember));
//            when(spaceMember.getMemberProfileCard()).thenReturn(memberProfileCard);
//
//            // when
//            var result = spaceService.getSpaceMemberList(10L, 1L, 100L);
//
//            // then
//            assertThat(result).hasSize(1);
//        }
//    }
//}