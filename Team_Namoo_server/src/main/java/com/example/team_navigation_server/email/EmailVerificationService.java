package com.example.team_navigation_server.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class EmailVerificationService {

    private static final int CODE_EXPIRY_MINUTES = 5;

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository, JavaMailSender mailSender) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.mailSender = mailSender;
    }

    // 인증 코드 발송
    public void sendCode(String email) {
        String code = generateCode();
        Instant expiresAt = Instant.now().plus(CODE_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        emailVerificationRepository.deleteByEmail(email);
        emailVerificationRepository.save(new EmailVerification(email, code, expiresAt, false));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Team_Namoo] 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n" + CODE_EXPIRY_MINUTES + "분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    // 인증 코드 확인
    public void verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드를 먼저 요청해주세요."));

        if (Instant.now().isAfter(verification.getExpiresAt())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }
        if (!verification.getCode().equals(code)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        emailVerificationRepository.deleteByEmail(email);
        emailVerificationRepository.save(new EmailVerification(email, code, verification.getExpiresAt(), true));
    }

    // 회원가입 시 이메일 인증 완료 여부 확인
    public boolean isVerified(String email) {
        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    private String generateCode() {
        int number = random.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}
