package com.narangnorang.room.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.narangnorang.auth.config.MyUserDetails;
import com.narangnorang.common.ApiResponse;
import com.narangnorang.room.dto.request.RoomCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomUpdateRequestDto;
import com.narangnorang.room.dto.response.RoomJoinResponseDto;
import com.narangnorang.room.dto.response.RoomProfileCustomFieldResponseDto;
import com.narangnorang.room.dto.response.RoomResponseDto;
import com.narangnorang.room.service.RoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ApiResponse<RoomResponseDto> createRoom(
            @RequestBody RoomCreateRequestDto requestDto,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        RoomResponseDto result = roomService.createRoom(
                requestDto,
                userDetails.getId()
        );

        return success(result);
    }

    @GetMapping("/{roomId}")
    public ApiResponse<RoomResponseDto> getRoom(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        RoomResponseDto result = roomService.getRoom(
                roomId,
                userDetails.getId()
        );

        return success(result);
    }

    @GetMapping("/join/{roomCode}")
    public ApiResponse<RoomJoinResponseDto> getRoomForJoin(
            @PathVariable("roomCode") String roomCode,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        RoomJoinResponseDto result = roomService.getRoomForJoin(
                roomCode,
                userDetails.getId()
        );

        return success(result);
    }

    @PatchMapping("/{roomId}")
    public ApiResponse<RoomResponseDto> updateRoom(
            @PathVariable("roomId") Long roomId,
            @RequestBody RoomUpdateRequestDto requestDto,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        RoomResponseDto result = roomService.updateRoom(
                roomId,
                requestDto,
                userDetails.getId()
        );

        return success(result);
    }

    @PostMapping("/{roomId}/customFields")
    public ApiResponse<RoomProfileCustomFieldResponseDto> createCustomField(
            @PathVariable("roomId") Long roomId,
            @RequestBody RoomProfileCustomFieldCreateRequestDto requestDto,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        RoomProfileCustomFieldResponseDto result = roomService.createCustomField(
                roomId,
                requestDto,
                userDetails.getId()
        );

        return success(result);
    }

    @PatchMapping("/{roomId}/customFields/{fieldId}")
    public ApiResponse<RoomProfileCustomFieldResponseDto> updateCustomField(
            @PathVariable("roomId") Long roomId,
            @PathVariable("fieldId") Long fieldId,
            @RequestBody RoomProfileCustomFieldUpdateRequestDto requestDto,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        RoomProfileCustomFieldResponseDto result = roomService.updateCustomField(
                roomId,
                fieldId,
                requestDto,
                userDetails.getId()
        );

        return success(result);
    }

    @DeleteMapping("/{roomId}/customFields/{fieldId}")
    public ApiResponse<Void> deleteCustomField(
            @PathVariable("roomId") Long roomId,
            @PathVariable("fieldId") Long fieldId,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        roomService.deleteCustomField(
                roomId,
                fieldId,
                userDetails.getId()
        );

        return success(null);
    }

    @DeleteMapping("/{roomId}")
    public ApiResponse<Void> deleteRoom(
            @PathVariable("roomId") Long roomId,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        roomService.deleteRoom(roomId, userDetails.getId());
        return success(null);
    }

    private <T> ApiResponse<T> success(T result) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(result);
        return response;
    }
}
