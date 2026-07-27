package com.narangnorang.invitespace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.narangnorang.invitespace.dto.response.InviteSpaceResponseDto;
import com.narangnorang.invitespace.entity.InviteSpace;
import com.narangnorang.invitespace.entity.InviteStatus;
import com.narangnorang.invitespace.entity.InviteType;
import com.narangnorang.invitespace.exception.InviteSpaceException;
import com.narangnorang.invitespace.exception.errorcode.InviteErrorCode;
import com.narangnorang.invitespace.repository.InviteSpaceRepository;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.space.entity.Space;
import com.narangnorang.space.repository.SpaceMemberRepository;
import com.narangnorang.space.repository.SpaceRepository;

@ExtendWith(MockitoExtension.class)
class InviteSpaceServiceTest {

    @Mock
    private InviteSpaceRepository inviteSpaceRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private SpaceMemberRepository spaceMemberRepository;

    @Mock
    private MemberProfileCardRepository memberProfileCardRepository;

    @InjectMocks
    private InviteSpaceServiceImpl inviteSpaceService;

    @Nested
    @DisplayName("applyToSpace")
    class ApplyToSpace {

        @Test
        @DisplayName("존재하지 않는 스페이스 실패")
        void spaceNotFound() {
            Long spaceId = 1L;
            Long userId = 2L;

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.empty());

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.applyToSpace(spaceId, userId));
            assertEquals(InviteErrorCode.SPACE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("룸 멤버 아님 실패")
        void notRoomMember() {
            Long spaceId = 1L;
            Long userId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(1L).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.findByUserIdAndRoomId(userId, 10L)).thenReturn(Optional.empty());

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.applyToSpace(spaceId, userId));
            assertEquals(InviteErrorCode.NOT_ROOM_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("이미 스페이스 멤버 실패")
        void alreadySpaceMember() {
            Long spaceId = 1L;
            Long userId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(1L).build();
            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.findByUserIdAndRoomId(userId, 10L)).thenReturn(Optional.of(memberProfileCard));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId)).thenReturn(true);

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.applyToSpace(spaceId, userId));
            assertEquals(InviteErrorCode.ALREADY_SPACE_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("이미 대기 중인 신청 존재 실패")
        void alreadyPendingInvite() {
            Long spaceId = 1L;
            Long userId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(1L).build();
            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.findByUserIdAndRoomId(userId, 10L)).thenReturn(Optional.of(memberProfileCard));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId)).thenReturn(false);
            when(inviteSpaceRepository.existsBySpaceIdAndMemberIdAndStatus(spaceId, userId, InviteStatus.PENDING)).thenReturn(true);

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.applyToSpace(spaceId, userId));
            assertEquals(InviteErrorCode.ALREADY_PENDING_INVITE, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 신청 성공")
        void success() {
            Long spaceId = 1L;
            Long userId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(1L).build();
            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.findByUserIdAndRoomId(userId, 10L)).thenReturn(Optional.of(memberProfileCard));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId)).thenReturn(false);
            when(inviteSpaceRepository.existsBySpaceIdAndMemberIdAndStatus(spaceId, userId, InviteStatus.PENDING)).thenReturn(false);

            inviteSpaceService.applyToSpace(spaceId, userId);

            verify(inviteSpaceRepository, times(1)).save(any(InviteSpace.class));
        }
    }

    @Nested
    @DisplayName("inviteToSpace")
    class InviteToSpace {

        @Test
        @DisplayName("존재하지 않는 스페이스 실패")
        void spaceNotFound() {
            Long spaceId = 1L;
            Long ownerId = 1L;
            Long targetUserId = 2L;

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.empty());

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.inviteToSpace(spaceId, ownerId, targetUserId));
            assertEquals(InviteErrorCode.SPACE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("스페이스 오너 아님 실패")
        void notSpaceOwner() {
            Long spaceId = 1L;
            Long ownerId = 1L;
            Long targetUserId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(999L).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.inviteToSpace(spaceId, ownerId, targetUserId));
            assertEquals(InviteErrorCode.NOT_SPACE_OWNER, ex.getErrorCode());
        }

        @Test
        @DisplayName("대상이 룸 멤버 아님 실패")
        void targetNotRoomMember() {
            Long spaceId = 1L;
            Long ownerId = 1L;
            Long targetUserId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(ownerId).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.findByUserIdAndRoomId(targetUserId, 10L)).thenReturn(Optional.empty());

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.inviteToSpace(spaceId, ownerId, targetUserId));
            assertEquals(InviteErrorCode.NOT_ROOM_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("대상이 이미 스페이스 멤버 실패")
        void alreadySpaceMember() {
            Long spaceId = 1L;
            Long ownerId = 1L;
            Long targetUserId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(ownerId).build();
            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.findByUserIdAndRoomId(targetUserId, 10L)).thenReturn(Optional.of(memberProfileCard));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, targetUserId)).thenReturn(true);

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.inviteToSpace(spaceId, ownerId, targetUserId));
            assertEquals(InviteErrorCode.ALREADY_SPACE_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("대상에게 이미 대기 중인 권유 존재 실패")
        void alreadyPendingInvite() {
            Long spaceId = 1L;
            Long ownerId = 1L;
            Long targetUserId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(ownerId).build();
            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.findByUserIdAndRoomId(targetUserId, 10L)).thenReturn(Optional.of(memberProfileCard));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, targetUserId)).thenReturn(false);
            when(inviteSpaceRepository.existsBySpaceIdAndMemberIdAndStatus(spaceId, targetUserId, InviteStatus.PENDING)).thenReturn(true);

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.inviteToSpace(spaceId, ownerId, targetUserId));
            assertEquals(InviteErrorCode.ALREADY_PENDING_INVITE, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 권유 성공")
        void success() {
            Long spaceId = 1L;
            Long ownerId = 1L;
            Long targetUserId = 2L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(ownerId).build();
            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.findByUserIdAndRoomId(targetUserId, 10L)).thenReturn(Optional.of(memberProfileCard));
            when(spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, targetUserId)).thenReturn(false);
            when(inviteSpaceRepository.existsBySpaceIdAndMemberIdAndStatus(spaceId, targetUserId, InviteStatus.PENDING)).thenReturn(false);

            inviteSpaceService.inviteToSpace(spaceId, ownerId, targetUserId);

            verify(inviteSpaceRepository, times(1)).save(any(InviteSpace.class));
        }
    }

    @Nested
    @DisplayName("acceptInvite")
    class AcceptInvite {

        @Test
        @DisplayName("존재하지 않는 신청/권유 실패")
        void inviteNotFound() {
            Long inviteId = 1L;
            Long requesterId = 2L;

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.empty());

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.acceptInvite(inviteId, requesterId));
            assertEquals(InviteErrorCode.INVITE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("승인 권한자 아님 실패")
        void notApprover() {
            Long inviteId = 1L;
            Long requesterId = 2L;
            InviteSpace inviteSpace = mock(InviteSpace.class);
            when(inviteSpace.getApproverId()).thenReturn(999L);

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.of(inviteSpace));

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.acceptInvite(inviteId, requesterId));
            assertEquals(InviteErrorCode.NOT_APPROVER, ex.getErrorCode());
        }

        @Test
        @DisplayName("이미 처리된 요청 실패")
        void alreadyProcessed() {
            Long inviteId = 1L;
            Long requesterId = 2L;
            InviteSpace inviteSpace = mock(InviteSpace.class);
            when(inviteSpace.getApproverId()).thenReturn(requesterId);
            when(inviteSpace.getStatus()).thenReturn(InviteStatus.ACCEPTED);

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.of(inviteSpace));

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.acceptInvite(inviteId, requesterId));
            assertEquals(InviteErrorCode.ALREADY_PROCESSED, ex.getErrorCode());
        }

        @Test
        @DisplayName("룸을 탈퇴한 상태 실패")
        void leftRoom() {
            Long inviteId = 1L;
            Long requesterId = 2L;
            Long memberId = 3L;
            Space space = Space.builder().id(1L).roomId(10L).ownerId(requesterId).build();

            InviteSpace inviteSpace = mock(InviteSpace.class);
            when(inviteSpace.getApproverId()).thenReturn(requesterId);
            when(inviteSpace.getStatus()).thenReturn(InviteStatus.PENDING);
            when(inviteSpace.getSpace()).thenReturn(space);
            when(inviteSpace.getMemberId()).thenReturn(memberId);

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.of(inviteSpace));
            when(memberProfileCardRepository.existsByUserIdAndRoomId(memberId, 10L)).thenReturn(false);

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.acceptInvite(inviteId, requesterId));
            assertEquals(InviteErrorCode.LEFT_ROOM, ex.getErrorCode());
        }

        @Test
        @DisplayName("이미 스페이스 멤버 실패")
        void alreadySpaceMember() {
            Long inviteId = 1L;
            Long requesterId = 2L;
            Long memberId = 3L;
            Space space = Space.builder().id(1L).roomId(10L).ownerId(requesterId).build();

            InviteSpace inviteSpace = mock(InviteSpace.class);
            when(inviteSpace.getApproverId()).thenReturn(requesterId);
            when(inviteSpace.getStatus()).thenReturn(InviteStatus.PENDING);
            when(inviteSpace.getSpace()).thenReturn(space);
            when(inviteSpace.getMemberId()).thenReturn(memberId);

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.of(inviteSpace));
            when(memberProfileCardRepository.existsByUserIdAndRoomId(memberId, 10L)).thenReturn(true);
            when(spaceMemberRepository.existsBySpaceIdAndUserId(space.getId(), memberId)).thenReturn(true);

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.acceptInvite(inviteId, requesterId));
            assertEquals(InviteErrorCode.ALREADY_SPACE_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 수락 성공")
        void success() {
            Long inviteId = 1L;
            Long requesterId = 2L;
            Long memberId = 3L;
            Space space = Space.builder()
                    .id(1L).roomId(10L).ownerId(requesterId)
                    .currentMemberCount(1L).maxMemberCount(5L)
                    .build();
            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);

            InviteSpace inviteSpace = mock(InviteSpace.class);
            when(inviteSpace.getApproverId()).thenReturn(requesterId);
            when(inviteSpace.getStatus()).thenReturn(InviteStatus.PENDING);
            when(inviteSpace.getSpace()).thenReturn(space);
            when(inviteSpace.getMemberId()).thenReturn(memberId);
            when(inviteSpace.getMemberProfileCard()).thenReturn(memberProfileCard);

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.of(inviteSpace));
            when(memberProfileCardRepository.existsByUserIdAndRoomId(memberId, 10L)).thenReturn(true);
            when(spaceMemberRepository.existsBySpaceIdAndUserId(space.getId(), memberId)).thenReturn(false);

            inviteSpaceService.acceptInvite(inviteId, requesterId);

            verify(inviteSpace, times(1)).accept();
            verify(spaceMemberRepository, times(1)).save(any());
            assertEquals(2L, space.getCurrentMemberCount());
        }
    }

    @Nested
    @DisplayName("rejectInvite")
    class RejectInvite {

        @Test
        @DisplayName("존재하지 않는 신청/권유 실패")
        void inviteNotFound() {
            Long inviteId = 1L;
            Long requesterId = 2L;

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.empty());

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.rejectInvite(inviteId, requesterId));
            assertEquals(InviteErrorCode.INVITE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("승인 권한자 아님 실패")
        void notApprover() {
            Long inviteId = 1L;
            Long requesterId = 2L;
            InviteSpace inviteSpace = mock(InviteSpace.class);
            when(inviteSpace.getApproverId()).thenReturn(999L);

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.of(inviteSpace));

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.rejectInvite(inviteId, requesterId));
            assertEquals(InviteErrorCode.NOT_APPROVER, ex.getErrorCode());
        }

        @Test
        @DisplayName("이미 처리된 요청 실패")
        void alreadyProcessed() {
            Long inviteId = 1L;
            Long requesterId = 2L;
            InviteSpace inviteSpace = mock(InviteSpace.class);
            when(inviteSpace.getApproverId()).thenReturn(requesterId);
            when(inviteSpace.getStatus()).thenReturn(InviteStatus.REJECTED);

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.of(inviteSpace));

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.rejectInvite(inviteId, requesterId));
            assertEquals(InviteErrorCode.ALREADY_PROCESSED, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 거절 성공")
        void success() {
            Long inviteId = 1L;
            Long requesterId = 2L;
            InviteSpace inviteSpace = mock(InviteSpace.class);
            when(inviteSpace.getApproverId()).thenReturn(requesterId);
            when(inviteSpace.getStatus()).thenReturn(InviteStatus.PENDING);

            when(inviteSpaceRepository.findByIdWithDetails(inviteId)).thenReturn(Optional.of(inviteSpace));

            inviteSpaceService.rejectInvite(inviteId, requesterId);

            verify(inviteSpace, times(1)).reject();
        }
    }

    @Nested
    @DisplayName("getPendingInvites")
    class GetPendingInvites {

        @Test
        @DisplayName("존재하지 않는 스페이스 실패")
        void spaceNotFound() {
            Long spaceId = 1L;
            Long userId = 1L;

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.empty());

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.getPendingInvites(spaceId, userId));
            assertEquals(InviteErrorCode.SPACE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("스페이스 오너 아님 실패")
        void notSpaceOwner() {
            Long spaceId = 1L;
            Long userId = 1L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(999L).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.getPendingInvites(spaceId, userId));
            assertEquals(InviteErrorCode.NOT_SPACE_OWNER, ex.getErrorCode());
        }

        @Test
        @DisplayName("룸 멤버 아님 실패")
        void notRoomMember() {
            Long spaceId = 1L;
            Long userId = 1L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(userId).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, 10L)).thenReturn(false);

            InviteSpaceException ex = assertThrows(InviteSpaceException.class,
                    () -> inviteSpaceService.getPendingInvites(spaceId, userId));
            assertEquals(InviteErrorCode.NOT_ROOM_MEMBER, ex.getErrorCode());
        }

        @Test
        @DisplayName("정상 조회 성공")
        void success() {
            Long spaceId = 1L;
            Long userId = 1L;
            Space space = Space.builder().id(spaceId).roomId(10L).ownerId(userId).build();

            when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
            when(memberProfileCardRepository.existsByUserIdAndRoomId(userId, 10L)).thenReturn(true);
            when(inviteSpaceRepository.findBySpaceIdAndStatus(spaceId, InviteStatus.PENDING)).thenReturn(List.of());

            List<InviteSpaceResponseDto> result = inviteSpaceService.getPendingInvites(spaceId, userId);

            assertEquals(0, result.size());
        }
    }

    @Nested
    @DisplayName("getInvites")
    class GetInvites {

        @Test
        @DisplayName("멤버로 받은 권유와 오너로 받은 신청을 각각 걸러서 합친다")
        void success() {
            Long userId = 1L;

            Space space = Space.builder().id(1L).name("space1").roomId(10L).ownerId(1L).build();
            MemberProfileCard memberProfileCard = mock(MemberProfileCard.class);
            when(memberProfileCard.getName()).thenReturn("현빈");

            InviteSpace receivedInvite = mock(InviteSpace.class);
            when(receivedInvite.getType()).thenReturn(InviteType.SpaceToMember);
            when(receivedInvite.getSpace()).thenReturn(space);
            when(receivedInvite.getMemberProfileCard()).thenReturn(memberProfileCard);

            InviteSpace sentApplyAsMember = mock(InviteSpace.class);
            when(sentApplyAsMember.getType()).thenReturn(InviteType.MemberToSpace);

            InviteSpace receivedApply = mock(InviteSpace.class);
            when(receivedApply.getType()).thenReturn(InviteType.MemberToSpace);
            when(receivedApply.getSpace()).thenReturn(space);
            when(receivedApply.getMemberProfileCard()).thenReturn(memberProfileCard);

            InviteSpace sentInviteAsOwner = mock(InviteSpace.class);
            when(sentInviteAsOwner.getType()).thenReturn(InviteType.SpaceToMember);

            when(inviteSpaceRepository.findByMemberIdAndStatus(userId, InviteStatus.PENDING))
                    .thenReturn(List.of(receivedInvite, sentApplyAsMember));
            when(inviteSpaceRepository.findByOwnerUserIdAndStatus(userId, InviteStatus.PENDING))
                    .thenReturn(List.of(receivedApply, sentInviteAsOwner));

            List<InviteSpaceResponseDto> result = inviteSpaceService.getInvites(userId);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("받은 신청/권유가 없으면 빈 목록을 반환한다")
        void success_Empty() {
            Long userId = 1L;

            when(inviteSpaceRepository.findByMemberIdAndStatus(userId, InviteStatus.PENDING)).thenReturn(List.of());
            when(inviteSpaceRepository.findByOwnerUserIdAndStatus(userId, InviteStatus.PENDING)).thenReturn(List.of());

            List<InviteSpaceResponseDto> result = inviteSpaceService.getInvites(userId);

            assertEquals(0, result.size());
        }
    }
}
