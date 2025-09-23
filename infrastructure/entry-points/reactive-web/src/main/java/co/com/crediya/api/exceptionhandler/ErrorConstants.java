package co.com.crediya.api.exceptionhandler;

import org.springframework.http.HttpStatus;

public final class ErrorConstants {

    private ErrorConstants() {}

    public static final String ATTRIBUTE_ERROR = "error";
    public static final String ATTRIBUTE_STATUS = "status";
    public static final String ATTRIBUTE_MESSAGE = "message";
    public static final String ATTRIBUTE_METHOD = "method";
    public static final String ATTRIBUTE_PATH = "path";

    public static final String VALIDATION_ERROR = "Validation Error";
    public static final String BUSINESS_RULE_VIOLATION = "Business Rule Violation";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String JWT_ERROR = "JWT Validation Error";
    public static final String AUTHORIZATION_ERROR = "Authorization Error";
    public static final String AWS_ERROR = "AWS service not available";


    public static final int STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    public static final int STATUS_CONFLICT = HttpStatus.CONFLICT.value();
    public static final int STATUS_INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static final int STATUS_FORBIDDEN = HttpStatus.FORBIDDEN.value();
    public static final int SERVICE_UNAVAILABLE = HttpStatus.SERVICE_UNAVAILABLE.value();
}
