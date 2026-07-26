package com.narangnorang.invitespace.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.narangnorang.space.entity.SpaceMember;
import com.narangnorang.space.repository.SpaceMemberRepository;
import com.narangnorang.space.repository.SpaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InviteSpaceServiceImpl implements InviteSpaceService {

    private final InviteSpaceRepository inviteSpaceRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final MemberProfileCardRepository memberProfileCardRepository;

    // 멤버가 스페이스에 신청
    @Override
    @Transactional
    public void applyToSpace(Long spaceId, Long userId) {

        Space space = getSpace(spaceId);

        MemberProfileCard memberProfileCard = validateInvitable(space, userId);

        InviteSpace inviteSpace = InviteSpace.memberToSpace(space, userId, memberProfileCard);
        inviteSpaceRepository.save(inviteSpace);

        log.info("스페이스 신청 완료 - spaceId={}, userId={}", spaceId, userId);
    }

    // 오너가 멤버에게 권유
    @Override
    @Transactional
    public void inviteToSpace(Long spaceId, Long ownerId, Long memberUserId) {

        Space space = getSpace(spaceId);
        validateSpaceOwner(space, ownerId);

        MemberProfileCard memberProfileCard = validateInvitable(space, memberUserId);

        InviteSpace inviteSpace = InviteSpace.spaceToMember(space, memberUserId, memberProfileCard);
        inviteSpaceRepository.save(inviteSpace);

        log.info("스페이스 권유 완료 - spaceId={}, memberId={}", spaceId, memberUserId);
    }

    // 수락
    @Override
    @Transactional
    public void acceptInvite(Long inviteId, Long requesterId) {

        InviteSpace inviteSpace = getInvite(inviteId);

        validateApprover(inviteSpace, requesterId);
        validatePending(inviteSpace);

        Space space = inviteSpace.getSpace();
        Long memberId = inviteSpace.getMemberId();

        if (!memberProfileCardRepository.existsByUserIdAndRoomId(memberId, space.getRoomId())) {
            throw new InviteSpaceException(InviteErrorCode.LEFT_ROOM);
        }
        validateSpaceMember(space.getId(), memberId);

        inviteSpace.accept();

        space.updateCurrentMember(space.getCurrentMemberCount() + 1);
        spaceMemberRepository.save(SpaceMember.toSpaceMemberEntity(space, memberId, inviteSpace.getMemberProfileCard()));

        log.info("스페이스 초대/신청 수락 완료 - inviteId={}, memberId={}", inviteId, memberId);
    }

    // 거절
    @Override
    @Transactional
    public void rejectInvite(Long inviteId, Long requesterId) {

        InviteSpace inviteSpace = getInvite(inviteId);

        validateApprover(inviteSpace, requesterId);
        validatePending(inviteSpace);

        inviteSpace.reject();

        log.info("스페이스 초대/신청 거절 완료 - inviteId={}", inviteId);
    }

    // 스페이스 기준 대기 목록 조회 (오너가 확인)
    @Override
    public List<InviteSpaceResponseDto> getPendingInvites(Long spaceId, Long userId) {

        Space space = getSpace(spaceId);
        validateSpaceOwner(space, userId);

        if (!memberProfileCardRepository.existsByUserIdAndRoomId(userId, space.getRoomId())) {
            throw new InviteSpaceException(InviteErrorCode.NOT_ROOM_MEMBER);
        }

        return inviteSpaceRepository.findBySpaceIdAndStatus(spaceId, InviteStatus.PENDING).stream()
                .map(InviteSpaceResponseDto::from)
                .toList();
    }

    // 본인이 받은 신청/초대 대기 목록 조회
    // (내가 멤버로서 받은 권유 + 내가 오너로서 받은 신청을 각각 필터링해서 합침)
    @Override
    public List<InviteSpaceResponseDto> getInvites(Long userId) {

        List<InviteSpace> receivedAsMember = inviteSpaceRepository
                .findByMemberIdAndStatus(userId, InviteStatus.PENDING).stream()
                .filter(i -> i.getType() == InviteType.SpaceToMember)
                .toList();

        List<InviteSpace> receivedAsOwner = inviteSpaceRepository
                .findByOwnerUserIdAndStatus(userId, InviteStatus.PENDING).stream()
                .filter(i -> i.getType() == InviteType.MemberToSpace)
                .toList();

        List<InviteSpace> allReceived = new ArrayList<>();
        allReceived.addAll(receivedAsMember);
        allReceived.addAll(receivedAsOwner);

        return allReceived.stream()
                .map(InviteSpaceResponseDto::from)
                .toList();
    }

    // 스페이스 존재 확인 + 조회
    private Space getSpace(Long spaceId) {
        return spaceRepository.findById(spaceId)
                .orElseThrow(() -> new InviteSpaceException(InviteErrorCode.SPACE_NOT_FOUND));
    }

    // 신청/초대 건 존재 확인 + 조회 (space, memberProfileCard까지 fetch join된 상태로 반환)
    private InviteSpace getInvite(Long inviteId) {
        return inviteSpaceRepository.findByIdWithDetails(inviteId)
                .orElseThrow(() -> new InviteSpaceException(InviteErrorCode.INVITE_NOT_FOUND));
    }

    // 요청자가 해당 스페이스의 오너인지 검증
    private void validateSpaceOwner(Space space, Long userId) {
        if (!space.getOwnerId().equals(userId)) {
            throw new InviteSpaceException(InviteErrorCode.NOT_SPACE_OWNER);
        }
    }

    // 이미 해당 스페이스의 멤버인지 검증
    private void validateSpaceMember(Long spaceId, Long userId) {
        if (spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, userId)) {
            throw new InviteSpaceException(InviteErrorCode.ALREADY_SPACE_MEMBER);
        }
    }

    // 수락/거절 요청자가 실제 승인 권한자인지 검증
    private void validateApprover(InviteSpace inviteSpace, Long requesterId) {
        if (!inviteSpace.getApproverId().equals(requesterId)) {
            throw new InviteSpaceException(InviteErrorCode.NOT_APPROVER);
        }
    }

    // 아직 처리되지 않은(PENDING) 요청인지 검증
    private void validatePending(InviteSpace inviteSpace) {
        if (inviteSpace.getStatus() != InviteStatus.PENDING) {
            throw new InviteSpaceException(InviteErrorCode.ALREADY_PROCESSED);
        }
    }

    // 신청/권유 생성 전 공통 검증 (룸 멤버 여부 + 중복 가입 + 중복 신청 확인) 후 멤버 프로필 카드 반환
    private MemberProfileCard validateInvitable(Space space, Long userId) {

        MemberProfileCard memberProfileCard = memberProfileCardRepository
                .findByUserIdAndRoomId(userId, space.getRoomId())
                .orElseThrow(() -> new InviteSpaceException(InviteErrorCode.NOT_ROOM_MEMBER));

        validateSpaceMember(space.getId(), userId);

        if (inviteSpaceRepository.existsBySpaceIdAndMemberIdAndStatus(space.getId(), userId, InviteStatus.PENDING)) {
            throw new InviteSpaceException(InviteErrorCode.ALREADY_PENDING_INVITE);
        }

        return memberProfileCard;
    }
}