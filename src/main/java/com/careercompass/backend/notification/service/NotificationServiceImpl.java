package com.careercompass.backend.notification.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final Resend resend;
    private final String fromEmail;

    public NotificationServiceImpl(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.email}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        String subject = "Welcome to CareerCompass, " + userName + "!";
        String body = """
                Hi %s,

                Welcome to CareerCompass — your AI-powered career intelligence platform!

                Here's what you can do to get started:
                1. Upload your resume to auto-extract your skills
                2. Set your target job role in your profile
                3. Run your skill gap analysis
                4. View your personalized learning roadmap
                5. Chat with your AI career mentor anytime

                We're excited to help you land your dream role.

                Best,
                The CareerCompass Team
                """.formatted(userName);
        sendEmail(toEmail, subject, body);
    }

    @Override
    @Async
    public void sendRoadmapStepCompletedEmail(String toEmail,
                                              String userName, String stepTitle, String roadmapTitle) {
        String subject = "Great progress, " + userName + "!";
        String body = """
                Hi %s,

                You just completed a step in your learning roadmap!

                Step completed: %s
                Roadmap: %s

                Keep up the momentum — every step brings you closer to your target role.

                Keep going!
                The CareerCompass Team
                """.formatted(userName, stepTitle, roadmapTitle);
        sendEmail(toEmail, subject, body);
    }

    @Override
    @Async
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(toEmail)
                    .subject(subject)
                    .text(body)
                    .build();

            resend.emails().send(params);
            log.info("Email sent successfully via Resend to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email via Resend to {}: {}", toEmail, e.getMessage());
        }
    }
}