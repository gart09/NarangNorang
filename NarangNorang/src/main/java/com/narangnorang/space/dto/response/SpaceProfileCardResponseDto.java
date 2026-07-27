package com.narangnorang.space.dto.response;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.narangnorang.space.entity.Space;
import com.narangnorang.space.entity.SpaceProfileCard;
import com.narangnorang.space.entity.Tag;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceProfileCardResponseDto {
    private String name;
    private String owner;
    private Long ownerId;
    private List<String> techStack;
    private LocalTime preferredStartTime;
    private LocalTime preferredEndTime;
    private Map<String, List<String>> customField;
    private Long maxMemberCount;
    private Long currentMemberCount;
    private List<String> tags;
    
    public static SpaceProfileCardResponseDto from(Space space, SpaceProfileCard card, List<String> tags, String ownerName) {
        return SpaceProfileCardResponseDto.builder()
                .name(card.getName())
                .owner(ownerName)
                .ownerId(space.getOwnerId())
                .techStack(card.getTechStack())
                .preferredStartTime(card.getPreferredStartTime())
                .preferredEndTime(card.getPreferredEndTime())
                .customField(card.getCustomField())
                .maxMemberCount(space.getMaxMemberCount())
                .currentMemberCount(space.getCurrentMemberCount())
                .tags(tags)
                .build();
    }
}