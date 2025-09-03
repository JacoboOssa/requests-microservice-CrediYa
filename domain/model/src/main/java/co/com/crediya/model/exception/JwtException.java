package co.com.crediya.model.exception;

public class JwtException extends RuntimeException {
    public static final String TOKEN_NOT_FOUND = "Token not found";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String TOKEN_EXPIRED = "Token expired";
    public static final String TOKEN_UNSUPPORTED = "Token unsupported";
    public static final String TOKEN_MALFORMED = "Token malformed";
    public static final String ILEGAL_ARGUMENTS = "Illegal arguments";
    public static final String NO_USER_FOUND = "No user found with the given token";

    public JwtException(String message) {
        super(message);
    }
}