package com.narangnorang.space.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.narangnorang.space.dto.request.SpaceCreateRequestDto;
import com.narangnorang.space.dto.request.SpaceUpdateRequestDto;
import com.narangnorang.space.dto.response.SpaceProfileCardResponseDto;
import com.narangnorang.space.dto.response.SpaceSummaryResponseDto;
import com.narangnorang.space.entity.Space;
import com.narangnorang.space.entity.SpaceProfileCard;
import com.narangnorang.space.entity.Tag;
import com.narangnorang.space.repository.SpaceProfileCardRepository;
import com.narangnorang.space.repository.SpaceRepository;
import com.narangnorang.space.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SpaceServiceImpl implements SpaceService{
	
	private final SpaceRepository spaceRepository;
	private final SpaceProfileCardRepository spaceProfileCardRepository;
	private final TagRepository tagRepository;
	
	// 스페이스 목록 조회 (태그 필터링 옵션)
	@Override
	public List<SpaceSummaryResponseDto> getSpaceList(Long roomId, List<String> tags) {
		List<Space> spaces = (tags == null || tags.isEmpty())
				? spaces = spaceRepository.findByRoomId(roomId)
				: spaceRepository.findByRoomIdAndTagNames(roomId, tags);
		return spaces.stream()
				.map(SpaceSummaryResponseDto::from)
				.toList();
	}

	// 스페이스 상세 조회 (프로필 카드, 태그 포함)
	@Override
	public SpaceProfileCardResponseDto getSpaceDetail(Long spaceId) {
		
		Space space = spaceRepository.findByIdWithProfileCard(spaceId)
									.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스페이스입니다."));
		SpaceProfileCard spaceProfileCard = space.getProfileCard();
		List<String> tags = space.getTags()
								 .stream()
								 .map(Tag::getName)
								 .toList();
		return SpaceProfileCardResponseDto.from(space, spaceProfileCard, tags);
	}
	
	
	// 스페이스 생성
	@Override
	@Transactional
	public SpaceProfileCardResponseDto createSpace(Long roomId, Long ownerId, SpaceCreateRequestDto spaceCreateRequestDto) {
		
		Space space = spaceCreateRequestDto.toSpaceEntity(roomId, ownerId);
		spaceRepository.save(space);
		
		SpaceProfileCard card = spaceCreateRequestDto.toProfileCardEntity(space);
        spaceProfileCardRepository.save(card);
        
		List<Tag> tags = spaceCreateRequestDto.getTags().stream()
		        .map(tagName -> Tag.builder()
					                .space(space)
					                .name(tagName)
					                .build())
					        		.toList();	
        tagRepository.saveAll(tags);
        
        
        log.info("스페이스 생성 완료 - space : {}", space);

        return SpaceProfileCardResponseDto.from(space, card, spaceCreateRequestDto.getTags());
	}

	
	// 스페이스 삭제( 스페이스 + 프로필 카드 + 태그 + 스페이스 멤버 함께 삭제)
	@Override
	@Transactional
	public void deleteSpace(Long spaceId, Long userId) {
		
		Space space = spaceRepository.findById(spaceId)
				 .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스페이스입니다."));
		
		//스페이스 오너 검증 -- 룸 오너 검증 추가 필요
	    if (!space.getOwnerId().equals(userId)) {
	    	throw new IllegalArgumentException("스페이스 오너 또는 룸 오너가 아닙니다.");
	    }
	    
    	// cascade로 profileCard, tags, spaceMembers 전부 같이 삭제
    	spaceRepository.delete(space);
    	log.info("스페이스 삭제 완료 - spaceId={}, requestedBy={}", spaceId, userId);
	    
	}

	// 스페이스 수정 (기본 정보 + 프로필 카드 + 태그)
	@Override
	@Transactional
	public SpaceProfileCardResponseDto updateSpaceCard(Long spaceId, Long userId, SpaceUpdateRequestDto spaceUpdateRequestDto) {
		
		Space space = spaceRepository.findByIdWithProfileCard(spaceId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스페이스입니다."));
		
		//스페이스 오너 검증 -- 룸 오너 검증 추가 필요
	    if (!space.getOwnerId().equals(userId)) {
	    	throw new IllegalArgumentException("스페이스 오너 또는 룸 오너가 아닙니다.");
	    }
	    
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
	        tagNames = tagRepository.findBySpaceId(spaceId).stream()
	                .map(Tag::getName)
	                .toList();
	    }

	    log.info("스페이스 수정 완료 - spaceId={}, requestedBy={}", spaceId, userId);

	    return SpaceProfileCardResponseDto.from(space, card, tagNames);
	}

	
	// 룸 내 태그 목록 조회
	@Override
	public List<String> getRoomTagNames(Long roomId) {
		return tagRepository.findTagNamesByRoomId(roomId);
	}
	
	
	
	
}
