package com.tamm.identity.exception;

import java.util.Map;
import java.util.Objects;

import jakarta.validation.ConstraintViolation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamm.identity.dto.request.ApiResponse;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(Exception exception) {
        log.error("Exception: ", exception);
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        log.error("Exception: ", exception);
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        log.error("Exception: ", exception);

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        log.error("Validation Exception: ", exception);

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        // Check if the validation error is about token being blank/null
        if (enumKey != null && enumKey.contains("Token cannot be blank")) {
            errorCode = ErrorCode.UNAUTHENTICATED;
        } else {
            try {
                errorCode = ErrorCode.valueOf(enumKey);

                var constraintViolation =
                        exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

                attributes = constraintViolation.getConstraintDescriptor().getAttributes();

                log.info(attributes.toString());

            } catch (IllegalArgumentException e) {
                // Keep default INVALID_KEY if enum not found
            }
        }

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(
                Objects.nonNull(attributes)
                        ? mapAttribute(errorCode.getMessage(), attributes)
                        : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    /**
     * Handle FeignException - Parse error response from downstream services
     */
    @ExceptionHandler(value = FeignException.class)
    ResponseEntity<ApiResponse> handlingFeignException(FeignException exception) {
        log.error("FeignException: {}", exception.getMessage());

        ApiResponse apiResponse = new ApiResponse();

        try {
            // Try to parse the error response from the downstream service
            String responseBody = exception.contentUTF8();
            log.debug("Feign response body: {}", responseBody);

            if (responseBody != null && !responseBody.isEmpty()) {
                // Parse the JSON response
                ApiResponse downstreamResponse = objectMapper.readValue(responseBody, ApiResponse.class);

                apiResponse.setCode(downstreamResponse.getCode());
                apiResponse.setMessage(downstreamResponse.getMessage());
                apiResponse.setResult(downstreamResponse.getResult());
            } else {
                // No response body, use status code
                apiResponse.setCode(exception.status());
                apiResponse.setMessage(exception.getMessage());
            }

            return ResponseEntity.status(exception.status()).body(apiResponse);

        } catch (Exception e) {
            log.error("Error parsing Feign exception response", e);

            // Fallback to generic error
            apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
            apiResponse.setMessage("Error calling external service: " + exception.getMessage());

            return ResponseEntity.status(exception.status()).body(apiResponse);
        }
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
