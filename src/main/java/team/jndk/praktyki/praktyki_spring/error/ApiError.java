package team.jndk.praktyki.praktyki_spring.error;

import java.time.Instant;

public class ApiError {
    public String message;
    public Instant timestamp;
    public String details;

    public ApiError(String message, String details) {
        this.message = message;
        this.timestamp = Instant.now();
        this.details = details;
    }
}

