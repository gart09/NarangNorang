//package com.narangnorang.space.entity;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
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
//public class SpaceProfileCard {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "space_id", nullable = false, unique = true)
//    private Space space;
//
//    private String name;
//
//    private String owner;
//
//    //@Convert(converter = TechStackConverter.class)
//    @Column(name = "tech_stack", columnDefinition = "json")
//    private List<String> techStack;
//
//    @Column(name = "preferred_start_time")
//    private LocalDateTime preferredStartTime;
//
//    @Column(name = "preferred_end_time")
//    private LocalDateTime preferredEndTime;
//
//    //@Convert(converter = CustomFieldConverter.class)
//    @Column(name = "custom_field", columnDefinition = "json")
//    private Map<String, List<String>> customField;
//
//    @CreatedDate
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//}