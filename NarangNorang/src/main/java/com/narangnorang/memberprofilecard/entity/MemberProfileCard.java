package com.narangnorang.memberprofilecard.entity;

import com.narangnorang.room.entity.Room;
import com.narangnorang.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	public void addAnswer(MemberProfileCustomAnswer answer){
		this.answers.add(answer);
		answer.assignMemberProfileCard(this);
	}

	public void updateName(String name){
		this.name = name;
	}

	public void updateAnswers(Map<Long, String> newAnswers) {
		if (newAnswers == null || newAnswers.isEmpty()) {
			return;
		}

		this.answers.forEach(existingAnswer -> {
			Long fieldId = existingAnswer.getRoomProfileCustomField().getId();

			if (newAnswers.containsKey(fieldId)) {
				existingAnswer.updateValue(newAnswers.get(fieldId));
			}
		});
	}

	public void updateAnswersWithNewField(List<MemberProfileCustomAnswer> newAnswers){
		this.answers.clear();
		if(newAnswers != null && newAnswers.isEmpty() == false)
			this.answers.addAll(newAnswers);
	}
}