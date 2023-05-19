package tokens.info.task.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.Map;

@Getter
@Setter
@Builder
public class ErrorResponse{
    private String message;
    @Singular
    private Map<String, String> details;
}
