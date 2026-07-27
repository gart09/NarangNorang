package com.narangnorang.memberprofilecard.entity;

import com.narangnorang.room.entity.RoomProfileCustomField;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberProfileCustomAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 255)
	private String value;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "field_id")
	private RoomProfileCustomField roomProfileCustomField;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private MemberProfileCard memberProfileCard;

	public void assignMemberProfileCard(MemberProfileCard memberProfileCard) {
		this.memberProfileCard = memberProfileCard;
	}

	public void updateValue(String value){
		this.value = value;
	}
}