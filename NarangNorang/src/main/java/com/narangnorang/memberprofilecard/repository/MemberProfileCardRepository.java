package com.narangnorang.memberprofilecard.repository;

import com.narangnorang.common.querydsl.MemberProfileCardQuerydslRepository;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardCreateRequestDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardCreateResponseDto;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.room.entity.Room;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberProfileCardRepository extends JpaRepository<MemberProfileCard, Long>, MemberProfileCardQuerydslRepository {

	Optional<MemberProfileCard> findByUserIdAndRoomId(Long userId, Long roomId);

	@Query("SELECT m.name FROM MemberProfileCard m WHERE m.user.id = :userId AND m.room.id = :roomId")
	String findNameByRoomIdAndUserUserId(@Param("roomId")Long roomId, @Param("userId") Long userId);

	@Query("SELECT m FROM MemberProfileCard m " +
			"JOIN m.spaceMembers sm " +
			"WHERE sm.space.id = :spaceId AND m.user.id = :userId")
	Optional<MemberProfileCard> findByUserIdAndSpaceId(@Param("spaceId")Long spaceId, @Param("userId") Long userId);

	@Query("SELECT m.name FROM MemberProfileCard m " +
			"JOIN m.spaceMembers sm " +
			"WHERE sm.space.id = :spaceId AND m.user.id = :userId")
	String findNameByUserIdAndSpaceId(@Param("spaceId")Long spaceId, @Param("userId") Long userId);

	boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    long countByRoomId(Long roomId);

	@Query("select m.room from MemberProfileCard m where m.user.id = :userId")
	List<Room> findRoomsByUserId(Long userId);
}
