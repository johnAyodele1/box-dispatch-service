package com.example.boxdispatch.common.exception;

import com.example.boxdispatch.box.exception.BoxNotFoundException;
import com.example.boxdispatch.box.exception.DuplicateBoxTxrefException;
import com.example.boxdispatch.box.exception.DuplicateItemCodeException;
import com.example.boxdispatch.box.exception.InsufficientBatteryException;
import com.example.boxdispatch.box.exception.InvalidBoxStateException;
import com.example.boxdispatch.box.exception.WeightLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoxNotFoundException.class)
    public ResponseEntity<ApiError> handleBoxNotFound(
        BoxNotFoundException exception,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request
        );
    }

    @ExceptionHandler({
        InvalidBoxStateException.class,
        InsufficientBatteryException.class,
        WeightLimitExceededException.class,
        DuplicateItemCodeException.class,
        DuplicateBoxTxrefException.class
    })
    public ResponseEntity<ApiError> handleConflict(
        RuntimeException exception,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error ->
                error.getField() + ": " + error.getDefaultMessage()
            )
            .collect(Collectors.joining("; "));

        return buildError(
            HttpStatus.BAD_REQUEST,
            message,
            request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
        ConstraintViolationException exception,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
        DataIntegrityViolationException exception,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.CONFLICT,
            "The request conflicts with existing data",
            request
        );
    }

    private ResponseEntity<ApiError> buildError(
        HttpStatus status,
        String message,
        HttpServletRequest request
    ) {
        ApiError error = new ApiError(
            OffsetDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI()
        );

        return ResponseEntity
            .status(status)
            .body(error);
    }
}
