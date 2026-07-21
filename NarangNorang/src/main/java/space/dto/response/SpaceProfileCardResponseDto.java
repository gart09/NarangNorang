package space.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import space.dto.resquest.SpaceCreateRequestDto;
import space.entity.SpaceProfileCard;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceProfileCardResponseDto {
    private String name;
    private String owner;
    private List<String> techStack;
    private LocalDateTime preferredStartTime;
    private LocalDateTime preferredEndTime;
    private Map<String, List<String>> customField;

    public SpaceProfileCardResponseDto from(SpaceProfileCard card) {
        return SpaceProfileCardResponseDto.builder()
                .name(card.getName())
                .owner(card.getOwner())
                .techStack(card.getTechStack())
                .preferredStartTime(card.getPreferredStartTime())
                .preferredEndTime(card.getPreferredEndTime())
                .customField(card.getCustomField())
                .build();
    }
}