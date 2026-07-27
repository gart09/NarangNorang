package com.narangnorang.invitespace.dto.response;

import java.time.LocalDateTime;

import com.narangnorang.invitespace.entity.InviteSpace;
import com.narangnorang.invitespace.entity.InviteStatus;
import com.narangnorang.invitespace.entity.InviteType;

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
    private Long memberId;
    private Long ownerId;
    private String spaceName;
    private String memberName;
    private InviteType type;
    private InviteStatus status;
    private LocalDateTime createdAt;

    public static InviteSpaceResponseDto from(InviteSpace inviteSpace) {
        return InviteSpaceResponseDto.builder()
                .id(inviteSpace.getId())
                .memberId(inviteSpace.getMemberId())
                .ownerId(inviteSpace.getSpace().getOwnerId())
                .spaceName(inviteSpace.getSpace().getName())
                .memberName(inviteSpace.getMemberProfileCard().getName())
                .type(inviteSpace.getType())
                .status(inviteSpace.getStatus())
                .createdAt(inviteSpace.getCreatedAt())
                .build();
    }
}