package team.jndk.praktyki.praktyki_spring.model.exception;

public class EmptyParamException extends RuntimeException {
    public EmptyParamException(String message) {
        super(message);
    }
}