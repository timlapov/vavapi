package art.lapov.vavapi.service;

import art.lapov.vavapi.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String myEmail = "lapov.art@gmail.cpm";

    public void sendEmailValidation(User user, String token) {
        String link = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()+"/api/account/validate/" + token;

        Context ctx = new Context();
        ctx.setVariable("name", user.getFirstName());
        ctx.setVariable("verificationUrl", link);

        String htmlContent = templateEngine.process("email/validation", ctx);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(myEmail);
            helper.setSubject("Volt à vous : Confirmation de l'adresse e-mail");
            helper.setText(htmlContent, true); // true => HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }

    }

//    private void sendMailBase(String to, String message, String subject) {
//        try {
//            MimeMessage mimeMessage = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
//            helper.setTo(to);
//            helper.setFrom("springholiday@human-booster.fr");
//            helper.setSubject(subject);
//
//            helper.setText(message,true); //Temporaire, email à remplacer par un JWT
//            mailSender.send(mimeMessage);
//        } catch (MailException | MessagingException e) {
//            throw new RuntimeException("Unable to send mail", e);
//        }
//    }

    public void sendResetPassword(User user, String token) {
        String link = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()+"/reset-password.html?token="+token;

        Context ctx = new Context();
        ctx.setVariable("resetPasswordLink", link);

        String htmlContent = templateEngine.process("email/reset-password", ctx);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(myEmail);
            helper.setSubject("Volt à vous : Récupération du mot de passe");
            helper.setText(htmlContent, true); // true => HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }

}
