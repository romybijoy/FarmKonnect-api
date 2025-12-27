package com.fc.authservice.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Utility class for sending application-related email messages such as OTP emails
 * and password reset links.
 * Uses JavaMailSender to construct and deliver MIME emails.
 */
@Slf4j
@Component
public class EmailUtil {

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * Sends an OTP email to the specified recipient.
     *
     * @param email recipient's email
     * @param otp   generated OTP to include in the email
     * @throws MessagingException if the email construction or sending fails
     */
    public void sendOtpEmail(String email, String otp) throws MessagingException {
        log.info("Sending OTP email to {}", email);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Verify OTP");
        mimeMessageHelper.setText("""
        <div>
         <p>Your OTP is: <strong>%s</strong></p>
        </div>
        """.formatted(otp), true);

        javaMailSender.send(mimeMessage);

        log.info("OTP email successfully sent to {}", email);
    }

    /**
     * Sends a password setup link to the user.
     *
     * @param email recipient's email
     * @throws MessagingException if the email cannot be sent
     */
    public void sendSetPasswordEmail(String email) throws MessagingException {
        log.info("Sending Set Password email to {}", email);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Set Password");

        String url = frontendBaseUrl + "/set-password?email=" + email;

        mimeMessageHelper.setText("""
                <div>
                  <p>Click the link below to set your password:</p>
                     <a href="%s" target="_blank">Set Password</a>
                 </div>
        """.formatted(url), true);

        javaMailSender.send(mimeMessage);

        log.info("Set Password email successfully sent to {}", email);
    }
}
