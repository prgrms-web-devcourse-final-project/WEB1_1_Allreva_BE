package com.backend.allreva.rent.exception;

import com.backend.allreva.common.exception.code.ErrorCode;
import com.backend.allreva.common.exception.code.ErrorCodeInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RentErrorCode implements ErrorCodeInterface {
    RENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "RENT_NOT_FOUND", "존재하지 않는 차 대절 폼입니다."),
    RENT_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "RENT_ACCESS_DENIED", "차 대절 폼에 접근할 수 없습니다."),
    RENT_JOIN_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "RENT_JOIN_NOT_FOUND", "존재하지 않는 차 대절 참석 폼입니다."),
    RENT_JOIN_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "RENT_JOIN_ACCESS_DENIED", "차 대절 참석 폼에 접근할 수 없습니다.")
    ;

    private final Integer status;
    private final String errorCode;
    private final String message;

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.of(status, errorCode, message);
    }
}
