package co.com.crediya.model.exception;

public class AwsException extends RuntimeException {
    public static final String QUEUE_NOT_AVAILABLE = "The AWS SQS queue is not available";
    public static final String QUEUE_SEND_ERROR = "Error sending message to AWS SQS queue";

    public AwsException(String message) {
        super(message);
    }
}
