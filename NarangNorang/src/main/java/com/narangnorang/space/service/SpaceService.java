package com.narangnorang.space.service;

import java.util.List;

import com.narangnorang.space.dto.request.SpaceCreateRequestDto;
import com.narangnorang.space.dto.request.SpaceUpdateRequestDto;
import com.narangnorang.space.dto.response.SpaceProfileCardResponseDto;
import com.narangnorang.space.dto.response.SpaceSummaryResponseDto;

public interface SpaceService {
	
	// Space 간편정보 리스트업
	List<SpaceSummaryResponseDto> getSpaceList(Long roomId, Long userId, List<String> tags);
	
	//스페이스Detail 정보 (프로필카드) 조회
	SpaceProfileCardResponseDto getSpaceDetail(Long roomId, Long spaceId, Long userId);
	
	//스페이스 생성
	SpaceProfileCardResponseDto createSpace(Long roomId, Long ownerId, SpaceCreateRequestDto spaceCreateRequestDto);
	
	// 스페이스 업데이트
	SpaceProfileCardResponseDto updateSpaceCard(Long roomId, Long spaceId, Long userId, SpaceUpdateRequestDto spaceUpdateRequestDto);
	
	// 스페이스 제거 
	void deleteSpace(Long roomId, Long spaceId, Long userId);
	
	// 룸 내의 태그 목록 조회
	List<String> getRoomTagNames(Long roomId);
}
