package com.narangnorang.memberprofilecard.entity;

import com.narangnorang.room.entity.RoomProfileCustomField;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
	@JoinColumn(name = "field_id")
	private RoomProfileCustomField roomProfileCustomField;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private MemberProfileCard memberProfileCard;
}