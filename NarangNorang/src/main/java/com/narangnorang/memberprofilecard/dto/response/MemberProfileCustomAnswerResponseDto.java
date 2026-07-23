package com.narangnorang.memberprofilecard.dto.response;


import com.narangnorang.memberprofilecard.entity.MemberProfileCustomAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileCustomAnswerResponseDto {
	private Long id;
	private String value;
	private Long fieldId;
	private Long memberId;

	public static MemberProfileCustomAnswerResponseDto from(MemberProfileCustomAnswer memberProfileCustomAnswer) {
		return MemberProfileCustomAnswerResponseDto.builder()
				.id(memberProfileCustomAnswer.getId())
				.value(memberProfileCustomAnswer.getValue())
				.fieldId(memberProfileCustomAnswer.getRoomProfileCustomField().getId())
				.memberId(memberProfileCustomAnswer.getMemberProfileCard().getId())
				.build();
	}
}