package com.narangnorang.room.service;

import com.narangnorang.room.dto.request.RoomCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomUpdateRequestDto;
import com.narangnorang.room.dto.response.RoomProfileCustomFieldResponseDto;
import com.narangnorang.room.dto.response.RoomJoinResponseDto;
import com.narangnorang.room.dto.response.RoomResponseDto;

public interface RoomService {

    RoomResponseDto createRoom(RoomCreateRequestDto requestDto, Long userId);

    RoomResponseDto getRoom(Long roomId, Long userId);

    RoomJoinResponseDto getRoomForJoin(String roomCode, Long userId);

    RoomResponseDto updateRoom(Long roomId, RoomUpdateRequestDto requestDto, Long userId);

    RoomProfileCustomFieldResponseDto updateCustomField(
            Long roomId,
            Long fieldId,
            RoomProfileCustomFieldUpdateRequestDto requestDto,
            Long userId
    );

    void deleteRoom(Long roomId, Long userId);
}
