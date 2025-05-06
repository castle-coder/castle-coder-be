package com.castle.documentation.annotation;

import com.castle.code.error.BaseErrorCode;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiErrorCodeExamples.class)
public @interface ApiErrorCodeExample {
    Class<? extends BaseErrorCode> errorClass();
    String field();
}