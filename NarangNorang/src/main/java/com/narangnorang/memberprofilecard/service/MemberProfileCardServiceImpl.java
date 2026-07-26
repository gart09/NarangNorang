package com.narangnorang.memberprofilecard.service;

import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardCreateRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardReadRequestDto;
import com.narangnorang.memberprofilecard.dto.request.MemberProfileCardUpdateRequestDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardCreateResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardReadResponseDto;
import com.narangnorang.memberprofilecard.dto.response.MemberProfileCardUpdateResponseDto;
import com.narangnorang.memberprofilecard.dto.response.RoomsListResponseDto;
import com.narangnorang.memberprofilecard.entity.MemberProfileCard;
import com.narangnorang.memberprofilecard.entity.MemberProfileCustomAnswer;
import com.narangnorang.memberprofilecard.exception.MemberProfileCardErrorCode;
import com.narangnorang.memberprofilecard.exception.MemberProfileCardException;
import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.room.dto.response.RoomResponseDto;
import com.narangnorang.room.entity.OptionType;
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

import java.util.*;
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
		User user = userRepository.findById(userId).orElseThrow(
				() -> new MemberProfileCardException(MemberProfileCardErrorCode.USER_NOT_FOUND));
		Room room = roomRepository.findByRoomCode(requestDto.getRoomCode()).orElseThrow(
				() -> new MemberProfileCardException(MemberProfileCardErrorCode.ROOM_NOT_FOUND));

		if(memberProfileCardRepository.existsByUserIdAndRoomId(user.getId(), room.getId())){
			throw new MemberProfileCardException(MemberProfileCardErrorCode.MEMBER_PROFILE_CARD_DUPLICATED);
		}

		Map<Long, RoomProfileCustomField> fieldMap = getFieldMap(requestDto.getAnswers());

		checkRequiredField(room, requestDto.getAnswers());

		MemberProfileCard savedCard = memberProfileCardRepository.save(requestDto.toEntity(user, room, fieldMap));

		return MemberProfileCardCreateResponseDto.from(savedCard);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MemberProfileCardReadResponseDto> findMemberProfileCard(Long userId, MemberProfileCardReadRequestDto requestDto) {

		log.info("userId: {}, roomId: {}", userId, requestDto.getRoomId());
		if(memberProfileCardRepository.existsByUserIdAndRoomId(userId, requestDto.getRoomId()) == false){
			throw new MemberProfileCardException(MemberProfileCardErrorCode.USER_NOT_PERMITTED);
		}

		List<MemberProfileCard> memberProfileCards = memberProfileCardRepository.searchMemberProfileCards(requestDto);

		return memberProfileCards.stream().map(MemberProfileCardReadResponseDto::from).toList();
	}

	@Override
	@Transactional
	public MemberProfileCardUpdateResponseDto updateMemberProfileCard(Long userId, MemberProfileCardUpdateRequestDto requestDto) {
		MemberProfileCard memberProfileCard = memberProfileCardRepository.findById(requestDto.getMemberProfileCardId())
				.orElseThrow(
						() -> new MemberProfileCardException(MemberProfileCardErrorCode.MEMBER_PROFILE_CARD_NOT_FOUND));

		if(memberProfileCard.getUser().getId().equals(userId) == false){
			throw new MemberProfileCardException(MemberProfileCardErrorCode.USER_NOT_PERMITTED);
		}

		Room room = memberProfileCard.getRoom();

		checkRequiredField(room, requestDto.getAnswers());


		Set<Long> requestedFieldIds = requestDto.getAnswers().keySet();

		// 원래 멤버프로필카드에 있던 항목이지만, requestDto에 없는 항목이라면 삭제된 항목이므로, 미리 삭제시킨다.
		memberProfileCard.getAnswers().removeIf(answer -> requestedFieldIds.contains(answer.getRoomProfileCustomField().getId()) == false);

		// requestDto의 fieldId를 가지고 있는 룸프로필커스텀필드 리스트를 만듬
		List<RoomProfileCustomField> roomProfileCustomFields = roomProfileCustomFieldRepository.findAllById(requestedFieldIds);

		//Map으로 커스텀필드id랑 엔티티를 연결(newAnswers만들때 편하게 만들기 위해서)
		Map<Long, RoomProfileCustomField> newCustomFieldMap = roomProfileCustomFields.stream()
				.collect(Collectors.toMap(RoomProfileCustomField::getId, Function.identity()));

		List<MemberProfileCustomAnswer> newAnswers = requestDto.getAnswers().entrySet().stream()
				.map(entry -> MemberProfileCustomAnswer.builder()
						.memberProfileCard(memberProfileCard)
						.roomProfileCustomField(newCustomFieldMap.get(entry.getKey()))
						.value(entry.getValue())
						.build())
				.toList();

		memberProfileCard.updateAnswers(newAnswers);
		memberProfileCard.updateName(requestDto.getName());
		memberProfileCard.updateDate();


		//@Transactional로 자동으로 변경 감지하고 저장하므로 save()는 호출 불필요
		return MemberProfileCardUpdateResponseDto.from(memberProfileCard);
	}

	@Override
	public void deleteMemberProfileCard(Long userId, Long memberProfileCardId) {
		MemberProfileCard memberProfileCard = memberProfileCardRepository.findById(memberProfileCardId).orElseThrow(() -> new MemberProfileCardException(MemberProfileCardErrorCode.MEMBER_PROFILE_CARD_NOT_FOUND));

		if(memberProfileCard.getUser().getId().equals(userId) == false){
			throw new MemberProfileCardException(MemberProfileCardErrorCode.USER_NOT_PERMITTED);
		}
		memberProfileCardRepository.delete(memberProfileCard);
	}

	@Override
	@Transactional(readOnly = true)
	public RoomsListResponseDto getRoomsList(Long userId) {
		userRepository.findById(userId).orElseThrow(() -> new MemberProfileCardException(MemberProfileCardErrorCode.USER_NOT_FOUND));
		List<Room> roomsList = memberProfileCardRepository.findRoomsByUserId(userId);

		return RoomsListResponseDto.builder()
				.rooms(roomsList.stream()
						.map(room -> {
							long currentMemberCount = memberProfileCardRepository.countByRoomId(room.getId());
							return RoomResponseDto.from(room, currentMemberCount);
						})
						.toList())
				.build();
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

	private void checkRequiredField(Room room, Map<Long, String> answers){

		Set<Long> requestedFieldIds = answers.keySet();

		//1. 필수값 누락 확인
		List<RoomProfileCustomField> requiredField = roomProfileCustomFieldRepository.findAllByRoomIdAndRequiredTrue(room.getId());
		List<String> missingRequiredFieldNames = requiredField.stream()
				.filter(field -> !requestedFieldIds.contains(field.getId()))
				.map(RoomProfileCustomField::getFieldName)
				.toList();

		if(missingRequiredFieldNames.isEmpty() == false){
			throw new MemberProfileCardException(MemberProfileCardErrorCode.REQUIRED_FIELD_MISS, missingRequiredFieldNames);
		}

		//2. 필수 필드값인데 필드값이 빈 칸인지 확인
		List<String> blanckedRequiredFieldNames = answers.entrySet().stream()
				.filter(entry -> requestedFieldIds.contains(entry.getKey()))
				.filter(entry -> entry.getValue().isBlank())
				.map(entry -> {
					RoomProfileCustomField field = roomProfileCustomFieldRepository.findById(entry.getKey()).orElseThrow();
					return field.getFieldName();
				})
				.toList();

		if(blanckedRequiredFieldNames.isEmpty() == false){
			throw new MemberProfileCardException(MemberProfileCardErrorCode.REQUIRED_FIELD_SPACE, blanckedRequiredFieldNames);
		}

		//3. 항목 선택 값인데 DB에 있는 항목 값이 아닌 다른 값이 들어오는 경우
		List<Map<String, String>> unmatchedSelectTypeFieldNames = answers.entrySet().stream()
				.filter(entry -> roomProfileCustomFieldRepository.existsByIdAndOptionTypeIn(
						entry.getKey(), List.of(OptionType.SINGLE_SELECT, OptionType.MULTI_SELECT)))
				.filter(entry -> {
					List<String> values = Arrays.stream(entry.getValue().split(","))
							.map(String::trim)
							.toList();
					for( String value : values) {
						if(roomProfileCustomFieldRepository.findSelectTypeOptionsByFieldId(entry.getKey()).contains(value) == false)
							return true;
					}
					return false;
				})
				.map(entry -> {
					RoomProfileCustomField field = roomProfileCustomFieldRepository.findById(entry.getKey()).orElseThrow();
					Map<String, String> map = new HashMap<>();
					map.put(field.getFieldName(), entry.getValue());
					return map;
				})
				.toList();

		if(unmatchedSelectTypeFieldNames.isEmpty() == false){
			throw new MemberProfileCardException(MemberProfileCardErrorCode.CANT_SELECT_FIELD, unmatchedSelectTypeFieldNames);
		}
	}
}
