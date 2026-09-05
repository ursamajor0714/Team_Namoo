package com.example.team_navigation_server.email;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean verified;

    protected EmailVerification() {
    }

    public EmailVerification(String email, String code, Instant expiresAt, boolean verified) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = verified;
    }

    public Long getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getCode() {
        return code;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public boolean isVerified() {
        return verified;
    }

    public void updateCode(String code, Instant expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
    }

    public void markVerified() {
        this.verified = true;
    }
}
