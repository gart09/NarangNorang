package com.narangnorang.space.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardReadResponseDto;
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
import com.narangnorang.space.entity.Tag;
import com.narangnorang.space.exception.SpaceException;
import com.narangnorang.space.exception.errorcode.SpaceErrorCode;
import com.narangnorang.space.repository.SpaceMemberRepository;
import com.narangnorang.space.repository.SpaceRepository;
import com.narangnorang.space.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SpaceServiceImpl implements SpaceService {

    private final SpaceRepository spaceRepository;
    private final TagRepository tagRepository;
    private final RoomRepository roomRepository;
    private final MemberProfileCardRepository memberProfileCardRepository;
    private final SpaceMemberRepository spaceMemberRepository;

    // 스페이스 목록 조회 (태그 필터링 옵션)
    @Override
    public List<SpaceSummaryResponseDto> getSpaceList(Long roomId, Long userId, List<String> tags) {

        validateRoomMember(roomId, userId);

        List<Space> spaces = (tags == null || tags.isEmpty())
                ? spaceRepository.findByRoomIdWithTags(roomId)
                : spaceRepository.findByRoomIdAndTagNamesWithTags(roomId, tags);

        return spaces.stream()
                .map(SpaceSummaryResponseDto::from)
                .toList();
    }

    // 스페이스 상세 조회 (프로필 카드, 태그, 오너 이름 포함)
    @Override
    public SpaceProfileCardResponseDto getSpaceDetail(Long roomId, Long spaceId, Long userId) {

        validateRoomMember(roomId, userId);

        Space space = getSpaceWithProfileCardOrThrow(spaceId);
        validateSpaceInRoom(space, roomId);

        String ownerName = resolveOwnerName(spaceId, space.getOwnerId());
        List<String> tags = space.getTags().stream().map(Tag::getName).toList();

        return SpaceProfileCardResponseDto.from(space, space.getProfileCard(), tags, ownerName);
    }

    // 스페이스 생성
    @Override
    @Transactional
    public SpaceProfileCardResponseDto createSpace(Long roomId, Long ownerId, SpaceCreateRequestDto spaceCreateRequestDto) {

        MemberProfileCard memberProfileCard = memberProfileCardRepository
                .findByUserIdAndRoomId(ownerId, roomId)
                .orElseThrow(() -> new SpaceException(SpaceErrorCode.NOT_ROOM_MEMBER));

        Space space = spaceCreateRequestDto.toSpaceEntity(roomId, ownerId);

        SpaceProfileCard card = spaceCreateRequestDto.toProfileCardEntity(space);
        space.assignProfileCard(card);

        spaceCreateRequestDto.getTags().forEach(space::addTag);

        space.addMember(SpaceMember.toSpaceMemberEntity(space, ownerId, memberProfileCard));
        space.updateCurrentMember(1L);

        spaceRepository.save(space);

        log.info("스페이스 생성 완료 - space : {}", space);

        return SpaceProfileCardResponseDto.from(space, card, spaceCreateRequestDto.getTags(), memberProfileCard.getName());
    }

    // 스페이스 삭제 (스페이스 + 프로필 카드 + 태그 + 스페이스 멤버 함께 삭제)
    @Override
    @Transactional
    public void deleteSpace(Long roomId, Long spaceId, Long userId) {

        Space space = getSpaceOrThrow(spaceId);

        validateManagePermission(space, roomId, userId);

        // cascade로 profileCard, tags, spaceMembers 전부 같이 삭제
        spaceRepository.delete(space);
        log.info("스페이스 삭제 완료 - spaceId={}, requestedBy={}", spaceId, userId);
    }

    // 스페이스 수정 (기본 정보 + 프로필 카드 + 태그)
    @Override
    @Transactional
    public SpaceProfileCardResponseDto updateSpaceCard(Long roomId, Long spaceId, Long userId, SpaceUpdateRequestDto spaceUpdateRequestDto) {

        Space space = getSpaceWithProfileCardOrThrow(spaceId);

        validateManagePermission(space, roomId, userId);

        space.updateInfo(spaceUpdateRequestDto.getName(), spaceUpdateRequestDto.getMaxMemberCount());

        SpaceProfileCard card = space.getProfileCard();
        card.updateProfileCard(spaceUpdateRequestDto.getTechStack(), spaceUpdateRequestDto.getPreferredStartTime(), spaceUpdateRequestDto.getPreferredEndTime());
        card.updateCustomField(spaceUpdateRequestDto.getCustomField());

        // 태그 수정이 없으면 null로 받고 태그는 재등록하지 않음.
        List<String> tagNames;
        if (spaceUpdateRequestDto.getTags() != null) {
        	
            tagRepository.deleteBySpaceId(spaceId);
            List<Tag> newTags = spaceUpdateRequestDto.getTags()
                    .stream()
                    .map(tagName -> Tag.of(space, tagName))
                    .toList();
            tagRepository.saveAll(newTags);
            tagNames = spaceUpdateRequestDto.getTags();
            log.info("스페이스 태그 업데이트 완료 - spaceId={}, tags={}", spaceId, tagNames);
            
        } else {
            tagNames = space.getTags().stream().map(Tag::getName).toList();
        }

        String ownerName = resolveOwnerName(spaceId, space.getOwnerId());

        log.info("스페이스 수정 완료 - spaceId={}, requestedBy={}", spaceId, userId);

        return SpaceProfileCardResponseDto.from(space, card, tagNames, ownerName);
    }

    // 룸 내 태그 목록 조회
    @Override
    public List<String> getRoomTagNames(Long roomId) {
        return tagRepository.findTagNamesByRoomId(roomId);
    }

    // 스페이스 탈퇴
    @Override
    @Transactional
    public void leaveSpace(Long roomId, Long spaceId, Long userId) {

        Space space = getSpaceOrThrow(spaceId);

        validateSpaceInRoom(space, roomId);

        SpaceMember spaceMember = spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)
                .orElseThrow(() -> new SpaceException(SpaceErrorCode.NOT_SPACE_MEMBER));

        // 오너는 탈퇴 불가
        if (space.getOwnerId().equals(userId)) {
            throw new SpaceException(SpaceErrorCode.OWNER_CANNOT_LEAVE);
        }

        spaceMemberRepository.delete(spaceMember);
        space.updateCurrentMember(space.getCurrentMemberCount() - 1);

        log.info("스페이스 탈퇴 완료 - spaceId={}, userId={}", spaceId, userId);
    }

    // 스페이스 오너 위임
    @Override
    @Transactional
    public void transferOwner(Long roomId, Long spaceId, Long currentOwnerId, Long newOwnerId) {

        Space space = getSpaceOrThrow(spaceId);

        validateManagePermission(space, roomId, currentOwnerId);

        // 새 오너가 이 스페이스의 멤버인지 확인
        if (!spaceMemberRepository.existsBySpaceIdAndUserId(spaceId, newOwnerId)) {
            throw new SpaceException(SpaceErrorCode.NOT_SPACE_MEMBER);
        }

        // 자기 자신에게 위임하는 경우 방지
        if (currentOwnerId.equals(newOwnerId)) {
            throw new SpaceException(SpaceErrorCode.ALREADY_OWNER);
        }

        space.transferOwner(newOwnerId);

        log.info("스페이스 오너 위임 완료 - spaceId={}, from={}, to={}", spaceId, currentOwnerId, newOwnerId);
    }

    // 스페이스 멤버 목록
    @Override
    public List<MemberProfileCardReadResponseDto> getSpaceMemberList(Long roomId, Long spaceId, Long userId) {

        validateRoomMember(roomId, userId);

        List<SpaceMember> spaceMembers = spaceMemberRepository.findBySpaceId(spaceId);
        return spaceMembers.stream()
                .map(SpaceMember::getMemberProfileCard)
                .map(MemberProfileCardReadResponseDto::from)
                .toList();
    }

    // 스페이스 존재 확인 + 조회
    private Space getSpaceOrThrow(Long spaceId) {
        return spaceRepository.findById(spaceId)
                .orElseThrow(() -> new SpaceException(SpaceErrorCode.SPACE_NOT_FOUND));
    }

    // 스페이스 존재 확인 + 조회 (프로필카드, 태그까지 fetch join)
    private Space getSpaceWithProfileCardOrThrow(Long spaceId) {
        return spaceRepository.findByIdWithProfileCard(spaceId)
                .orElseThrow(() -> new SpaceException(SpaceErrorCode.SPACE_NOT_FOUND));
    }

    // 스페이스 오너의 멤버 프로필 카드에서 이름 조회
    private String resolveOwnerName(Long spaceId, Long ownerId) {
        return spaceMemberRepository.findBySpaceIdAndUserId(spaceId, ownerId)
                .map(sm -> sm.getMemberProfileCard().getName())
                .orElse(null);
    }

    // 룸 멤버 확인
    private void validateRoomMember(Long roomId, Long userId) {
        boolean member = memberProfileCardRepository.existsByUserIdAndRoomId(userId, roomId);
        if (!member) {
            throw new SpaceException(SpaceErrorCode.NOT_ROOM_MEMBER);
        }
    }

    // 스페이스가 해당 룸 소속이 맞는지 검증
    private void validateSpaceInRoom(Space space, Long roomId) {
        if (!space.getRoomId().equals(roomId)) {
            throw new SpaceException(SpaceErrorCode.SPACE_ROOM_MISMATCH);
        }
    }

    // 스페이스 및 룸 검증
    private void validateManagePermission(Space space, Long roomId, Long userId) {

        validateSpaceInRoom(space, roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new SpaceException(SpaceErrorCode.ROOM_NOT_FOUND));

        boolean spaceOwner = space.getOwnerId().equals(userId);
        boolean roomOwner = room.getOwner().getId().equals(userId);

        if (!spaceOwner && !roomOwner) {
            throw new SpaceException(SpaceErrorCode.NO_MANAGE_PERMISSION);
        }
    }
}