package com.narangnorang.room.service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.memberprofilecard.repository.MemberProfileCustomAnswerRepository;
import com.narangnorang.room.dto.request.RoomCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldBulkUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldOptionCreateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldOptionUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldsUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomProfileCustomFieldUpdateRequestDto;
import com.narangnorang.room.dto.request.RoomUpdateRequestDto;
import com.narangnorang.room.dto.response.RoomJoinResponseDto;
import com.narangnorang.room.dto.response.RoomProfileCustomFieldResponseDto;
import com.narangnorang.room.dto.response.RoomResponseDto;
import com.narangnorang.room.entity.OptionType;
import com.narangnorang.room.entity.Room;
import com.narangnorang.room.entity.RoomProfileCustomField;
import com.narangnorang.room.entity.RoomProfileCustomFieldOption;
import com.narangnorang.room.repository.RoomProfileCustomFieldOptionRepository;
import com.narangnorang.room.repository.RoomProfileCustomFieldRepository;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.space.repository.SpaceRepository;
import com.narangnorang.user.entity.User;
import com.narangnorang.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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
    private final SpaceRepository spaceRepository;
    private final MemberProfileCustomAnswerRepository memberProfileCustomAnswerRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public RoomResponseDto createRoom(RoomCreateRequestDto requestDto, Long userId) {
        User owner = findUser(userId);
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
                room.addCustomField(buildCustomField(fieldDto));
            }
        }

        Room savedRoom = roomRepository.save(room);
        long currentMember = memberProfileCardRepository.countByRoomId(savedRoom.getId());
        return RoomResponseDto.from(savedRoom, currentMember);
    }

    @Override
    public RoomResponseDto getRoom(Long roomId, Long userId) {
        Room room = findRoom(roomId);
        User user = findUser(userId);
        validateRoomMember(room, user);

        long currentMember = memberProfileCardRepository.countByRoomId(room.getId());
        return RoomResponseDto.from(room, currentMember);
    }

    @Override
    public RoomJoinResponseDto getRoomForJoin(String roomCode, Long userId) {
        User user = findUser(userId);
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("룸 코드를 확인해 주세요."));

        boolean member = memberProfileCardRepository.existsByUserIdAndRoomId(
                user.getId(),
                room.getId()
        );
        long currentMember = memberProfileCardRepository.countByRoomId(room.getId());

        return RoomJoinResponseDto.from(room, currentMember, member);
    }

    @Override
    @Transactional
    public RoomResponseDto updateRoom(
            Long roomId,
            RoomUpdateRequestDto requestDto,
            Long userId
    ) {
        Room room = findRoom(roomId);
        User user = findUser(userId);

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
            Long userId
    ) {
        Room room = findRoom(roomId);
        User user = findUser(userId);

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

        customFieldRepository.flush();

        return RoomProfileCustomFieldResponseDto.from(customField);
    }

    @Override
    @Transactional
    public RoomProfileCustomFieldResponseDto createCustomField(
            Long roomId,
            RoomProfileCustomFieldCreateRequestDto requestDto,
            Long userId
    ) {
        Room room = findRoom(roomId);
        User user = findUser(userId);

        validateOwner(room, user);

        RoomProfileCustomField customField = buildCustomField(requestDto);
        room.addCustomField(customField);
        customFieldRepository.save(customField);

        return RoomProfileCustomFieldResponseDto.from(customField);
    }

    @Override
    @Transactional
    public List<RoomProfileCustomFieldResponseDto> updateCustomFields(
            Long roomId,
            RoomProfileCustomFieldsUpdateRequestDto requestDto,
            Long userId
    ) {
        Room room = findRoom(roomId);
        User user = findUser(userId);

        validateOwner(room, user);

        if (requestDto.getCustomFields() == null) {
            throw new IllegalArgumentException("커스텀 필드 목록은 필수입니다.");
        }

        Set<Long> requestedFieldIds = new HashSet<>();

        for (RoomProfileCustomFieldBulkUpdateRequestDto fieldDto : requestDto.getCustomFields()) {
            validateOptions(fieldDto.getOptionType(), fieldDto.getOptions());

            if (fieldDto.getId() == null) {
                room.addCustomField(buildCustomField(fieldDto));
                continue;
            }

            if (!requestedFieldIds.add(fieldDto.getId())) {
                throw new IllegalArgumentException("중복된 커스텀 필드 ID가 존재합니다.");
            }

            RoomProfileCustomField customField = customFieldRepository.findById(fieldDto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("커스텀 필드를 찾을 수 없습니다."));

            validateFieldBelongsToRoom(customField, roomId);

            customField.update(
                    fieldDto.getFieldName(),
                    fieldDto.isRequired(),
                    fieldDto.getOptionType()
            );

            if (isSelectType(fieldDto.getOptionType())) {
                updateOptions(customField, fieldDto.getOptions());
            } else {
                customField.getOptions().clear();
            }
        }

        List<RoomProfileCustomField> fieldsToDelete = room.getCustomFields().stream()
                .filter(field -> field.getId() != null)
                .filter(field -> !requestedFieldIds.contains(field.getId()))
                .toList();

        for (RoomProfileCustomField field : fieldsToDelete) {
            memberProfileCustomAnswerRepository.deleteByRoomProfileCustomFieldId(field.getId());
            room.removeCustomField(field);
        }

        customFieldRepository.flush();

        return room.getCustomFields().stream()
                .map(RoomProfileCustomFieldResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCustomField(Long roomId, Long fieldId, Long userId) {
        Room room = findRoom(roomId);
        User user = findUser(userId);

        validateOwner(room, user);

        RoomProfileCustomField customField = customFieldRepository.findById(fieldId)
                .orElseThrow(() -> new IllegalArgumentException("커스텀 필드를 찾을 수 없습니다."));

        validateFieldBelongsToRoom(customField, roomId);

        memberProfileCustomAnswerRepository.deleteByRoomProfileCustomFieldId(fieldId);
        room.removeCustomField(customField);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId, Long userId) {
        Room room = findRoom(roomId);
        User user = findUser(userId);

        validateOwner(room, user);
        
        spaceRepository.deleteAll(
                spaceRepository.findByRoomId(roomId)
        );
        spaceRepository.flush();
        
        roomRepository.delete(room);
    }

    private RoomProfileCustomField buildCustomField(
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

    private RoomProfileCustomField buildCustomField(
            RoomProfileCustomFieldBulkUpdateRequestDto fieldDto
    ) {
        validateOptions(fieldDto.getOptionType(), fieldDto.getOptions());

        RoomProfileCustomField customField = RoomProfileCustomField.builder()
                .fieldName(fieldDto.getFieldName())
                .required(fieldDto.isRequired())
                .optionType(fieldDto.getOptionType())
                .build();

        if (fieldDto.getOptions() != null) {
            for (RoomProfileCustomFieldOptionUpdateRequestDto optionDto : fieldDto.getOptions()) {
                if (optionDto.getId() != null) {
                    throw new IllegalArgumentException(
                            "새 커스텀 필드의 선택지에는 ID를 지정할 수 없습니다."
                    );
                }

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

    private User findUser(Long userId) {
        return userRepository.findById(userId)
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
