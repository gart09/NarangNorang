# MemberProfileCardService 테스트 분기 목록

## 1. createMemberProfileCard (프로필 카드 생성)
1. **성공**: 정상적인 요청으로 프로필 카드 생성에 성공한 경우
2. **실패 (유저 없음)**: 요청한 userId에 해당하는 유저가 존재하지 않는 경우 (`MemberProfileCardException` - `USER_NOT_FOUND`)
3. **실패 (방 없음)**: 요청한 roomCode에 해당하는 방이 존재하지 않는 경우 (`MemberProfileCardException` - `ROOM_NOT_FOUND`)
4. **실패 (이미 존재함)**: 해당 유저가 해당 방에 이미 프로필 카드를 생성한 경우 (`MemberProfileCardException` - `MEMBER_PROFILE_CARD_DUPLICATED`)
5. **실패 (필수값 누락)**: 방에 설정된 필수 입력 항목을 누락한 경우 (`MemberProfileCardException` - `REQUIRED_FIELD_MISS`)
6. **실패 (필수값 빈칸)**: 필수 입력 항목에 빈 칸이나 공백을 입력한 경우 (`MemberProfileCardException` - `REQUIRED_FIELD_SPACE`)
7. **실패 (선택값 불일치)**: 단일선택/다중선택 항목에 존재하지 않는 옵션 값을 입력한 경우 (`MemberProfileCardException` - `CANT_SELECT_FIELD`)

## 2. findMemberProfileCard (프로필 카드 조회)
1. **성공**: 유저가 해당 방에 속해있어 정상적으로 프로필 카드 목록을 조회한 경우
2. **실패 (권한 없음)**: 유저가 해당 방에 속해있지 않은데 조회를 시도하는 경우 (`MemberProfileCardException` - `USER_NOT_PERMITTED`)

## 3. updateMemberProfileCard (프로필 카드 수정)
1. **성공**: 정상적인 요청으로 프로필 카드 수정에 성공한 경우
2. **실패 (카드 없음)**: 수정하려는 프로필 카드가 존재하지 않는 경우 (`MemberProfileCardException` - `MEMBER_PROFILE_CARD_NOT_FOUND`)
3. **실패 (수정 권한 없음)**: 본인의 프로필 카드가 아닌 다른 카드를 수정하려고 시도하는 경우 (`MemberProfileCardException` - `USER_NOT_PERMITTED`)
4. **실패 (필수값 누락)**: 필수 입력 항목을 누락하여 수정하려는 경우 (`MemberProfileCardException` - `REQUIRED_FIELD_MISS`)
5. **실패 (필수값 빈칸)**: 필수 항목을 빈 칸이나 공백으로 수정하려는 경우 (`MemberProfileCardException` - `REQUIRED_FIELD_SPACE`)
6. **실패 (선택값 불일치)**: 단일/다중선택 항목에 잘못된 옵션을 선택한 경우 (`MemberProfileCardException` - `CANT_SELECT_FIELD`)

## 4. deleteMemberProfileCard (프로필 카드 삭제)
1. **성공**: 정상적인 요청으로 본인의 프로필 카드를 삭제한 경우
2. **실패 (카드 없음)**: 삭제하려는 프로필 카드가 존재하지 않는 경우 (`MemberProfileCardException` - `MEMBER_PROFILE_CARD_NOT_FOUND`)
3. **실패 (삭제 권한 없음)**: 본인의 프로필 카드가 아닌 다른 카드를 삭제하려고 시도하는 경우 (`MemberProfileCardException` - `USER_NOT_PERMITTED`)

## 5. getRoomsList (속한 방 목록 조회)
1. **성공**: 유저가 속한 방 목록과 각 방의 인원수를 정상적으로 조회한 경우
2. **실패 (유저 없음)**: 요청한 userId에 해당하는 유저가 존재하지 않는 경우 (`MemberProfileCardException` - `USER_NOT_FOUND`)
