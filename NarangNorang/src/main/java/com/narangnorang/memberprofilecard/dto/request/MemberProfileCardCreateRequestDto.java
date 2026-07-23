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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileCardCreateRequestDto {
		private String name;
		private String roomCode;
		private Map<Long, String> answers;

		public MemberProfileCard toEntity(User user, Room room, Map<Long, RoomProfileCustomField> fieldMap){
			MemberProfileCard memberProfileCard =  MemberProfileCard.builder()
					.name(this.getName())
					.user(user)
					.room(room)
					.createdAt(LocalDateTime.now())
					.updatedAt(LocalDateTime.now())
					.build();

			if(this.answers != null && this.answers.isEmpty() == false){
				this.answers.forEach((fieldId, value) -> {
					RoomProfileCustomField customField = fieldMap.get(fieldId);

					if (customField == null) {
						throw new IllegalArgumentException("존재하지 않는 필드 ID입니다: " + fieldId);
					}

					MemberProfileCustomAnswer answer = MemberProfileCustomAnswer.builder()
							.value(value)
							.roomProfileCustomField(customField)
							.build();

					memberProfileCard.addAnswer(answer);
				});
			}
			return memberProfileCard;
		}
}
