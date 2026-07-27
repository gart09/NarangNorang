# NarangNorang API 명세서

- Base URL: `http://localhost:8080`
- 인증: `Authorization: Bearer {accessToken}` 헤더 (별도 표기 없으면 인증 필요)
- 공통 응답 포맷: `ApiResponse<T>` — `{ "success": boolean, "result": T }` (실패 시 `{ "success": false, "message": "..." }`)

---

## 1. Auth (인증)

### 1.1 로그인
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /users/login` |
| 인증 | 불필요 |
| Request Body | `{ "email": "user@example.com", "password": "password123!" }` |
| Response | `{ "token": "...", "refreshToken": "..." }` |
| 설명 | 이메일/비밀번호로 로그인, 액세스/리프레시 토큰 발급 |

### 1.2 리프레시 토큰 검증 및 재발급
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /users/checkRefreshToken` |
| 인증 | 불필요 |
| Request Param | `refreshToken` (String) |
| Response | `{ "token": "...", "refreshToken": "..." }` |
| 설명 | 만료된 액세스 토큰을 리프레시 토큰으로 갱신 |

### 1.3 로그아웃
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /users/logout` |
| 인증 | 필요 |
| Response | `Void` |
| 설명 | 현재 로그인한 유저 로그아웃 처리 |

---

## 2. User (회원)

### 2.1 회원가입
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /users/register` |
| 인증 | 불필요 |
| Request Body | `{ "name": "현빈", "email": "user@example.com", "password": "password123!" }` |
| Response | `UserResponseDto { id, name, email, userRoles }` |

### 2.2 내 정보 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /users/details` |
| 인증 | 필요 |
| Response | `UserResponseDto` |

### 2.3 내 정보 수정
| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /users` |
| 인증 | 필요 |
| Request Body | `{ "name": "새이름" 또는 null, "email": "본인이메일(필수)", "password": "새비밀번호" 또는 null }` |
| Response | `UserResponseDto` |
| 설명 | `name`/`password`가 null이면 기존값 유지. `email`은 본인 확인용이라 반드시 일치해야 함 |

### 2.4 회원 탈퇴
| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /users` |
| 인증 | 필요 |
| Request Param | `email` (본인 확인용) |
| Response | `Void` |

---

## 3. Room (룸)

### 3.1 룸 생성
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /rooms` |
| 인증 | 필요 |
| Request Body | `{ "name", "description", "maxMember", "customFields": [{ "fieldName", "required", "optionType", "options": [{ "optionValue", "displayOrder" }] }] }` |
| Response | `RoomResponseDto { id, name, description, maxMember, currentMember, roomCode, createdAt, ownerId, ownerName, customFields }` |

### 3.2 룸 상세 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /rooms/{roomId}` |
| 인증 | 필요 (룸 멤버) |
| Response | `RoomResponseDto` |

### 3.3 초대코드로 룸 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /rooms/join/{roomCode}` |
| 인증 | 필요 |
| Response | `RoomJoinResponseDto { member: boolean, room: RoomResponseDto }` |
| 설명 | `member`는 요청자의 현재 참여 여부 |

### 3.4 룸 정보 수정
| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /rooms/{roomId}` |
| 인증 | 필요 (룸 오너) |
| Request Body | `{ "name", "description", "maxMember" }` |
| Response | `RoomResponseDto` |

### 3.5 룸 커스텀 필드 일괄 수정
| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /rooms/{roomId}/customFields` |
| 인증 | 필요 (룸 오너) |
| Request Body | `{ "customFields": [{ "id"(nullable), "fieldName", "required", "optionType", "options": [{ "id"(nullable), "optionValue", "displayOrder" }] }] }` |
| Response | `List<RoomProfileCustomFieldResponseDto>` |
| 설명 | `id` 없으면 신규 생성. 기존 필드는 이름/타입/옵션 수정 불가(삭제 후 재생성만 가능) |

### 3.6 룸 삭제
| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /rooms/{roomId}` |
| 인증 | 필요 (룸 오너) |
| Response | `Void` |

---

## 4. MemberProfileCard (멤버 프로필 카드)

### 4.1 프로필 카드 생성
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /memberProfileCard/create` |
| 인증 | 필요 |
| Request Body | `{ "name", "roomCode", "answers": { "필드ID": "답변값" } }` |
| Response | `MemberProfileCardCreateResponseDto { id, name, createdAt, updatedAt, userId, roomId, answers }` |
| 설명 | 룸 코드로 룸에 참여하며 프로필 카드 동시 생성 |

### 4.2 프로필 카드 조건 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /memberProfileCard/filter` |
| 인증 | 필요 |
| Request Body | `{ "name", "memberProfileCardId", "roomId", "customFields": { "필드ID": ["매칭값1", ...] } }` (전부 선택) |
| Response | `List<MemberProfileCardReadResponseDto>` |

### 4.3 프로필 카드 수정
| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /memberProfileCard/update` |
| 인증 | 필요 (본인) |
| Request Body | `{ "memberProfileCardId", "name", "answers": { "필드ID": "답변값" } }` |
| Response | `MemberProfileCardUpdateResponseDto` |

### 4.4 프로필 카드 삭제
| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /memberProfileCard/{memberProfileCardId}` |
| 인증 | 필요 (본인) |
| Response | `Void` |

### 4.5 내 룸 목록 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /memberProfileCard/roomsList` |
| 인증 | 필요 |
| Response | `RoomsListResponseDto { rooms: [RoomResponseDto, ...] }` |

---

## 5. Space (스페이스)

