package com.narangnorang.space.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Getter;

@Getter
public class SpaceUpdateRequestDto {

    // Space
    private String name;
    private Long maxMemberCount;

    // SpaceProfileCard
    private List<String> techStack;
    private LocalDateTime preferredStartTime;
    private LocalDateTime preferredEndTime;
    private Map<String, List<String>> customField;

    // Tag
    private List<String> tags;
}