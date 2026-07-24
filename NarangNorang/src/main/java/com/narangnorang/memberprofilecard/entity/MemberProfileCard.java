package com.narangnorang.memberprofilecard.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.narangnorang.room.entity.Room;
import com.narangnorang.space.entity.SpaceMember;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

	@Builder.Default
	@OneToMany(mappedBy = "memberProfileCard", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MemberProfileCustomAnswer> answers = new ArrayList<>();

	// Space 멤버 추가
	@Builder.Default
	@OneToMany(mappedBy = "memberProfileCard", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SpaceMember> spaceMembers = new ArrayList<>();
	
	public void addAnswer(MemberProfileCustomAnswer answer){
		this.answers.add(answer);
		answer.assignMemberProfileCard(this);
	}

	public void updateName(String name){
		this.name = name;
	}

	public void updateAnswers(List<MemberProfileCustomAnswer> newAnswers) {
		if (newAnswers == null || newAnswers.isEmpty()) {
			return;
		}

		Map<Long, MemberProfileCustomAnswer> answerMap = answers.stream()
				.collect(Collectors.toMap(
						answer -> answer.getRoomProfileCustomField().getId(),
						answer -> answer
				));

		for(MemberProfileCustomAnswer newAnswer : newAnswers){
			MemberProfileCustomAnswer existingAnswer = answerMap.get(newAnswer.getRoomProfileCustomField().getId());
			if(existingAnswer != null){
				existingAnswer.updateValue(newAnswer.getValue());
			}else{
				addAnswer(newAnswer);
			}
		}
	}

	public void updateDate(){
		this.updatedAt = LocalDateTime.now();
	}
}