package co.com.crediya.api.exceptionhandler;

import co.com.crediya.model.exception.AuthorizationException;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import static co.com.crediya.api.exceptionhandler.ErrorConstants.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        Throwable error = getError(request);
        errorAttributes.put(ATTRIBUTE_MESSAGE, error.getMessage());
        errorAttributes.put(ATTRIBUTE_METHOD, request.method().name());
        errorAttributes.put(ATTRIBUTE_PATH, request.path());


        if (error instanceof ConstraintViolationException) {
            errorAttributes.put(ATTRIBUTE_ERROR, VALIDATION_ERROR);
            errorAttributes.put(ATTRIBUTE_STATUS, STATUS_BAD_REQUEST);
        } else if (error instanceof BusinessException) {
            errorAttributes.put(ATTRIBUTE_ERROR, BUSINESS_RULE_VIOLATION);
            errorAttributes.put(ATTRIBUTE_STATUS, STATUS_CONFLICT);
        }  else if (error instanceof JwtException) {
            errorAttributes.put(ATTRIBUTE_ERROR, JWT_ERROR);
            errorAttributes.put(ATTRIBUTE_STATUS, HttpStatus.UNAUTHORIZED.value());
        } else if (error instanceof AuthorizationException) {
            errorAttributes.put(ATTRIBUTE_ERROR, AUTHORIZATION_ERROR);
            errorAttributes.put(ATTRIBUTE_STATUS, STATUS_FORBIDDEN);

        } else {
            errorAttributes.put(ATTRIBUTE_ERROR, INTERNAL_SERVER_ERROR);
            errorAttributes.put(ATTRIBUTE_STATUS, STATUS_INTERNAL_SERVER_ERROR);
        }
        return errorAttributes;
    }
}
