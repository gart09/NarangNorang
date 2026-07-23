package com.narangnorang.space.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.narangnorang.space.entity.SpaceProfileCard;

public interface SpaceProfileCardRepository extends JpaRepository<SpaceProfileCard, Long>{

	Optional<SpaceProfileCard> findBySpaceId(Long spaceId);
	
}
