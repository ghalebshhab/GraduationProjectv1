package com.jomap.backend.Services.Notefications;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;



    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Welcome to JoMap");
            helper.setFrom("jomap.noreply@gmail.com", "JoMap");

            String htmlContent = buildWelcomeEmailTemplate(username);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean sendPasswordResetOtp(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("JoMap Password Reset OTP");

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0; padding:0; background-color:#f3f7f4; font-family:Arial, Helvetica, sans-serif;">
                
                    <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f3f7f4; padding:40px 0;">
                        <tr>
                            <td align="center">
                
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:18px; overflow:hidden; box-shadow:0 10px 30px rgba(0,0,0,0.10);">
                                    
                                    <tr>
                                        <td style="background:linear-gradient(135deg,#0f766e,#16a34a); padding:35px 25px; text-align:center;">
                                            <h1 style="margin:0; color:#ffffff; font-size:32px; letter-spacing:1px;">
                                                JoMap
                                            </h1>
                                            <p style="margin:10px 0 0; color:#dff7ea; font-size:15px;">
                                                Explore Jordan safely and easily
                                            </p>
                                        </td>
                                    </tr>
                
                                    <tr>
                                        <td style="padding:35px 40px; text-align:center;">
                                            <h2 style="margin:0; color:#1f2937; font-size:24px;">
                                                Password Reset Request
                                            </h2>
                
                                            <p style="color:#6b7280; font-size:15px; line-height:1.7; margin:18px 0 25px;">
                                                We received a request to reset your JoMap account password.
                                                Use the OTP code below to continue.
                                            </p>
                
                                            <div style="background:#ecfdf5; border:2px dashed #10b981; border-radius:14px; padding:22px; margin:25px 0;">
                                                <p style="margin:0 0 8px; color:#065f46; font-size:14px; font-weight:bold;">
                                                    Your OTP Code
                                                </p>
                                                <div style="font-size:38px; font-weight:bold; letter-spacing:8px; color:#047857;">
                                                    {{OTP_CODE}}
                                                </div>
                                            </div>
                
                                            <p style="background:#fff7ed; color:#c2410c; padding:12px 16px; border-radius:10px; font-size:14px; margin:25px 0;">
                                                This OTP is valid for <strong>1 minute only</strong>.
                                            </p>
                
                                            <p style="color:#6b7280; font-size:14px; line-height:1.6;">
                                                If you did not request a password reset, please ignore this email.
                                                Your account will remain secure.
                                            </p>
                                        </td>
                                    </tr>
                
                                    <tr>
                                        <td style="background-color:#f9fafb; padding:22px; text-align:center; border-top:1px solid #e5e7eb;">
                                            <p style="margin:0; color:#9ca3af; font-size:13px;">
                                                © 2026 JoMap. All rights reserved.
                                            </p>
                                            <p style="margin:8px 0 0; color:#9ca3af; font-size:12px;">
                                                This is an automated message. Please do not reply.
                                            </p>
                                        </td>
                                    </tr>
                
                                </table>
                
                            </td>
                        </tr>
                    </table>
                
                </body>
                </html>
                """;

            htmlContent = htmlContent.replace("{{OTP_CODE}}", otp);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            return true;

        } catch (Exception e) {
            System.out.println("Failed to send OTP email: " + e.getMessage());
            return false;
        }
    }

    public void sendLoginSuccessEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Login Successful - JoMap");
            helper.setFrom("jomap.noreply@gmail.com", "JoMap");

            String htmlContent = buildLoginSuccessTemplate(username);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildWelcomeEmailTemplate(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Welcome to JoMap</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:40px 0;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 4px 18px rgba(0,0,0,0.08);">
                                    
                                    <tr>
                                        <td style="background:linear-gradient(135deg, #0f172a, #1e3a8a); padding:32px; text-align:center;">
                                            <h1 style="margin:0; color:#ffffff; font-size:30px;">Welcome to JoMap</h1>
                                            <p style="margin:10px 0 0; color:#dbeafe; font-size:15px;">
                                                Your journey starts here
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:40px 35px;">
                                            <h2 style="margin:0 0 16px; color:#111827; font-size:24px;">
                                                Hello %s,
                                            </h2>

                                            <p style="margin:0 0 16px; color:#4b5563; font-size:16px; line-height:1.7;">
                                                We’re excited to have you join <strong>JoMap</strong>.
                                                Your account has been created successfully, and you’re now ready to explore the app.
                                            </p>

                                            <p style="margin:0 0 24px; color:#4b5563; font-size:16px; line-height:1.7;">
                                                You can now discover features, connect with the community, and enjoy a smooth experience built just for you.
                                            </p>

                                            <table cellpadding="0" cellspacing="0" style="margin:30px 0;">
                                                <tr>
                                                    <td align="center" style="border-radius:10px;" bgcolor="#2563eb">
                                                        <a href="http://localhost:5173"
                                                           style="display:inline-block; padding:14px 28px; font-size:16px; color:#ffffff; text-decoration:none; font-weight:bold; border-radius:10px;">
                                                            Open JoMap
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>

                                            <div style="margin-top:30px; padding:20px; background:#f9fafb; border-radius:12px; border:1px solid #e5e7eb;">
                                                <p style="margin:0; color:#374151; font-size:14px; line-height:1.6;">
                                                    <strong>Need help?</strong><br>
                                                    If you have any questions, simply reply to this email or contact our support team.
                                                </p>
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:24px 35px; background:#f9fafb; border-top:1px solid #e5e7eb; text-align:center;">
                                            <p style="margin:0; color:#6b7280; font-size:13px; line-height:1.6;">
                                                © 2026 JoMap. All rights reserved.<br>
                                                This is an automated email, please do not reply directly.
                                            </p>
                                        </td>
                                    </tr>

                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(username);
    }

    private String buildLoginSuccessTemplate(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Login Successful</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, Helvetica, sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:40px 0;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 4px 18px rgba(0,0,0,0.08);">

                                    <tr>
                                        <td style="background:linear-gradient(135deg, #065f46, #10b981); padding:32px; text-align:center;">
                                            <h1 style="margin:0; color:#ffffff; font-size:28px;">Login Successful</h1>
                                            <p style="margin:10px 0 0; color:#d1fae5; font-size:15px;">
                                                Your JoMap account was accessed successfully
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:40px 35px;">
                                            <h2 style="margin:0 0 16px; color:#111827; font-size:24px;">
                                                Hello %s,
                                            </h2>

                                            <p style="margin:0 0 16px; color:#4b5563; font-size:16px; line-height:1.7;">
                                                This is a confirmation that your <strong>JoMap</strong> account was logged in successfully.
                                            </p>

                                            <p style="margin:0 0 24px; color:#4b5563; font-size:16px; line-height:1.7;">
                                                If this was you, no action is needed.
                                                If you do not recognize this activity, please reset your password immediately.
                                            </p>

                                            <table cellpadding="0" cellspacing="0" style="margin:30px 0;">
                                                <tr>
                                                    <td align="center" style="border-radius:10px;" bgcolor="#dc2626">
                                                        <a href="http://localhost:5173/forgot-password"
                                                           style="display:inline-block; padding:14px 28px; font-size:16px; color:#ffffff; text-decoration:none; font-weight:bold; border-radius:10px;">
                                                            Reset Password
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:24px 35px; background:#f9fafb; border-top:1px solid #e5e7eb; text-align:center;">
                                            <p style="margin:0; color:#6b7280; font-size:13px; line-height:1.6;">
                                                © 2026 JoMap. All rights reserved.<br>
                                                This is an automated security message.
                                            </p>
                                        </td>
                                    </tr>

                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(username);
    }

}