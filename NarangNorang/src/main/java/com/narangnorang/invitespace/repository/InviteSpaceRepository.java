package com.narangnorang.invitespace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.narangnorang.invitespace.entity.InviteSpace;
import com.narangnorang.invitespace.entity.InviteStatus;

public interface InviteSpaceRepository extends JpaRepository<InviteSpace, Long> {

    boolean existsBySpaceIdAndMemberIdAndStatus(Long spaceId, Long memberId, InviteStatus status);

    @Query("""
            SELECT i FROM InviteSpace i
            JOIN FETCH i.space
            JOIN FETCH i.memberProfileCard
            WHERE i.space.id = :spaceId AND i.status = :status
            """)
    List<InviteSpace> findBySpaceIdAndStatus(@Param("spaceId") Long spaceId, @Param("status") InviteStatus status);

    @Query("""
            SELECT i FROM InviteSpace i
            JOIN FETCH i.space
            JOIN FETCH i.memberProfileCard
            WHERE i.memberId = :memberId AND i.status = :status
            """)
    List<InviteSpace> findByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") InviteStatus status);

    @Query("""
            SELECT i FROM InviteSpace i
            JOIN FETCH i.space
            JOIN FETCH i.memberProfileCard
            WHERE i.space.ownerId = :ownerId AND i.status = :status
            """)
    List<InviteSpace> findByOwnerUserIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") InviteStatus status);
    
    @Query("""
            SELECT i FROM InviteSpace i
            JOIN FETCH i.space
            JOIN FETCH i.memberProfileCard
            WHERE i.id = :id
            """)
    Optional<InviteSpace> findByIdWithDetails(@Param("id") Long id);
}
    