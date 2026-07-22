package com.narangnorang.memberprofilecard.repository;

import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberProfileCardRepository extends JpaRepository<MemberProfileCard, Long> {
	Optional<MemberProfileCard> findByUserIdAndRoomId(Long userId, Long roomId);

	@Query("SELECT m.name FROM MemberProfileCard m WHERE m.user.id = :userId")
	String findNameById(@Param("userId") Long id);
}
