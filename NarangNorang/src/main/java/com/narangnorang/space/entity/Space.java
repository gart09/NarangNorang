package com.narangnorang.space.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String name;

    private Long maxMemberCount;

    private Long currentMemberCount;

    @Builder.Default
    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceMember> spaceMembers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> tags = new ArrayList<>();

    @OneToOne(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private SpaceProfileCard profileCard;

    
    public void updateInfo(String name, Long maxMemberCount) {
        this.name = name;
        this.maxMemberCount = maxMemberCount;
    }
    
    public void updateCurrentMember(Long currentMemberCount) {
    	if(currentMemberCount > maxMemberCount)
    		throw new IllegalStateException("정원 초과입니다.");
        this.currentMemberCount = currentMemberCount;
    }
    
    public void updateOwnerId(Long ownerId) {
    	this.ownerId = ownerId;
    }
    
    public void assignProfileCard(SpaceProfileCard profileCard) {
        this.profileCard = profileCard;
    }

    public void addTag(String tagName) {
        this.tags.add(Tag.builder().space(this).name(tagName).build());
    }

    public void addMember(SpaceMember spaceMember) {
        this.spaceMembers.add(spaceMember);
    }
    
    public void transferOwner(Long newOwnerId) {
        this.ownerId = newOwnerId;
    }
}