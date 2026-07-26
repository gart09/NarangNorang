package com.narangnorang.invitespace.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
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
    @JoinColumn(name = "member_profile_card_id", nullable = false)
    private MemberProfileCard memberProfileCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 이 요청을 승인/거절할 권한이 있는 사람 — type에 따라 memberId 또는 ownerId
    public Long getApproverId() {
        return this.type == InviteType.MemberToSpace ? this.space.getOwnerId() : this.memberId;
    }

    public void accept() {
        this.status = InviteStatus.ACCEPTED;
    }

    public void reject() {
        this.status = InviteStatus.REJECTED;
    }

    // 멤버가 스페이스에 신청
    public static InviteSpace memberToSpace(Space space, Long memberUserId, MemberProfileCard memberProfileCard) {
        return InviteSpace.builder()
                .space(space)
                .memberId(memberUserId)
                .memberProfileCard(memberProfileCard)
                .type(InviteType.MemberToSpace)
                .status(InviteStatus.PENDING)
                .build();
    }

    // 오너가 멤버에게 권유
    public static InviteSpace spaceToMember(Space space, Long memberUserId, MemberProfileCard memberProfileCard) {
        return InviteSpace.builder()
                .space(space)
                .memberId(memberUserId)
                .memberProfileCard(memberProfileCard)
                .type(InviteType.SpaceToMember)
                .status(InviteStatus.PENDING)
                .build();
    }
    
}


