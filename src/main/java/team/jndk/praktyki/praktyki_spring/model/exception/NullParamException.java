package team.jndk.praktyki.praktyki_spring.model.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


public class NullParamException extends RuntimeException {

    public NullParamException(String message) {
        super(message);
    }
}
