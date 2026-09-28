package com.example.machinerisk.exception;

import com.example.machinerisk.service.MachineService.MlServiceUnavailableException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,String> notFound(Exception e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> badRequest(Exception e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(IllegalStateException.class) @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String,String> conflict(Exception e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(MlServiceUnavailableException.class) @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String,String> mlUnavailable(Exception e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> invalid(MethodArgumentNotValidException e) { return Map.of("error", "Request is missing required fields or contains invalid data"); }
}
