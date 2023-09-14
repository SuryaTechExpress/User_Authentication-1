package com.User_Authentication.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendVerificationEmail(String to, String verificationToken) {
        String subject = "Account Verification";
        String text = "Click the following link to verify your account: http://yourwebsite.com/verify?token=" + verificationToken;
        sendEmail(to, subject, text);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    public void sendPasswordResetEmail(String email, String resetLink) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText("To reset your password, click the link below:\n" + resetLink, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            // Handle email sending exception here
            e.printStackTrace();
        }
    }
}
