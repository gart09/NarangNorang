package com.narangnorang.space.service;

import java.util.List;

import com.narangnorang.space.dto.request.SpaceCreateRequestDto;
import com.narangnorang.space.dto.request.SpaceUpdateRequestDto;
import com.narangnorang.space.dto.response.SpaceProfileCardResponseDto;
import com.narangnorang.space.dto.response.SpaceSummaryResponseDto;

public interface SpaceService {
	
	// Space 간편정보 리스트업
	List<SpaceSummaryResponseDto> getSpaceList(Long roomId, List<String> tags);
	
	//스페이스Detail 정보 (프로필카드) 조회
	SpaceProfileCardResponseDto getSpaceDetail(Long spaceId);
	
	//스페이스 생성
	SpaceProfileCardResponseDto createSpace(Long roomId, Long ownerId, SpaceCreateRequestDto spaceCreateRequestDto);
	
	SpaceProfileCardResponseDto updateSpaceCard(Long spaceId, Long userId, SpaceUpdateRequestDto spaceUpdateRequestDto);
	
	void deleteSpace(Long spaceId, Long userId);
}
