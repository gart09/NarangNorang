package com.narangnorang.memberprofilecard.dto.request;

import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.entity.MemberProfileCustomAnswer;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.entity.RoomProfileCustomField;
import com.narangnorang.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileCardReadRequestDto {
		private String name;
		private Long memberProfileCardId;
		private Long roomId;
		//필드이름: 필터링하고자 하는 필드 항목들)
		private Map<Long, List<String>> customFields;
}
