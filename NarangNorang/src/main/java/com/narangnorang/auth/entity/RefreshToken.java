package com.narangnorang.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String tokenKey;

    private LocalDateTime expiredAt;

    // 재발급/재로그인 시 토큰 값 교체 (Rotation)
    public void updateToken(String newTokenKey, LocalDateTime newExpiredAt) {
        this.tokenKey = newTokenKey;
        this.expiredAt = newExpiredAt;
    }
}