package com.narangnorang.space.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.narangnorang.space.entity.SpaceMember;

public interface SpaceMemberRepository extends JpaRepository<SpaceMember, Long> {

    boolean existsBySpaceIdAndUserId(Long spaceId, Long userId);

    void deleteBySpaceId(Long spaceId);

    void deleteBySpaceIdAndUserId(Long spaceId, Long userId);
    
    Optional<SpaceMember> findBySpaceIdAndUserId(Long spaceId, Long userId);
    
}