package tokens.info.task.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tokens.info.task.exception.ErrorResponse;
import tokens.info.task.exception.TokenException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> notValidFields(TokenException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ErrorResponse.builder()
                        .message(exception.getMessage())
                        .details(exception.getDetails())
                        .build());
    }
}
