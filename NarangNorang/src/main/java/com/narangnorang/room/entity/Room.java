package com.narangnorang.room.entity;

import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 512)
    private String description;

    @Column(name = "max_member", nullable = false)
    private Integer maxMember;

    @Column(name = "room_code", nullable = false, unique = true, length = 6)
    private String roomCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Builder.Default
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomProfileCustomField> customFields = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MemberProfileCard> memberProfileCards = new ArrayList<>();

    public void addCustomField(RoomProfileCustomField customField) {
        customFields.add(customField);
        customField.assignRoom(this);
    }

    public void updateInfo(String name, String description, Integer maxMember) {
        this.name = name;
        this.description = description;
        this.maxMember = maxMember;
    }
}
