package com.narangnorang.room.service;

import java.util.List;

import com.narangnorang.room.dto.request.RoomCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldsUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomUpdateRequestDto;
import com.narangnorang.room.dto.response.RoomJoinResponseDto;
import com.narangnorang.room.dto.response.RoomProfileCustomFieldResponseDto;
import com.narangnorang.room.dto.response.RoomResponseDto;

public interface RoomService {

    RoomResponseDto createRoom(RoomCreateRequestDto requestDto, Long userId);

    RoomResponseDto getRoom(Long roomId, Long userId);

    RoomJoinResponseDto getRoomForJoin(String roomCode, Long userId);

    RoomResponseDto updateRoom(Long roomId, RoomUpdateRequestDto requestDto, Long userId);

    List<RoomProfileCustomFieldResponseDto> updateCustomFields(
            Long roomId,
            RoomProfileCustomFieldsUpdateRequestDto requestDto,
            Long userId
    );

    void deleteRoom(Long roomId, Long userId);
}
