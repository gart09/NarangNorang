package com.narangnorang.room.dto.response;

import com.narangnorang.room.entity.Room;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomJoinResponseDto {

    private boolean member;
    private RoomResponseDto room;

    public static RoomJoinResponseDto from(
            Room room,
            long currentMember,
            boolean member
    ) {
        return RoomJoinResponseDto.builder()
                .member(member)
                .room(RoomResponseDto.from(room, currentMember))
                .build();
    }
}
