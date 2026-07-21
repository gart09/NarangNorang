package com.narangnorang.room.dto.request;

import com.narangnorang.room.entity.FieldType;
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
public class RoomProfileCustomFieldCreateRequestDto {

    private String fieldName;
    private boolean required;
    private FieldType fieldType;
    private List<RoomProfileCustomFieldOptionCreateRequestDto> options;
}
