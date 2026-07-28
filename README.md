# NarangNorang (나랑노랑)

NarangNorang(나랑노랑)은 대규모 커뮤니티(Room)와 그 안의 소규모 모임/프로젝트(Space)를 중심으로 사람들을 연결하고 소통하게 해주며, 원하는 조건의 Space Member(팀원) 혹은 Space(팀)을 찾을 수 있도록 도와주는 실시간 웹 애플리케이션입니다. 

---

## 🏗 프로젝트 주요 개념 및 구조 (Architecture)

이 프로젝트는 **Spring Boot** 기반의 백엔드와 **Vanilla JS(단일 SPA 형태)** 기반의 프론트엔드로 구성되어 있습니다. 

### 핵심 도메인 (Core Domains)

#### 1. User & Auth (사용자 및 인증)
- **`user`**: 시스템의 전역 사용자 엔티티입니다. 기본 인적 사항과 로그인 정보를 담당합니다.
- **`auth` / `jwt`**: 로그인, 회원가입, JWT 토큰 발급 및 검증을 담당합니다. API 요청 시 헤더의 JWT를 파싱하여 사용자 컨텍스트를 유지합니다.

#### 2. Room (대규모 커뮤니티)
- **`room`**: 디스코드의 '서버'나 슬랙의 '워크스페이스'에 해당하는 가장 큰 단위의 커뮤니티입니다.
- 사용자가 특정 Room에 가입하면 **Member**가 되며, 해당 Room에 특화된 프로필 카드(`MemberProfileCard`)를 작성하게 됩니다.
- Room마다 오너가 존재하며, 가입 시 입력받을 '커스텀 필드(동적 입력항목)'를 자유롭게 정의할 수 있습니다.

#### 3. Space (소규모 모임/채널)
- **`space`**: Room 내부에 존재하는 하위 그룹입니다. 스터디, 프로젝트 팀, 혹은 특정 주제의 채널로 활용됩니다.
- Space 역시 멤버와 전용 프로필 카드(`SpaceProfileCard`)를 갖습니다. 멤버는 `space_member` 테이블에 `space`와 `MemberProfileCard`가 `N:N` 관계로 기록됩니다.
- 스페이스 오너는 멤버의 최대 인원을 제한할 수 있고, 커스텀 필드를 자유롭게 추가하여 멤버들의 상태나 역할(예: 필요 기술, 선호 시간 등)을 관리할 수 있습니다.

#### 4. MemberProfileCard (프로필 카드 시스템)
- **`memberprofilecard`**: 전역 User 정보와 별개로, **Room이나 Space마다 사용자가 각기 다른 프로필를 가질 수 있도록** 지원하는 도메인입니다.
- 오너가 설정한 동적 커스텀 필드(단일 선택, 다중 선택, 텍스트, 숫자 등)에 대한 사용자들의 답변 내역이 저장됩니다.

#### 5. InviteSpace (초대 및 신청)
- **`invitespace`**: Space 오너가 Room의 다른 멤버를 스페이스로 초대하거나, 일반 멤버가 특정 Space에 가입 신청을 하는 양방향 워크플로우를 처리합니다.
- PENDING(대기), ACCEPTED(수락), REJECTED(거절) 상태를 가집니다.

#### 6. Chat (실시간 채팅)
- **`chat`**: Spring WebSocket과 STOMP 프로토콜을 활용하여 실시간 양방향 통신을 구현합니다.
- **Room 채팅**과 **Space 채팅**이 모두 지원되며, 채팅 내역은 DB에 영구적으로 저장(`ChatHistory`)되어 언제든 이전 대화 내역을 불러올 수 있습니다.

---

## 🔗 도메인 간의 관계 (Domain Relationships)

백엔드 엔티티들은 다음과 같이 유기적으로 연결되어 있습니다.

1. **User ↔ Room**: 사용자는 여러 Room에 가입할 수 있으며, 가입 시 `MemberProfileCard` 엔티티가 중간 매핑 역할을 합니다.
2. **Room ↔ Space**: 하나의 Room은 여러 개의 Space를 가질 수 있습니다. (1:N)
3. **MemberProfileCard ↔ Space**: Room의 멤버들만이 해당 Room에 속한 Space에 가입하거나 초대받을 수 있습니다.
4. **Room ↔ RoomCustomField**: Room(또는 Space)은 가입 시 필요한 동적 입력 항목(CustomField)을 1:N으로 소유합니다.
5. **User ↔ MemberProfileCard ↔ MemberProfileCustomAnswer**: `User`는 각각의 `Room`에 해당하는 `MemberProfileCard`를 가지며, `MemberProfileCard`에는 여러 개의 `MemberProfileCustomAnswer`이 연결됩니다.

---

## 💻 기술 스택

### Backend
- **Java 21** / **Spring Boot 4.1**
- **Spring Data JPA** (Hibernate) + **Querydsl**
- **Spring WebSocket / STOMP** (실시간 통신)
- **JWT (JSON Web Token)** (인증/인가)

### Frontend
- **HTML5 / CSS3 / Vanilla JavaScript**
- SPA(Single Page Application) 구조를 `narangnorang.html` 단일 파일 내에서 렌더링 함수와 상태(`state`) 객체를 통해 자체적으로 구현
- **SockJS / Stomp.js** (웹소켓 클라이언트)
