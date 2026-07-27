package com.narangnorang.space.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.narangnorang.space.entity.converter.CustomFieldConverter;
import com.narangnorang.space.entity.converter.TechStackConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class SpaceProfileCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false, unique = true)
    private Space space;

    private String name;

    @Convert(converter = TechStackConverter.class)
    @Column(name = "tech_stack", columnDefinition = "json")
    private List<String> techStack;

    @Column(name = "preferred_start_time")
    private LocalTime preferredStartTime;

    @Column(name = "preferred_end_time")
    private LocalTime preferredEndTime;

    @Convert(converter = CustomFieldConverter.class)
    @Column(name = "custom_field", columnDefinition = "json")
    private Map<String, List<String>> customField;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    
    public void updateProfileCard(List<String> techStack, LocalTime preferredStartTime, LocalTime preferredEndTime) {
        this.techStack = techStack;
        this.preferredStartTime = preferredStartTime;
        this.preferredEndTime = preferredEndTime;
    }

    public void updateCustomField(Map<String, List<String>> customField) {
        this.customField = customField;
    }
}