package com.narangnorang.memberprofilecard.dto.response;

import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
	public class MemberProfileCardReadResponseDto {
		private Long id;
		private String name;
		private LocalDateTime createdAt;
		private LocalDateTime updatedAt;
		private Long userId;
		private Long roomId;
		private List<MemberProfileCustomAnswerResponseDto> answers;

		public static MemberProfileCardReadResponseDto from(MemberProfileCard memberProfileCard){
			return MemberProfileCardReadResponseDto.builder()
					.id(memberProfileCard.getId())
					.name(memberProfileCard.getName())
					.createdAt(LocalDateTime.now())
					.updatedAt(LocalDateTime.now())
					.userId(memberProfileCard.getUser().getId())
					.roomId(memberProfileCard.getRoom().getId())
					.answers(memberProfileCard.getAnswers().stream().map(MemberProfileCustomAnswerResponseDto::from).toList())
					.build();
		}
	}