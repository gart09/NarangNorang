package space.dto.resquest;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import space.entity.Space;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceCreateRequestDto {
	
    private String name;

    private Long maxMember;

    private List<String> tags;

    public Space toEntity(Long roomId, Long ownerId) {
        return Space.builder()
                .roomId(roomId)
                .ownerId(ownerId)
                .name(name)
                .maxMember(maxMember)
                .currentMember(0L)
                .build();
    }
}