package com.narangnorang.room.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomProfileCustomFieldOptionUpdateRequestDto {

    private Long id;

    private String optionValue;
    private Integer displayOrder;
}
