package space.dto.resquest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import space.entity.Space;
import space.entity.SpaceProfileCard;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceProfileCardCreateRequestDto {
    private String name;
    private List<String> techStack;
    private LocalDateTime preferredStartTime;
    private LocalDateTime preferredEndTime;
    private Map<String, List<String>> customField;
    
    public SpaceProfileCard toEntity(LocalDateTime createdAt) {
        return SpaceProfileCard.builder()
                .name(name)
                .techStack(techStack)
                .preferredStartTime(preferredStartTime)
                .preferredEndTime(preferredEndTime)
                .customField(customField)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
