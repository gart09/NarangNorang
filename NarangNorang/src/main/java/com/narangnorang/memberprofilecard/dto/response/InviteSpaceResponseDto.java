package com.narangnorang.memberprofilecard.dto.response;

import java.time.LocalDateTime;

import com.narangnorang.memberprofilecard.entity.InviteSpace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteSpaceResponseDto {
    private Long id;
    private Long requesterId;
    private Long targetId;
    private String spaceName;
    private String memberName;
    private InviteSpace.InviteType type;
    private InviteSpace.InviteStatus status;
    private LocalDateTime createdAt;

    public static InviteSpaceResponseDto from(InviteSpace inviteSpace) {
        return InviteSpaceResponseDto.builder()
                .id(inviteSpace.getId())
                .requesterId(inviteSpace.getRequestId())
                .targetId(inviteSpace.getTargetId())
                .spaceName(inviteSpace.getSpace().getName())
                .memberName(inviteSpace.getMemberProfileCard().getName())
                .type(inviteSpace.getType())
                .status(inviteSpace.getStatus())
                .createdAt(inviteSpace.getCreatedAt())
                .build();
    }
}