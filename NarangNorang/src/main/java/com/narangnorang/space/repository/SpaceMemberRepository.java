package com.narangnorang.space.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.narangnorang.space.entity.SpaceMember;

public interface SpaceMemberRepository extends JpaRepository<SpaceMember, Long> {

    // 권한 체크용 — userId 기준, 조인 없음
    boolean existsBySpaceIdAndUserId(Long spaceId, Long userId);

    @Query("SELECT sm FROM SpaceMember sm WHERE sm.space.id = :spaceId")
    List<SpaceMember> findBySpaceIdWithProfileCard(@Param("spaceId") Long spaceId);

    void deleteBySpaceId(Long spaceId);

    void deleteBySpaceIdAndUserId(Long spaceId, Long userId);
}