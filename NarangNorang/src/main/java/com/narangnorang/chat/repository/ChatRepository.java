package com.narangnorang.chat.repository;

import com.narangnorang.chat.dto.request.ChatRequestDto;
import com.narangnorang.chat.entity.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
	Slice<Chat> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
			String targetType,
			Long targetId,
			Pageable pageable
	);
}
