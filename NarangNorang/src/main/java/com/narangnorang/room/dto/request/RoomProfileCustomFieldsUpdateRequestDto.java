package com.narangnorang.room.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RoomProfileCustomFieldsUpdateRequestDto {

    private List<RoomProfileCustomFieldBulkUpdateRequestDto> customFields;
}
