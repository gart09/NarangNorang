package com.narangnorang.memberprofilecard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteSpaceRequestDto {
    private Long targetUserId;
    private Long spaceId;
}