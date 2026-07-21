//package com.narangnorang.space.entity;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import jakarta.persistence.CascadeType;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.OneToMany;
//import jakarta.persistence.OneToOne;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Space {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private Long roomId;
//
//    @Column(nullable = false)
//    private Long ownerId;
//
//    @Column(nullable = false)
//    private String name;
//
//    private Long maxMemberCount;
//
//    private Long currentMemberCount;
//
//    @OneToMany(mappedBy = "com/narangnorang/space", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<SpaceMember> spaceMembers = new ArrayList<>();
//
//    @OneToMany(mappedBy = "com/narangnorang/space", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Tag> tags = new ArrayList<>();
//
//    @OneToOne(mappedBy = "com/narangnorang/space", cascade = CascadeType.ALL, orphanRemoval = true)
//    private SpaceProfileCard profileCard;
//
//}