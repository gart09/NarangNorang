package com.narangnorang.common.querydsl;

import static com.narangnorang.memberprofilecard.entity.QMemberProfileCard.memberProfileCard;
import static com.narangnorang.memberprofilecard.entity.QMemberProfileCustomAnswer.memberProfileCustomAnswer;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardReadRequestDto;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberProfileCardQuerydslRepositoryImpl implements MemberProfileCardQuerydslRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<MemberProfileCard> searchMemberProfileCards(MemberProfileCardReadRequestDto requestDto){
		return queryFactory
				.selectFrom(memberProfileCard)
				.where(
						nameEq(requestDto.getName()),
						memberProfileCardIdEq(requestDto.getMemberProfileCardId()),
						roomIdEq(requestDto.getRoomId()),
						customFieldsEq(requestDto.getCustomFields()) // 커스텀 필드 서브쿼리 조건
				)
				.fetch();
	}

	private BooleanExpression nameEq(String name) {
		return StringUtils.hasText(name) ? memberProfileCard.name.eq(name) : null;
	}

	private BooleanExpression memberProfileCardIdEq(Long memberProfileCardId) {
		return memberProfileCardId != null ? memberProfileCard.id.eq(memberProfileCardId) : null;
	}

	private BooleanExpression roomIdEq(Long roomId) {
		return roomId != null ? memberProfileCard.room.id.eq(roomId) : null;
	}

	private BooleanBuilder customFieldsEq(Map<Long, List<String>> customFields) {
		if (customFields == null || customFields.isEmpty()) {
			return null;
		}

		BooleanBuilder builder = new BooleanBuilder();

		for (Map.Entry<Long, List<String>> entry : customFields.entrySet()) {
			Long fieldId = entry.getKey();
			List<String> values = entry.getValue();

			if (values != null && !values.isEmpty()) {
				builder.and(
						JPAExpressions.selectOne()
								.from(memberProfileCustomAnswer)
								.where(
										memberProfileCustomAnswer.memberProfileCard.eq(memberProfileCard),
										memberProfileCustomAnswer.roomProfileCustomField.id.eq(fieldId),
										memberProfileCustomAnswer.value.in(values)
								)
								.exists()
				);
			}
		}
		return builder;
	}
}
