package com.narangnorang.memberprofilecard.service;

import com.narangnorang.memberprofilecard.dto.response.InviteSpaceResponseDto;
import com.narangnorang.memberprofilecard.entity.InviteSpace;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.repository.InviteSpaceRepository;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.repository.RoomRepository;
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
    private final RoomRepository roomRepository;

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
    public void inviteToSpace(Long spaceId, Long ownerId, Long memberUserId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스페이스입니다."));

        if (!space.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("스페이스 오너만 가능한 작업입니다.");
        }

        MemberProfileCard memberProfileCard = validateInvitable(space, memberUserId);

        InviteSpace inviteSpace = InviteSpace.spaceToMember(space, ownerId, memberUserId, memberProfileCard);
        inviteSpaceRepository.save(inviteSpace);

        log.info("스페이스 권유 완료 - spaceId={}, memberId={}", spaceId, memberUserId);
    }

    // 수락
    @Override
    @Transactional
    public void acceptInvite(Long inviteId, Long requesterId) {
    	
        InviteSpace inviteSpace = inviteSpaceRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청/초대입니다."));

        inviteSpace.accept(requesterId);

        Space space = inviteSpace.getSpace();
    	
        // 룸 탈퇴 검증
        if (!memberProfileCardRepository.existsByUserIdAndRoomId(inviteSpace.getMemberId(), space.getRoomId())) {
            throw new IllegalStateException("더 이상 룸 멤버가 아닙니다.");
        }
        //스페이스 멤버 중복 검증
        if (spaceMemberRepository.existsBySpaceIdAndUserId(space.getId(), inviteSpace.getMemberId())) {
            throw new IllegalStateException("이미 스페이스 멤버입니다.");
        }
        
        space.updateCurrentMember(space.getCurrentMemberCount() + 1);

        spaceMemberRepository.save(SpaceMember.toSpaceMemberEntity(space, inviteSpace.getMemberId(), inviteSpace.getMemberProfileCard()));

        log.info("스페이스 초대/신청 수락 완료 - inviteId={}, memberId={}", inviteId, inviteSpace.getMemberId());
    }

    // 거절
    @Override
    @Transactional
    public void rejectInvite(Long inviteId, Long requesterId) {
    	
        InviteSpace inviteSpace = inviteSpaceRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청/초대입니다."));
        Space space = inviteSpace.getSpace();

        inviteSpace.reject(requesterId);

        log.info("스페이스 초대/신청 거절 완료 - inviteId={}", inviteId);
    }

    // 스페이스 기준 대기 목록 조회 (오너가 확인)
    @Override
    public List<InviteSpaceResponseDto> getPendingInvites(Long spaceId, Long userId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스페이스입니다."));
        
        // 스페이스 오너 검증
        if (!space.getOwnerId().equals(userId)) {
            throw new IllegalStateException("스페이스 오너만 조회할 수 있습니다.");
        }
        
        // 룸 멤버 검증
        if (!memberProfileCardRepository.existsByUserIdAndRoomId(userId, space.getRoomId())) {
            throw new IllegalStateException("룸 멤버가 아닙니다.");
        }
        
        return inviteSpaceRepository.findBySpaceIdAndStatus(spaceId, InviteSpace.InviteStatus.PENDING).stream()
                .map(InviteSpaceResponseDto::from)
                .toList();
    }

    // 본인이 받은 신청/초대 대기 목록 조회
    @Override
    public List<InviteSpaceResponseDto> getInvites(Long userId) {
    	
        return inviteSpaceRepository.findByMemberIdAndStatus(userId, InviteSpace.InviteStatus.PENDING).stream()
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
        if (inviteSpaceRepository.existsBySpaceIdAndMemberIdAndStatus(space.getId(), userId, InviteSpace.InviteStatus.PENDING)) {
            throw new IllegalStateException("이미 처리 대기 중인 신청/초대가 있습니다.");
        }
        return memberProfileCard;
    }
    

    
    
}