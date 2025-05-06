package com.castle.code.success;

import lombok.Getter;

/**
 * 전역적으로 공통적으로 사용되는 코드를 관리하는 enum 입니다.
 */
@Getter
public enum GlobalSuccessCode {

    SUCCESS(200, "정상 처리되었습니다.", null),
    CREATE(201, "정상적으로 생성되었습니다.", null);

    private final int code;
    private final String message;
    private final String data;

    GlobalSuccessCode(int code, String message, String data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}