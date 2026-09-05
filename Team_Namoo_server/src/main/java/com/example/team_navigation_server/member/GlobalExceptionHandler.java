package com.example.team_navigation_server.member;

import com.example.team_navigation_server.admin.AdminForbiddenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    @ExceptionHandler(AdminForbiddenException.class)
    public ResponseEntity<?> handleAdminForbidden(AdminForbiddenException e){
        return ResponseEntity.status(403).body(e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(message);
    }
}
