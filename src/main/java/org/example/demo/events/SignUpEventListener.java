package org.example.demo.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.service.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignUpEventListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("EmailTaskExecutor")
    public void handleSignUpEvent(SignUpEvent event) {
        // 이메일 전송
        sendEmail(event);
    }

    private void sendEmail(SignUpEvent event) {
        String subject = "회원이 되신 것을 환영합니다!";
        String body = event.getUsername() + "님, 회원가입이 완료되었습니다.";
        emailService.send(event.getEmail(), subject, body);
    }

}
