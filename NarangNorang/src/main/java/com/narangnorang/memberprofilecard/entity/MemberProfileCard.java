package com.narangnorang.memberprofilecard.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.narangnorang.room.entity.Room;
import com.narangnorang.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "member_profile_card",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_profile_card_user_room",
                columnNames = {"user_id", "room_id"}
        )
)
@Getter
@NoArgsConstructor
public class MemberProfileCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50)
	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name= "room_id")
	private Room room;

	@OneToMany(mappedBy = "memberProfileCard", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MemberProfileCustomAnswer> answers = new ArrayList<>();
}
