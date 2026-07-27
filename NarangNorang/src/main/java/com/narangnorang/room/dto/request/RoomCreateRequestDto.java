package com.narangnorang.room.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomCreateRequestDto {

    private String name;
    private String description;
    private Integer maxMember;
    private List<RoomProfileCustomFieldCreateRequestDto> customFields;
}
