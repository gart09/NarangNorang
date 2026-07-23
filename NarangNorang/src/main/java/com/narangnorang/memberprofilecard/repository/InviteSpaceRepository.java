package com.narangnorang.memberprofilecard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.narangnorang.memberprofilecard.entity.InviteSpace;
import com.narangnorang.memberprofilecard.entity.InviteSpace.InviteStatus;

public interface InviteSpaceRepository extends JpaRepository<InviteSpace, Long> {

	boolean existsBySpaceIdAndTargetIdAndStatus(Long spaceId, Long targetId, InviteStatus status);

    // 대기 목록 (신청/권유 전체)
    @Query("""
            SELECT i FROM InviteSpace i
            JOIN FETCH i.space
            JOIN FETCH i.memberProfileCard
            WHERE i.space.id = :spaceId AND i.status = :status
            """)
    List<InviteSpace> findBySpaceIdAndStatus(@Param("spaceId") Long spaceId, @Param("status") InviteStatus status);

    // 본인이 받은 초대/신청 목록 (target = 본인)
    List<InviteSpace> findByTargetIdAndStatus(Long targetId, InviteStatus status);
    

}