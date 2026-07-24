package com.narangnorang.memberprofilecard.service;

import java.util.List;

import com.narangnorang.memberprofilecard.dto.response.InviteSpaceResponseDto;

public interface InviteSpaceService {

    void applyToSpace(Long roomId, Long spaceId, Long userId);

    void inviteToSpace(Long roomId, Long spaceId, Long ownerId, Long targetMemberId);

    void acceptInvite(Long roomId, Long inviteId, Long requesterId);

    void rejectInvite(Long roomId, Long inviteId, Long requesterId);

    List<InviteSpaceResponseDto> getPendingInvites(Long roomId, Long spaceId, Long userId);
    
    List<InviteSpaceResponseDto> getInvites(Long userId);
}