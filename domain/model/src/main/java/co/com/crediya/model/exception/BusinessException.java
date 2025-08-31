package co.com.crediya.model.exception;

public class BusinessException extends RuntimeException {

    public static final String USER_NOT_FOUND = "User not found";
    public static final String LOAN_TYPE_NOT_FOUND = "Loan type not found";
    public static final String AMOUNT_OUT_OF_RANGE = "Loan amount is out of allowed range";

    public BusinessException(String message) {
        super(message);
    }
}