기본 경로: `/rooms/{roomId}`

### 5.1 스페이스 목록 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /rooms/{roomId}/spaces` |
| 인증 | 필요 (룸 멤버) |
| Query Param | `tags` (선택, 목록 — 모두 포함하는 스페이스만 매칭) |
| Response | `List<SpaceSummaryResponseDto> { id, name, currentMemberCount, maxMemberCount, tags }` |

### 5.2 스페이스 상세 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /rooms/{roomId}/spaces/{spaceId}` |
| 인증 | 필요 (룸 멤버) |
| Response | `SpaceProfileCardResponseDto { name, owner, ownerId, techStack, preferredStartTime, preferredEndTime, customField, maxMemberCount, currentMemberCount, tags }` |

### 5.3 스페이스 생성
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /rooms/{roomId}/spaces` |
| 인증 | 필요 (룸 멤버) |
| Request Body | `{ "name", "maxMemberCount", "tags": [...], "techStack": [...], "preferredStartTime", "preferredEndTime", "customField": {...} }` |
| Response | `SpaceProfileCardResponseDto` |
| 설명 | 생성자가 오너 및 첫 멤버로 등록됨 |

### 5.4 스페이스 수정
| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /rooms/{roomId}/spaces/{spaceId}` |
| 인증 | 필요 (스페이스 오너 또는 룸 오너) |
| Request Body | `{ "name", "maxMemberCount", "techStack", "preferredStartTime", "preferredEndTime", "customField", "tags" }` |
| Response | `SpaceProfileCardResponseDto` |
| 설명 | `tags`가 `null`이면 기존 태그 유지, 값이 있으면 전체 재등록 |

### 5.5 스페이스 삭제
| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /rooms/{roomId}/spaces/{spaceId}` |
| 인증 | 필요 (스페이스 오너 또는 룸 오너) |
| Response | `Void` |

### 5.6 룸 내 태그 목록 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /rooms/{roomId}/tags` |
| 인증 | 필요 |
| Response | `List<String>` |

### 5.7 스페이스 탈퇴
| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /rooms/{roomId}/spaces/{spaceId}/leave` |
| 인증 | 필요 (스페이스 멤버) |
| Response | `Void` |
| 설명 | 오너는 탈퇴 불가 (먼저 오너 위임 필요) |

### 5.8 스페이스 오너 위임
| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /rooms/{roomId}/spaces/{spaceId}/owner` |
| 인증 | 필요 (스페이스 오너 또는 룸 오너) |
| Request Body | `{ "newOwnerId": 2 }` |
| Response | `Void` |
| 설명 | 새 오너는 해당 스페이스 멤버여야 함 |

### 5.9 스페이스 멤버 목록 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /rooms/{roomId}/spaces/{spaceId}/spaceMembers` |
| 인증 | 필요 (룸 멤버, 비가입자도 조회 가능) |
| Response | `List<MemberProfileCardReadResponseDto>` |

---

## 6. InviteSpace (스페이스 가입 신청/권유)

### 6.1 가입 신청
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /invites/apply` |
| 인증 | 필요 (룸 멤버) |
| Request Body | `{ "spaceId": 1 }` |
| Response | `Void` |

### 6.2 가입 권유
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /invites/invite` |
| 인증 | 필요 (스페이스 오너) |
| Request Body | `{ "spaceId": 1, "targetUserId": 2 }` |
| Response | `Void` |

### 6.3 스페이스 대기 목록 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /invites/spaces/{spaceId}` |
| 인증 | 필요 (스페이스 오너) |
| Response | `List<InviteSpaceResponseDto>` |

### 6.4 내가 받은 요청 목록 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /invites/members` |
| 인증 | 필요 |
| Response | `List<InviteSpaceResponseDto> { id, memberId, ownerId, spaceName, memberName, type, status, createdAt }` |
| 설명 | 본인이 승인 권한자인 모든 신청/권유(여러 스페이스 통합) |

### 6.5 신청/권유 수락
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /invites/{inviteId}/accept` |
| 인증 | 필요 (승인 권한자) |
| Response | `Void` |

### 6.6 신청/권유 거절
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /invites/{inviteId}/reject` |
| 인증 | 필요 (승인 권한자) |
| Response | `Void` |

---

## 7. Chat (채팅)

### 7.1 메시지 전송
| 항목 | 내용 |
|---|---|
| Method / URL | `POST /chat/send` |
| 인증 | 필요 |
| Request Body | `{ "targetType": "room" 또는 "space", "targetId": 1, "content": "안녕하세요" }` |
| Response | `ChatResponseDto { id, targetType, targetId, senderId, senderName, content, createdAt }` |
| 설명 | 저장 후 `/sub/{targetType}/{targetId}`로 WebSocket 브로드캐스트 |

### 7.2 채팅 이력 조회
| 항목 | 내용 |
|---|---|
| Method / URL | `GET /chat/{targetType}/{targetId}` |
| 인증 | 필요 (대상 접근 권한 보유자) |
| Query Param | `page`, `size` (기본 50) — Pageable |
| Response | `ChatHistoryResponseDto { chats: [ChatResponseDto...], hasNext, pageNumber, pageSize }` |

### 7.3 WebSocket 연결

| 항목 | 내용 |
|---|---|
| 연결 경로 | `/ws` (SockJS) |
| 구독 경로 | `/sub/room/{roomId}`, `/sub/space/{spaceId}` |
| 발신 방식 | REST `/chat/send` 호출 → 서버가 구독자에게 브로드캐스트 (클라이언트가 직접 STOMP publish하지 않음) |
