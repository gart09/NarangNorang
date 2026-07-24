package com.narangnorang.room.dto.request;

import java.util.List;

import com.narangnorang.room.entity.OptionType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomProfileCustomFieldBulkUpdateRequestDto {

    private Long id;
    private String fieldName;
    private boolean required;
    private OptionType optionType;
    private List<RoomProfileCustomFieldOptionUpdateRequestDto> options;
}
