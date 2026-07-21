package space.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import space.entity.Space;
import space.entity.Tag;

//목록 조회용 (가벼운 정보만)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceSummaryResponseDto {
 private Long id;
 private String name;
 private Long currentMember;
 private Long maxMember;
 private List<String> tags;

 public SpaceSummaryResponseDto from(Space space) {
     return SpaceSummaryResponseDto.builder()
             .id(space.getId())
             .name(space.getName())
             .currentMember(space.getCurrentMember())
             .maxMember(space.getMaxMember())
             .tags(space.getTags().stream().map(Tag::getName).toList())
             .build();
 }
}