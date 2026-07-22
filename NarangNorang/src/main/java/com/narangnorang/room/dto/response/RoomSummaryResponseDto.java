package com.narangnorang.room.dto.response;

import com.narangnorang.room.entity.Room;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomSummaryResponseDto {

    private Long id;
    private String name;
    private String description;
    private Integer maxMember;
    private long currentMember;
    private String ownerName;

    public static RoomSummaryResponseDto from(Room room, long currentMember) {
        return RoomSummaryResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .maxMember(room.getMaxMember())
                .currentMember(currentMember)
                .ownerName(room.getOwner().getName())
                .build();
    }
}
