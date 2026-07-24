# 도메인별 예외처리 가이드라인

본 프로젝트는 전역 예외 처리(Global Exception Handling)와 함께 **각 도메인별로 독립적인 예외 처리 구조**를 가집니다. 이를 통해 도메인의 응집도를 높이고, 추후 MSA(Microservices Architecture)로의 전환을 대비하며, 다수 개발자 간의 병합 충돌을 최소화할 수 있습니다.

이 문서는 `Chat` 도메인의 코드를 예시로 들어, 새로운 도메인 생성 시 예외 처리 코드를 어떻게 작성해야 하는지 설명합니다.

---

## 예외 처리 구조 요약
새로운 도메인을 개발할 때 예외처리를 위해 다음 세 가지 클래스를 생성해야 합니다.
예를 들어 도메인 이름이 `User`라면, `UserErrorCode`, `UserException`, `UserExceptionHandler`를 만듭니다.

1. **`{Domain}ErrorCode`**: 도메인에서 발생하는 에러 코드 정의 (Enum)
2. **`{Domain}Exception`**: 도메인 전용 비즈니스 예외 클래스
3. **`{Domain}ExceptionHandler`**: 도메인 패키지 내에서 발생하는 예외를 캐치하는 핸들러

---

## 도메인 예외처리 작성 단계 (Chat 도메인 예시)

새로운 도메인 패키지 하위에 `exception` 패키지를 만들고 아래 클래스들을 작성합니다. (예: `com.narangnorang.chat.exception`)

### Step 1. ErrorCode Enum 구현 (`ChatErrorCode.java`)
공통 에러 코드 인터페이스인 `ErrorCode`를 구현하는 Enum 클래스를 작성합니다. 이 곳에 도메인에서 발생할 수 있는 모든 에러를 정의합니다.

```java
package com.narangnorang.chat.exception;

import com.narangnorang.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    // 에러 상태코드, 도메인 고유 에러코드, 에러 메시지 순으로 정의합니다.
    USER_NOT_PERMITTED(HttpStatus.FORBIDDEN, "CHAT-001", "해당 유저는 접근 권한이 없습니다. %s"),
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "CHAT-002", "Member를 찾을 수 없습니다. (송신자ID: %s)"),
    DB_ERROR_OCCURRED(HttpStatus.INTERNAL_SERVER_ERROR,"CHAT-003","DB 에러가 발생했습니다. 콘솔 로그를 참고하세요."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "CHAT-004", "유효하지 않은 토큰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```
> [!TIP]
> 메시지 내에 `%s`나 `%d` 같은 포맷 문자열을 사용하면, 예외 발생 시 동적으로 값을 주입하여 더 구체적인 에러 메시지를 응답할 수 있습니다.

### Step 2. Domain Exception 구현 (`ChatException.java`)
공통 비즈니스 예외인 `BusinessException`을 상속받는 도메인 전용 예외 클래스를 생성합니다.

```java
package com.narangnorang.chat.exception;

import com.narangnorang.common.exception.BusinessException;
import com.narangnorang.common.exception.errorcode.ErrorCode;

public class ChatException extends BusinessException {

    // 기본 예외 생성자
    public ChatException(ErrorCode errorCode){
        super(errorCode);
    }

    // 동적 메시지 매개변수(Object... args)를 받기 위한 생성자
    public ChatException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
```

### Step 3. Domain Exception Handler 구현 (`ChatExceptionHandler.java`)
해당 도메인의 컨트롤러에서 발생하는 예외를 전담해서 처리할 핸들러를 작성합니다. `BaseExceptionHandler`를 상속받습니다.

```java
package com.narangnorang.chat.exception;

import com.narangnorang.common.exception.BaseExceptionHandler;
import com.narangnorang.common.exception.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// basePackages 속성에 해당 도메인의 기본 패키지 경로를 반드시 명시해야 합니다.
@RestControllerAdvice(basePackages = "com.narangnorang.chat")
public class ChatExceptionHandler extends BaseExceptionHandler {

    // 1. 도메인 전용 비즈니스 예외 처리
    @ExceptionHandler(ChatException.class)
    protected ResponseEntity<ErrorResponseDto> handleChatException(ChatException e) {
        log.warn("ChatException Occurred : {}", e.getMessage());
        return makeErrorResponse(e);
    }

    // 2. 도메인 내 특정 Exception(예: DB 관련)에 대한 처리 추가 가능
    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<ErrorResponseDto> handleDataAccessException(DataAccessException e) {
        log.error("채팅 도메인 DB 오류 발생 : ", e);
        return makeErrorResponse(ChatErrorCode.DB_ERROR_OCCURRED);
    }
}
```
> [!IMPORTANT]
> `@RestControllerAdvice`의 `basePackages`를 설정하지 않으면 모든 도메인의 예외를 가로채게 되므로, **반드시 해당 도메인의 패키지를 명시**해 주어야 합니다.

---

## 실제 서비스 레이어에서의 사용 예시

위에서 정의한 예외 구조를 활용하여 도메인 비즈니스 로직 처리 중 문제가 생겼을 때 바로 예외를 던지면 됩니다.

```java
@Service
@RequiredArgsConstructor
public class ChatService {

    public void checkUserPermission(Long userId) {
        boolean hasPermission = false; // 권한 확인 로직
        
        if (!hasPermission) {
            // 인자가 없는 에러 던지기
            // throw new ChatException(ChatErrorCode.INVALID_TOKEN);
            
            // 포맷 문자열(%s)에 값을 주입하며 에러 던지기
            throw new ChatException(ChatErrorCode.USER_NOT_PERMITTED, userId);
        }
    }
}
```

이렇게 에러를 발생시키면 `ChatExceptionHandler`가 자동으로 캐치하여 공통 응답 규격인 `ErrorResponseDto` 형태로 클라이언트에게 반환합니다.
