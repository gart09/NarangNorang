package com.narangnorang.memberprofilecard.repository;

import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberProfileCardRepository extends JpaRepository<MemberProfileCard, Long> {
    Optional<MemberProfileCard> findByUserIdAndRoomId(Long userId, Long roomId);

    boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    long countByRoomId(Long roomId);
}
