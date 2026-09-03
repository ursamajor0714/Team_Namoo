package com.example.team_navigation_server.email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody SendCodeRequest request) {
        emailVerificationService.sendCode(request.email());
        return ResponseEntity.ok("인증 코드를 전송했습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    public record SendCodeRequest(
            @NotBlank(message = "이메일을 입력해주세요.") @Email String email
    ) {
    }

    public record VerifyCodeRequest(
            @NotBlank(message = "이메일을 입력해주세요.") @Email String email,
            @NotBlank(message = "인증 코드를 입력해주세요.") String code
    ) {
    }
}
