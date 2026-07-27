package com.narangnorang.room.dto.response;

import com.narangnorang.room.entity.Room;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RoomResponseDto {

    private Long id;
    private String name;
    private String description;
    private Integer maxMember;
    private long currentMember;
    private String roomCode;
    private LocalDateTime createdAt;
    private Long ownerId;
    private String ownerName;
    private List<RoomProfileCustomFieldResponseDto> customFields;

    public static RoomResponseDto from(Room room, long currentMember) {
        List<RoomProfileCustomFieldResponseDto> customFields = room.getCustomFields()
                .stream()
                .map(RoomProfileCustomFieldResponseDto::from)
                .toList();

        return RoomResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .maxMember(room.getMaxMember())
                .currentMember(currentMember)
                .roomCode(room.getRoomCode())
                .createdAt(room.getCreatedAt())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getName())
                .customFields(customFields)
                .build();
    }
}
