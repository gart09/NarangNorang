package com.narangnorang.memberprofilecard.dto.response;

import com.narangnorang.room.dto.response.RoomResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomsListResponseDto {
	List<RoomResponseDto> rooms;
}
