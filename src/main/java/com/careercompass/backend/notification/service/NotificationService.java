package com.careercompass.backend.notification.service;

public interface NotificationService {

    void sendWelcomeEmail(String toEmail, String userName);

    void sendRoadmapStepCompletedEmail(String toEmail,
                                       String userName, String stepTitle, String roadmapTitle);

    void sendSimpleEmail(String toEmail,
                         String subject, String body);
}