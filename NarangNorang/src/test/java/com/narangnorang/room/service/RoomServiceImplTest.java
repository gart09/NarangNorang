package com.narangnorang.room.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.narangnorang.memberprofilecard.repository.MemberProfileCardRepository;
import com.narangnorang.memberprofilecard.repository.MemberProfileCustomAnswerRepository;
import com.narangnorang.room.repository.RoomProfileCustomFieldRepository;
import com.narangnorang.room.repository.RoomRepository;
import com.narangnorang.space.repository.SpaceRepository;
import com.narangnorang.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomProfileCustomFieldRepository customFieldRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberProfileCardRepository memberProfileCardRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private MemberProfileCustomAnswerRepository memberProfileCustomAnswerRepository;

    @InjectMocks
    private RoomServiceImpl roomService;
}
