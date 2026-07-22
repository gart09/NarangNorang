package com.narangnorang.room.service;

import com.narangnorang.room.dto.request.RoomCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomUpdateRequestDto;
import com.narangnorang.room.dto.response.RoomProfileCustomFieldResponseDto;
import com.narangnorang.room.dto.response.RoomResponseDto;

public interface RoomService {

    RoomResponseDto createRoom(RoomCreateRequestDto requestDto, String email);

    RoomResponseDto getRoom(Long roomId, String email);

    RoomResponseDto updateRoom(Long roomId, RoomUpdateRequestDto requestDto, String email);

    RoomProfileCustomFieldResponseDto updateCustomField(
            Long roomId,
            Long fieldId,
            RoomProfileCustomFieldUpdateRequestDto requestDto,
            String email
    );

    void deleteRoom(Long roomId, String email);
}
