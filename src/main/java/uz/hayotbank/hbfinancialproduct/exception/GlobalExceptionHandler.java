package uz.hayotbank.hbfinancialproduct.exception;

import uz.hayotbank.hbfinancialproduct.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleTransactionNotFoundException(
            TransactionNotFoundException ex, WebRequest request) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientBalanceException(
            InsufficientBalanceException ex, WebRequest request) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.BAD_REQUEST.value(),
            "Insufficient Balance",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidTransferException(
            InvalidTransferException ex, WebRequest request) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Transfer",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Validation failed for fields: " + errors.toString(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(
            Exception ex, WebRequest request) {

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}