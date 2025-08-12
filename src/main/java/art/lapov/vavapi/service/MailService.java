package art.lapov.vavapi.service;

import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
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
    private final String myEmail = "lapov.art@gmail.com";

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

    // TODO
    public void sendReservationAccepted(User client, Reservation updated) {
    }

    // TODO
    public void sendReviewRequest(User client, Reservation reservation) {
    }

    // TODO
    public void sendReservationRejected(User client, Reservation updated, String reason) {
    }

    // TODO
    public void sendReservationConfirmation(User client, Reservation saved) {
    }

    // TODO
    public void sendReservationCancelled(User owner, Reservation reservation) {
    }

    // TODO
    public void sendNewReservationRequest(User owner, Reservation saved) {
    }

    // TODO
    public void sendReservationRequestReceived(User client, Reservation saved) {
    }

    // TODO
    public void sendReservationAcceptedPleasePayRequest(User client, Reservation updated) {
    }

    // TODO
    public void sendPaymentConfirmation(User client, Reservation updated) {
    }

    // TODO
    public void sendPaymentReceivedNotification(User owner, Reservation updated) {
    }

    // TODO
    public void sendCancellationWithRefund(User client, Reservation reservation, int i) {
    }
}
