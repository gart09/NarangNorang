package com.narangnorang.memberprofilecard.service;

import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardCreateRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardReadRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardUpdateRequestDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardCreateResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardReadResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardUpdateResponseDto;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.entity.MemberProfileCustomAnswer;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.entity.RoomProfileCustomField;
import com.narangnorang.room.repository.RoomProfileCustomFieldRepository;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberProfileCardServiceImpl implements MemberProfileCardService{

	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final MemberProfileCardRepository memberProfileCardRepository;
	private final RoomProfileCustomFieldRepository roomProfileCustomFieldRepository;

	@Override
	public MemberProfileCardCreateResponseDto createMemberProfileCard(Long userId, MemberProfileCardCreateRequestDto requestDto) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
		Room room = roomRepository.findByRoomCode(requestDto.getRoomCode()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

		if(memberProfileCardRepository.existsByUserIdAndRoomId(user.getId(), room.getId())){
			throw new IllegalArgumentException("이미 존재하는 프로필카드입니다.");
		}

		Map<Long, RoomProfileCustomField> fieldMap = getFieldMap(requestDto.getAnswers());

		//필수 항목 포함 안할 시 예외 발생
		checkRequiredField(room, fieldMap);


		MemberProfileCard savedCard = memberProfileCardRepository.save(requestDto.toEntity(user, room, fieldMap));

		return MemberProfileCardCreateResponseDto.from(savedCard);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MemberProfileCardReadResponseDto> findMemberProfileCard(Long userId, MemberProfileCardReadRequestDto requestDto) {

		log.info("userId: {}, roomId: {}", userId, requestDto.getRoomId());
		if(memberProfileCardRepository.existsByUserIdAndRoomId(userId, requestDto.getRoomId()) == false){
			throw new IllegalArgumentException("해당 유저는 해당 방에 속해있지 않습니다.");
		}

		List<MemberProfileCard> memberProfileCards = memberProfileCardRepository.searchMemberProfileCards(requestDto);

		return memberProfileCards.stream().map(MemberProfileCardReadResponseDto::from).toList();
	}

	@Override
	@Transactional
	public MemberProfileCardUpdateResponseDto updateMemberProfileCard(Long userId, MemberProfileCardUpdateRequestDto requestDto) {
		MemberProfileCard memberProfileCard = memberProfileCardRepository.findById(requestDto.getMemberProfileCardId())
				.orElseThrow(() -> new IllegalArgumentException("해당 프로필 카드가 존재하지 않습니다."));

		if(memberProfileCard.getUser().getId().equals(userId) == false){
			throw new IllegalArgumentException("해당 유저는 해당 멤버프로필카드를 수정할 권한이 없습니다.");
		}

		if(requestDto.isMemberProfileCardUpdated() == true){

			Set<Long> requestedFieldIds = requestDto.getAnswers().keySet();
			List<RoomProfileCustomField> validFields = roomProfileCustomFieldRepository.findAllById(requestedFieldIds);
			if (validFields.size() != requestedFieldIds.size()) {
				throw new IllegalArgumentException("유효하지 않은 프로필 항목 ID가 포함되어 있습니다.");
			}

			Map<Long, RoomProfileCustomField> validFieldMap = validFields.stream()
					.collect(Collectors.toMap(RoomProfileCustomField::getId, field -> field));

			checkRequiredField(memberProfileCard.getRoom(), validFieldMap);

			List<MemberProfileCustomAnswer> newAnswers = requestDto.getAnswers().entrySet().stream()
					.map(entry -> MemberProfileCustomAnswer.builder()
							.memberProfileCard(memberProfileCard)
							.roomProfileCustomField(validFieldMap.get(entry.getKey()))
							.value(entry.getValue())
							.build())
					.toList();
			memberProfileCard.updateAnswersWithNewField(newAnswers);
		}
		else
			memberProfileCard.updateAnswers(requestDto.getAnswers());

		memberProfileCard.updateName(requestDto.getName());
		memberProfileCard.updateDate();


		//@Transactional로 자동으로 변경 감지하고 저장하므로 save()는 호출 불필요
		return MemberProfileCardUpdateResponseDto.from(memberProfileCard);
	}

	@Override
	public void deleteMemberProfileCard(Long userId, Long memberProfileCardId) {
		MemberProfileCard memberProfileCard = memberProfileCardRepository.findById(memberProfileCardId).orElseThrow();

		if(memberProfileCard.getUser().getId().equals(userId) == false){
			throw new IllegalArgumentException("해당 유저는 해당 멤버프로필카드를 삭제할 권한이 없습니다.");
		}
		memberProfileCardRepository.delete(memberProfileCard);
	}

	private Map<Long, RoomProfileCustomField> getFieldMap(Map<Long, String> answers) {
		if (answers == null || answers.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<Long> fieldIds = answers.keySet();

		return roomProfileCustomFieldRepository.findAllById(fieldIds).stream()
				.collect(Collectors.toMap(
						RoomProfileCustomField::getId,
						Function.identity()
				));
	}

	private void checkRequiredField(Room room, Map<Long, RoomProfileCustomField> fieldMap){
		Map<Long, RoomProfileCustomField> requiredFieldMap = room.getCustomFields().stream()
				.filter(RoomProfileCustomField::isRequired)
				.collect(Collectors.toMap(
						RoomProfileCustomField::getId,
						Function.identity()
				));

		List<String> missingFields = requiredFieldMap.values().stream()
				.filter(field -> {
					Long fieldId = field.getId();

					if(fieldMap.containsKey(fieldId) == false)
						return true;

					String value = fieldMap.get(fieldId).getFieldName();
					return value.isBlank();
				})
				.map(RoomProfileCustomField::getFieldName)
				.toList();

		if(missingFields.isEmpty() == false){
			String joinedFieldNames = String.join(", ", missingFields);
			throw new IllegalArgumentException("필수 입력 항목이 누락됐습니다. 누락된 항목: " + joinedFieldNames);
		}
	}


}
