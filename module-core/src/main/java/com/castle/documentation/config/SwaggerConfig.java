package com.castle.documentation.config;

import com.castle.code.error.BaseErrorCode;
import com.castle.documentation.annotation.ApiErrorCodeExample;
import com.castle.documentation.annotation.ApiErrorCodeExamples;
import com.castle.documentation.holder.ExampleHolder;
import com.castle.documentation.response.ErrorExampleResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        Info info = new Info()
                .title("Castle Coder Project API")
                .version("1.0")
                .description("Castle Coder Project API documentation");

        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Access Token");

        return new OpenAPI()
                .info(info)
                .components(new Components().addSecuritySchemes("Access Token", accessTokenScheme))
                .addSecurityItem(securityRequirement);
    }


    // OperationCustomizer를 통해 @ApiErrorCodeExample 어노테이션을 처리
    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // @ApiErrorCodeExamples가 있는 경우 처리
            ApiErrorCodeExamples apiErrorCodeExamples = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);
            if (apiErrorCodeExamples != null) {
                generateErrorCodeResponseExample(operation, apiErrorCodeExamples.value());
            } else {
                // @ApiErrorCodeExample이 있는 경우 처리
                ApiErrorCodeExample apiErrorCodeExample = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
                if (apiErrorCodeExample != null) {
                    generateErrorCodeResponseExample(operation, apiErrorCodeExample);
                }
            }
            return operation;
        };
    }

    // 여러 개의 에러 코드 예시 처리
    private void generateErrorCodeResponseExample(Operation operation, ApiErrorCodeExample[] examples) {
        List<BaseErrorCode> errorCodes = Arrays.stream(examples)
                .map(this::resolveErrorCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Integer, List<ExampleHolder>> grouped = errorCodes.stream()
                .map(code -> ExampleHolder.builder()
                        .code(code.getStatus().value())
                        .name(code.toString())  // 또는 code.name() if enum
                        .holder(getSwaggerExample(code))
                        .build())
                .collect(Collectors.groupingBy(ExampleHolder::getCode));

        addExamplesToResponses(operation.getResponses(), grouped);
    }

    // 단일 에러 코드 예시 처리
    private void generateErrorCodeResponseExample(Operation operation, ApiErrorCodeExample example) {
        BaseErrorCode code = resolveErrorCode(example);
        if (code == null) return;

        ExampleHolder holder = ExampleHolder.builder()
                .code(code.getStatus().value())
                .name(code.toString())  // 또는 code.name() if enum
                .holder(getSwaggerExample(code))
                .build();

        addExamplesToResponses(operation.getResponses(), holder);
    }

    // 어노테이션에서 Class + field를 통해 BaseErrorCode 객체 추출
    private BaseErrorCode resolveErrorCode(ApiErrorCodeExample example) {
        try {
            Field field = example.errorClass().getField(example.field());
            Object constant = field.get(null);
            if (constant instanceof BaseErrorCode) {
                return (BaseErrorCode) constant;
            }
        } catch (Exception e) {
            // 로그로 남기기
            e.printStackTrace();
        }
        return null;
    }

    // Swagger Example 객체 생성
    private Example getSwaggerExample(BaseErrorCode errorCode) {
        ErrorExampleResponse dto = ErrorExampleResponse.from(errorCode);
        Example example = new Example();
        example.setValue(dto);
        return example;
    }

    // 여러 개의 Example들을 ApiResponses에 추가
    private void addExamplesToResponses(ApiResponses responses,
                                        Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((status, v) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();

            v.forEach(exampleHolder -> mediaType.addExamples(
                    exampleHolder.getName(),
                    exampleHolder.getHolder()
            ));
            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(String.valueOf(status), apiResponse);
        });
    }

    // 단일 ExampleHolder를 ApiResponses에 추가
    private void addExamplesToResponses(ApiResponses responses, ExampleHolder exampleHolder) {
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ApiResponse apiResponse = new ApiResponse();

        mediaType.addExamples(exampleHolder.getName(), exampleHolder.getHolder());
        content.addMediaType("application/json", mediaType);
        apiResponse.content(content);
        responses.addApiResponse(String.valueOf(exampleHolder.getCode()), apiResponse);
    }
}