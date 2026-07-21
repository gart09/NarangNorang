//package com.narangnorang.space.dto.response;
//
//import java.util.List;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import com.narangnorang.space.entity.Space;
//import com.narangnorang.space.entity.Tag;
//
////목록 조회용 (가벼운 정보만)
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class SpaceSummaryResponseDto {
// private Long id;
// private String name;
// private Long currentMemberCount;
// private Long maxMemberCount;
// private List<String> tags;
//
// public SpaceSummaryResponseDto from(Space space) {
//     return SpaceSummaryResponseDto.builder()
//             .id(space.getId())
//             .name(space.getName())
//             .currentMemberCount(space.getCurrentMemberCount())
//             .maxMemberCount(space.getMaxMemberCount())
//             .tags(space.getTags().stream().map(Tag::getName).toList())
//             .build();
// }
//}