package com.narangnorang.memberprofilecard.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.narangnorang.space.entity.Space;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberProfileCard memberProfileCard;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Space space;

    @Column(nullable = false)
    private Long requestId;
    
    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void accept(Long requesterId) {
        if (!this.targetId.equals(requesterId)) {
            throw new IllegalStateException("본인에게 온 요청만 처리할 수 있습니다.");
        }
        if (this.status != InviteStatus.PENDING) {
            throw new IllegalStateException("이미 처리 완료된 가입/초대 요청입니다.");
        }
        this.status = InviteStatus.ACCEPTED;
    }

    public void reject(Long requesterId) {
        if (!this.targetId.equals(requesterId)) {
            throw new IllegalStateException("본인에게 온 요청만 처리할 수 있습니다.");
        }
        if (this.status != InviteStatus.PENDING) {
            throw new IllegalStateException("이미 처리 완료된 가입/초대 요청입니다.");
        }
        this.status = InviteStatus.REJECTED;
    }

    // 멤버가 스페이스에 신청
    public static InviteSpace memberToSpace(Space space, Long requestId, MemberProfileCard memberProfileCard) {
        return InviteSpace.builder()
                .space(space)
                .requestId(requestId)
                .targetId(space.getOwnerId())
                .memberProfileCard(memberProfileCard)
                .type(InviteType.MemberToSpace)
                .status(InviteStatus.PENDING)
                .build();
    }

    // 오너가 멤버에게 권유
    public static InviteSpace spaceToMember(Space space, Long requestId, Long targetUserId, MemberProfileCard memberProfileCard) {
        return InviteSpace.builder()
                .space(space)
                .requestId(requestId)
                .targetId(targetUserId)
                .memberProfileCard(memberProfileCard)
                .type(InviteType.SpaceToMember)
                .status(InviteStatus.PENDING)
                .build();
    }
    
    public enum InviteType {
        MemberToSpace,    // 멤버가 스페이스에 신청
        SpaceToMember    // 오너가 멤버에게 권유
    }

    public enum InviteStatus {
        PENDING, 		// 요청
        ACCEPTED, 		// 수락
        REJECTED		// 거절
    }
    
}


