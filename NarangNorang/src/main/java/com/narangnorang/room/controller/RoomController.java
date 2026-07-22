package com.narangnorang.room.controller;

import com.narangnorang.auth.ApiResponse;
import com.narangnorang.room.dto.request.RoomCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomUpdateRequestDto;
import com.narangnorang.room.dto.response.RoomProfileCustomFieldResponseDto;
import com.narangnorang.room.dto.response.RoomResponseDto;
import com.narangnorang.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ApiResponse<RoomResponseDto> createRoom(
            @RequestBody RoomCreateRequestDto requestDto,
            Authentication authentication
    ) {
        RoomResponseDto result = roomService.createRoom(
                requestDto,
                authentication.getName()
        );

        return success(result);
    }

    @GetMapping("/{roomId}")
    public ApiResponse<RoomResponseDto> getRoom(
            @PathVariable Long roomId,
            Authentication authentication
    ) {
        RoomResponseDto result = roomService.getRoom(
                roomId,
                authentication.getName()
        );

        return success(result);
    }

    @PatchMapping("/{roomId}")
    public ApiResponse<RoomResponseDto> updateRoom(
            @PathVariable Long roomId,
            @RequestBody RoomUpdateRequestDto requestDto,
            Authentication authentication
    ) {
        RoomResponseDto result = roomService.updateRoom(
                roomId,
                requestDto,
                authentication.getName()
        );

        return success(result);
    }

    @PatchMapping("/{roomId}/custom-fields/{fieldId}")
    public ApiResponse<RoomProfileCustomFieldResponseDto> updateCustomField(
            @PathVariable Long roomId,
            @PathVariable Long fieldId,
            @RequestBody RoomProfileCustomFieldUpdateRequestDto requestDto,
            Authentication authentication
    ) {
        RoomProfileCustomFieldResponseDto result = roomService.updateCustomField(
                roomId,
                fieldId,
                requestDto,
                authentication.getName()
        );

        return success(result);
    }

    @DeleteMapping("/{roomId}")
    public ApiResponse<Void> deleteRoom(
            @PathVariable Long roomId,
            Authentication authentication
    ) {
        roomService.deleteRoom(roomId, authentication.getName());
        return success(null);
    }

    private <T> ApiResponse<T> success(T result) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(result);
        return response;
    }
}
