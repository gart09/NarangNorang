package com.narangnorang.memberprofilecard.repository;

import com.narangnorang.memberprofilecard.entity.MemberProfileCustomAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileCustomAnswerRepository extends JpaRepository<MemberProfileCustomAnswer, Long> {
	void deleteByRoomProfileCustomFieldId(Long fieldId);
}
