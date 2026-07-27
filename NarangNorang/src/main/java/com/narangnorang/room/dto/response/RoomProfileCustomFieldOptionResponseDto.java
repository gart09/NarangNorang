package com.narangnorang.room.dto.response;

import com.narangnorang.room.entity.RoomProfileCustomFieldOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomProfileCustomFieldOptionResponseDto {

    private Long id;
    private String optionValue;
    private Integer displayOrder;

    public static RoomProfileCustomFieldOptionResponseDto from(RoomProfileCustomFieldOption option) {
        return RoomProfileCustomFieldOptionResponseDto.builder()
                .id(option.getId())
                .optionValue(option.getOptionValue())
                .displayOrder(option.getDisplayOrder())
                .build();
    }
}
