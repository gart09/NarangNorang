package com.narangnorang.memberprofilecard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_profile_custom_answer")
@Getter
@NoArgsConstructor
public class MemberProfileCustomAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 255)
	private String value;

	@ManyToOne(fetch = FetchType.LAZY)
	@Column(name = "field_id")
	private RoomProfileCustomField roomProfileCustomField;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private MemberProfileCard memberProfileCard;
}