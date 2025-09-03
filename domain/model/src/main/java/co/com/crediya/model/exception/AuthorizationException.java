package co.com.crediya.model.exception;

public class AuthorizationException extends RuntimeException {
    public static final String UNAUTHORIZED = "Unauthorized, you need to be authenticated to access this resource";
    public static final String FORBIDDEN = "Forbidden, you don't have permission to access this resource";
    public static final String EMAIL_NOT_OWNER = "You can't open credit applications for other people";

    public AuthorizationException(String message) {
        super(message);
    }
}
