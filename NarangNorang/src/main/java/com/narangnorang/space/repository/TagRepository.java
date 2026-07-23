package com.narangnorang.space.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.narangnorang.space.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long>{
	
    // 스페이스에 달린 태그 목록 조회 (상세 조회, 태그 없을 때 update 등)
    List<Tag> findBySpaceId(Long spaceId);

    // 삭제/수정 시 기존 태그 전체 제거
    void deleteBySpaceId(Long spaceId);

}
