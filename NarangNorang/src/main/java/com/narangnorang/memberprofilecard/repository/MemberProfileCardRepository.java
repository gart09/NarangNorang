package com.narangnorang.memberprofilecard.repository;

import com.narangnorang.common.querydsl.MemberProfileCardQuerydslRepository;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardCreateRequestDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardCreateResponseDto;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberProfileCardRepository extends JpaRepository<MemberProfileCard, Long>, MemberProfileCardQuerydslRepository {

	Optional<MemberProfileCard> findByUserIdAndRoomId(Long userId, Long roomId);

	@Query("SELECT m.name FROM MemberProfileCard m WHERE m.user.id = :userId")
	String findNameById(@Param("userId") Long id);

	boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    long countByRoomId(Long roomId);
}
