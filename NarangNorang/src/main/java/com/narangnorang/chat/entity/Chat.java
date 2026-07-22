package com.narangnorang.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String targetType;
	private Long targetId;
	private Long senderId;
	@Column(length = 300)
	private String content;
	private LocalDateTime createdAt;
}
