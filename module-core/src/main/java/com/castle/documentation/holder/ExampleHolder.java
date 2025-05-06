package com.castle.documentation.holder;

import io.swagger.v3.oas.models.examples.Example;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExampleHolder {
    private int code;
    private String name;
    private Example holder;
}
