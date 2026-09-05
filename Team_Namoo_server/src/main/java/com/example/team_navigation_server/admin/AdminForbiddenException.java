package com.example.team_navigation_server.admin;

public class AdminForbiddenException extends RuntimeException {
    public AdminForbiddenException(String message) {
        super(message);
    }
}
