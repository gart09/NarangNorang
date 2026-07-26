# ChatService 테스트 분기 목록

## 1. saveChat (채팅 저장)
1. **성공 (Room)**: `targetType`이 "room"이고 사용자가 해당 방에 접근 권한이 있는 경우
2. **실패 (Room 권한 없음)**: `targetType`이 "room"이지만 사용자가 해당 방에 접근 권한이 없는 경우 (`ChatException` - `USER_NOT_PERMITTED`)
3. **성공 (Space)**: `targetType`이 "space"이고 사용자가 해당 스페이스에 접근 권한이 있는 경우
4. **실패 (Space 권한 없음)**: `targetType`이 "space"이지만 사용자가 해당 스페이스에 접근 권한이 없는 경우 (`ChatException` - `USER_NOT_PERMITTED`)
5. **실패 (잘못된 targetType)**: `targetType`이 "room"이나 "space"가 아닌 경우 (`ChatException` - `INVALID_INPUT_TYPE`)

## 2. getChatHistory (채팅 내역 조회)
1. **성공 (Room)**: `targetType`이 "room"일 때 채팅 내역 조회에 성공한 경우
2. **성공 (Space)**: `targetType`이 "space"일 때 채팅 내역 조회에 성공한 경우
3. **실패 (잘못된 targetType)**: `targetType`이 "room"이나 "space"가 아닌 경우 (`ChatException` - `INVALID_INPUT_TYPE`)
