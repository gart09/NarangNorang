package com.narangnorang.space.dto.request;

import com.narangnorang.space.entity.Space;
import com.narangnorang.space.entity.SpaceProfileCard;
import com.narangnorang.space.entity.Tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceCreateRequestDto {

    private String name;

    private Long maxMemberCount;

    private List<String> tags;

    private List<String> techStack;

    private LocalDateTime preferredStartTime;

    private LocalDateTime preferredEndTime;

    private Map<String, List<String>> customField;

    public Space toSpaceEntity(Long roomId, Long ownerId) {
        return Space.builder()
                .roomId(roomId)
                .ownerId(ownerId)
                .name(name)
                .maxMemberCount(maxMemberCount)
                .currentMemberCount(1L)
                .build();
    }

    public SpaceProfileCard toProfileCardEntity(Space space) {
        return SpaceProfileCard.builder()
                .space(space)
                .name(name)
                .techStack(techStack)
                .preferredStartTime(preferredStartTime)
                .preferredEndTime(preferredEndTime)
                .customField(customField)
                .build();
    }
    
}