package com.narangnorang.memberprofilecard.service;

import com.narangnorang.memberprofilecard.dto.response.InviteSpaceResponseDto;
import com.narangnorang.memberprofilecard.entity.InviteSpace;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.repository.InviteSpaceRepository;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.space.entity.Space;
import com.narangnorang.space.entity.SpaceMember;
import com.narangnorang.space.repository.SpaceMemberRepository;
import com.narangnorang.space.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteSpaceServiceImpl implements InviteSpaceService {

    private final InviteSpaceRepository inviteSpaceRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final MemberProfileCardRepository memberProfileCardRepository;

    // 멤버가 스페이스에 신청
    @Override
    @Transactional
    public void applyToSpace(Long spaceId, Long userId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스페이스입니다."));

        MemberProfileCard memberProfileCard = validateInvitable(space, userId);

        InviteSpace inviteSpace = InviteSpace.memberToSpace(space, userId, memberProfileCard);
        inviteSpaceRepository.save(inviteSpace);

        log.info("스페이스 신청 완료 - spaceId={}, userId={}", spaceId, userId);
    }

    // 오너가 멤버에게 권유
    @Override
    @Transactional
    public void inviteToSpace(Long spaceId, Long ownerId, Long targetUserId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스페이스입니다."));

        if (!space.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("스페이스 오너만 가능한 작업입니다.");
        }

        MemberProfileCard memberProfileCard = validateInvitable(space, targetUserId);

        InviteSpace inviteSpace = InviteSpace.spaceToMember(space, ownerId, targetUserId, memberProfileCard);
        inviteSpaceRepository.save(inviteSpace);

        log.info("스페이스 권유 완료 - spaceId={}, targetUserId={}", spaceId, targetUserId);
    }

    // 수락
    @Override
    @Transactional
    public void acceptInvite(Long inviteId, Long requesterId) {
        InviteSpace inviteSpace = inviteSpaceRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청/초대입니다."));

        inviteSpace.accept(requesterId);

        Space space = inviteSpace.getSpace();
        space.updateCurrentMember(space.getCurrentMemberCount() + 1);

        spaceMemberRepository.save(SpaceMember.toSpaceMemberEntity(space, inviteSpace.getTargetId(), inviteSpace.getMemberProfileCard()));

        log.info("스페이스 초대/신청 수락 완료 - inviteId={}, targetId={}", inviteId, inviteSpace.getTargetId());
    }

    // 거절
    @Override
    @Transactional
    public void rejectInvite(Long inviteId, Long requesterId) {
        InviteSpace inviteSpace = inviteSpaceRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청/초대입니다."));

        inviteSpace.reject(requesterId);

        log.info("스페이스 초대/신청 거절 완료 - inviteId={}", inviteId);
    }

    // 스페이스 기준 대기 목록 조회 (오너가 확인)
    @Override
    public List<InviteSpaceResponseDto> getPendingInvites(Long spaceId) {
        return inviteSpaceRepository.findBySpaceIdAndStatus(spaceId, InviteSpace.InviteStatus.PENDING).stream()
                .map(InviteSpaceResponseDto::from)
                .toList();
    }

    // 본인이 받은 신청/초대 대기 목록 조회
    @Override
    public List<InviteSpaceResponseDto> getInvites(Long userId) {
        return inviteSpaceRepository.findByTargetIdAndStatus(userId, InviteSpace.InviteStatus.PENDING).stream()
                .map(InviteSpaceResponseDto::from)
                .toList();
    }

    // 신청/권유 생성 전 공통 검증
    private MemberProfileCard validateInvitable(Space space, Long userId) {
    	
    	// 룸 멤버 검증
        MemberProfileCard memberProfileCard = memberProfileCardRepository
                .findByUserIdAndRoomId(userId, space.getRoomId())
                .orElseThrow(() -> new IllegalStateException("룸 멤버만 가능한 작업입니다."));

        // 스페이스 멤버 중복 검증
        if (spaceMemberRepository.existsBySpaceIdAndUserId(space.getId(), userId)) {
            throw new IllegalStateException("이미 스페이스 멤버입니다.");
        }

        // 신청 중복 검증
        if (inviteSpaceRepository.existsBySpaceIdAndTargetIdAndStatus(space.getId(), userId, InviteSpace.InviteStatus.PENDING)) {
            throw new IllegalStateException("이미 처리 대기 중인 신청/초대가 있습니다.");
        }

        return memberProfileCard;
    }
}