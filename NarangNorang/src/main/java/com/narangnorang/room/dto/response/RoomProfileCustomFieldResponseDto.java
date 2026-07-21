package com.narangnorang.room.dto.response;

import com.narangnorang.room.entity.FieldType;
import com.narangnorang.room.entity.RoomProfileCustomField;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RoomProfileCustomFieldResponseDto {

    private Long id;
    private String fieldName;
    private boolean required;
    private FieldType fieldType;
    private List<RoomProfileCustomFieldOptionResponseDto> options;
    private LocalDateTime updatedAt;

    public static RoomProfileCustomFieldResponseDto from(RoomProfileCustomField customField) {
        return RoomProfileCustomFieldResponseDto.builder()
                .id(customField.getId())
                .fieldName(customField.getFieldName())
                .required(customField.isRequired())
                .fieldType(customField.getFieldType())
                .options(customField.getOptions().stream()
                        .map(RoomProfileCustomFieldOptionResponseDto::from)
                        .toList())
                .updatedAt(customField.getUpdatedAt())
                .build();
    }
}
