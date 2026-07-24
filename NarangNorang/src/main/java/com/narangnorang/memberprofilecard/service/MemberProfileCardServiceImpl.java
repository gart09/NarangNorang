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
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
		Room room = roomRepository.findByRoomCode(requestDto.getRoomCode()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

		if(memberProfileCardRepository.existsByUserIdAndRoomId(user.getId(), room.getId())){
			throw new IllegalArgumentException("이미 존재하는 프로필카드입니다.");
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
		MemberProfileCard memberProfileCard = memberProfileCardRepository.findById(memberProfileCardId).orElseThrow();

		if(memberProfileCard.getUser().getId().equals(userId) == false){
			throw new IllegalArgumentException("해당 유저는 해당 멤버프로필카드를 삭제할 권한이 없습니다.");
		}
		memberProfileCardRepository.delete(memberProfileCard);
	}

	@Override
	@Transactional(readOnly = true)
	public RoomsListResponseDto getRoomsList(Long userId) {
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
			throw new IllegalArgumentException("필수 입력 항목이 누락됐습니다." + missingRequiredFieldNames);
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
			throw new IllegalArgumentException("필수 항목은 빈 칸 혹은 공백이 될 수 없습니다." + blanckedRequiredFieldNames);
		}

		//3. 항목 선택 값인데 DB에 있는 항목 값이 아닌 다른 값이 들어오는 경우
		List<Map<String, String>> unmatchedSelectTypeFieldNames = answers.entrySet().stream()
				.filter(entry -> roomProfileCustomFieldRepository.existsByIdAndOptionTypeIn(
						entry.getKey(), List.of(OptionType.SINGLE_SELECT, OptionType.MULTI_SELECT)))
				.filter(entry -> roomProfileCustomFieldRepository.findSelectTypeOptionsByFieldId(
						entry.getKey()).contains(entry.getValue()) == false)
				.map(entry -> {
					RoomProfileCustomField field = roomProfileCustomFieldRepository.findById(entry.getKey()).orElseThrow();
					Map<String, String> map = new HashMap<>();
					map.put(field.getFieldName(), entry.getValue());
					return map;
				})
				.toList();

		if(unmatchedSelectTypeFieldNames.isEmpty() == false){
			throw new IllegalArgumentException("선택 가능하지 않은 항목입니다. " + unmatchedSelectTypeFieldNames);
		}
	}
}
