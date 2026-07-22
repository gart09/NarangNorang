package com.narangnorang.space.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.narangnorang.space.entity.Space;
import com.narangnorang.space.entity.SpaceProfileCard;

public interface SpaceRepository extends JpaRepository<Space, Long>{

	List<Space> findByRoomId(Long roomId);
	
	@Query("""
			SELECT DISTINCT s FROM Space s
		    JOIN FETCH s.profileCard
		    LEFT JOIN FETCH s.tags
		    WHERE s.id = :spaceId
			""")
    Optional<Space> findByIdWithProfileCard(@Param("spaceId") Long spaceId);
	
	@Query("SELECT DISTINCT s FROM Space s JOIN s.tags t WHERE s.roomId = :roomId AND t.name IN :tagNames")
    List<Space> findByRoomIdAndTagNames(@Param("roomId") Long roomId, @Param("tagNames") List<String> tagNames);

	
}
