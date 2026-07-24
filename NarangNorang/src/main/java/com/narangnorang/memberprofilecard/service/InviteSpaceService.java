package com.narangnorang.memberprofilecard.service;

import java.util.List;

import com.narangnorang.memberprofilecard.dto.response.InviteSpaceResponseDto;

public interface InviteSpaceService {

    void applyToSpace(Long spaceId, Long userId);

    void inviteToSpace(Long spaceId, Long ownerId, Long targetMemberId);

    void acceptInvite(Long inviteId, Long requesterId);

    void rejectInvite(Long inviteId, Long requesterId);

    List<InviteSpaceResponseDto> getPendingInvites(Long spaceId, Long userId);
    
    List<InviteSpaceResponseDto> getInvites(Long userId);
}