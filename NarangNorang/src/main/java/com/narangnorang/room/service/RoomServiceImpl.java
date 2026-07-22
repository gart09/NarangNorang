package com.narangnorang.room.service;

import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.room.dto.request.RoomCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldOptionCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldOptionUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomUpdateRequestDto;
import com.narangnorang.room.dto.response.RoomProfileCustomFieldResponseDto;
import com.narangnorang.room.dto.response.RoomResponseDto;
import com.narangnorang.room.entity.OptionType;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.entity.RoomProfileCustomField;
import com.narangnorang.room.entity.RoomProfileCustomFieldOption;
import com.narangnorang.room.repository.RoomProfileCustomFieldOptionRepository;
import com.narangnorang.room.repository.RoomProfileCustomFieldRepository;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private static final String ROOM_CODE_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOM_CODE_LENGTH = 6;
    private static final int ROOM_CODE_MAX_ATTEMPTS = 20;

    private final RoomRepository roomRepository;
    private final RoomProfileCustomFieldRepository customFieldRepository;
    private final RoomProfileCustomFieldOptionRepository optionRepository;
    private final UserRepository userRepository;
    private final MemberProfileCardRepository memberProfileCardRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public RoomResponseDto createRoom(RoomCreateRequestDto requestDto, String email) {
        User owner = findUser(email);
        validateMaxMember(requestDto.getMaxMember(), 0L);

        Room room = Room.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .maxMember(requestDto.getMaxMember())
                .roomCode(generateRoomCode())
                .owner(owner)
                .build();

        if (requestDto.getCustomFields() != null) {
            for (RoomProfileCustomFieldCreateRequestDto fieldDto : requestDto.getCustomFields()) {
                room.addCustomField(createCustomField(fieldDto));
            }
        }

        Room savedRoom = roomRepository.save(room);
        long currentMember = memberProfileCardRepository.countByRoomId(savedRoom.getId());
        return RoomResponseDto.from(savedRoom, currentMember);
    }

    @Override
    public RoomResponseDto getRoom(Long roomId, String email) {
        Room room = findRoom(roomId);
        User user = findUser(email);
        validateRoomMember(room, user);

        long currentMember = memberProfileCardRepository.countByRoomId(room.getId());
        return RoomResponseDto.from(room, currentMember);
    }

    @Override
    @Transactional
    public RoomResponseDto updateRoom(
            Long roomId,
            RoomUpdateRequestDto requestDto,
            String email
    ) {
        Room room = findRoom(roomId);
        User user = findUser(email);

        validateOwner(room, user);
        long currentMember = memberProfileCardRepository.countByRoomId(room.getId());
        validateMaxMember(requestDto.getMaxMember(), currentMember);

        room.updateInfo(requestDto.getName(), requestDto.getDescription(), requestDto.getMaxMember());
        return RoomResponseDto.from(room, currentMember);
    }

    @Override
    @Transactional
    public RoomProfileCustomFieldResponseDto updateCustomField(
            Long roomId,
            Long fieldId,
            RoomProfileCustomFieldUpdateRequestDto requestDto,
            String email
    ) {
        Room room = findRoom(roomId);
        User user = findUser(email);

        validateOwner(room, user);

        RoomProfileCustomField customField = customFieldRepository.findById(fieldId)
                .orElseThrow(() -> new IllegalArgumentException("커스텀 필드를 찾을 수 없습니다."));

        validateFieldBelongsToRoom(customField, roomId);
        validateOptions(requestDto.getOptionType(), requestDto.getOptions());

        customField.update(
                requestDto.getFieldName(),
                requestDto.isRequired(),
                requestDto.getOptionType()
        );

        if (isSelectType(requestDto.getOptionType())) {
            updateOptions(customField, requestDto.getOptions());
        } else {
            customField.getOptions().clear();
        }

        return RoomProfileCustomFieldResponseDto.from(customField);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId, String email) {
        Room room = findRoom(roomId);
        User user = findUser(email);

        validateOwner(room, user);
        roomRepository.delete(room);
    }

    private RoomProfileCustomField createCustomField(
            RoomProfileCustomFieldCreateRequestDto fieldDto
    ) {
        validateOptions(fieldDto.getOptionType(), fieldDto.getOptions());

        RoomProfileCustomField customField = RoomProfileCustomField.builder()
                .fieldName(fieldDto.getFieldName())
                .required(fieldDto.isRequired())
                .optionType(fieldDto.getOptionType())
                .build();

        if (fieldDto.getOptions() != null) {
            for (RoomProfileCustomFieldOptionCreateRequestDto optionDto : fieldDto.getOptions()) {
                RoomProfileCustomFieldOption option = RoomProfileCustomFieldOption.builder()
                        .optionValue(optionDto.getOptionValue())
                        .displayOrder(optionDto.getDisplayOrder())
                        .build();

                customField.addOption(option);
            }
        }

        return customField;
    }

    private void validateOptions(OptionType optionType, List<?> options) {
        if (optionType == null) {
            throw new IllegalArgumentException("옵션 타입은 필수입니다.");
        }

        boolean hasOptions = options != null && !options.isEmpty();

        if (isSelectType(optionType) && !hasOptions) {
            throw new IllegalArgumentException(
                    "SINGLE_SELECT와 MULTI_SELECT 타입은 선택지가 최소 1개 필요합니다."
            );
        }

        if (!isSelectType(optionType) && hasOptions) {
            throw new IllegalArgumentException(
                    "TEXT, NUMBER, DATE 타입에는 선택지를 사용할 수 없습니다."
            );
        }
    }

    private boolean isSelectType(OptionType optionType) {
        return optionType == OptionType.SINGLE_SELECT
                || optionType == OptionType.MULTI_SELECT;
    }

    private void updateOptions(
            RoomProfileCustomField customField,
            List<RoomProfileCustomFieldOptionUpdateRequestDto> optionDtos
    ) {
        Set<Long> requestedOptionIds = new HashSet<>();

        for (RoomProfileCustomFieldOptionUpdateRequestDto optionDto : optionDtos) {
            if (optionDto.getId() == null) {
                RoomProfileCustomFieldOption newOption = RoomProfileCustomFieldOption.builder()
                        .optionValue(optionDto.getOptionValue())
                        .displayOrder(optionDto.getDisplayOrder())
                        .build();

                customField.addOption(newOption);
                continue;
            }

            RoomProfileCustomFieldOption option = optionRepository.findById(optionDto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("선택지를 찾을 수 없습니다."));

            validateOptionBelongsToField(option, customField.getId());
            option.update(optionDto.getOptionValue(), optionDto.getDisplayOrder());
            requestedOptionIds.add(option.getId());
        }

        customField.getOptions().removeIf(option ->
                option.getId() != null && !requestedOptionIds.contains(option.getId())
        );
    }

    private Room findRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("룸을 찾을 수 없습니다."));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void validateOwner(Room room, User user) {
        if (!room.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("룸 오너만 수행할 수 있습니다.");
        }
    }

    private void validateRoomMember(Room room, User user) {
        boolean member = memberProfileCardRepository.existsByUserIdAndRoomId(
                user.getId(),
                room.getId()
        );

        if (!member) {
            throw new IllegalStateException("룸 멤버만 상세 정보를 조회할 수 있습니다.");
        }
    }

    private void validateMaxMember(Integer maxMember, long currentMember) {
        if (maxMember == null || maxMember < 1) {
            throw new IllegalArgumentException("최대 인원은 1명 이상이어야 합니다.");
        }

        if (maxMember < currentMember) {
            throw new IllegalArgumentException("최대 인원은 현재 인원보다 작을 수 없습니다.");
        }
    }

    private void validateFieldBelongsToRoom(RoomProfileCustomField customField, Long roomId) {
        if (!customField.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("해당 룸의 커스텀 필드가 아닙니다.");
        }
    }

    private void validateOptionBelongsToField(
            RoomProfileCustomFieldOption option,
            Long fieldId
    ) {
        if (!option.getCustomField().getId().equals(fieldId)) {
            throw new IllegalArgumentException("해당 커스텀 필드의 선택지가 아닙니다.");
        }
    }

    private String generateRoomCode() {
        for (int attempt = 0; attempt < ROOM_CODE_MAX_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder(ROOM_CODE_LENGTH);

            for (int i = 0; i < ROOM_CODE_LENGTH; i++) {
                int index = secureRandom.nextInt(ROOM_CODE_CHARACTERS.length());
                code.append(ROOM_CODE_CHARACTERS.charAt(index));
            }

            String roomCode = code.toString();
            if (!roomRepository.existsByRoomCode(roomCode)) {
                return roomCode;
            }
        }

        throw new IllegalStateException("룸 코드를 생성할 수 없습니다.");
    }
}
