package art.lapov.vavapi.service;

import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String myEmail = "lapov.art@gmail.com";
    @Value("${app.frontend.base-url}")
    private String frontBaseUrl;

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

    // Notify client that reservation was rejected by the owner (optionally include a reason)
    public void sendReservationRejected(User client, Reservation updated, String reason) {
        Context ctx = new Context();

        ctx.setVariable("name", client.getFirstName());
        ctx.setVariable("stationName", updated.getStation().getLocation().getName());
        ctx.setVariable("startDate", updated.getStartDate());
        ctx.setVariable("endDate", updated.getEndDate());
        ctx.setVariable("reservationId", updated.getId());

        // Optional 'reason' shown only if not empty (handled by template)
        ctx.setVariable("reason", reason);

        String linkToDashboard = frontBaseUrl + "/dashboard";
        ctx.setVariable("linkToDashboard", linkToDashboard);

        // Render Thymeleaf template
        String htmlContent = templateEngine.process("email/reservation-rejected", ctx);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );
            helper.setTo(client.getEmail());
            helper.setFrom(myEmail);
            helper.setSubject("Volt à vous : Réservation refusée");
            helper.setText(htmlContent, true); // true => HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }


    // Notify owner that a reservation has been cancelled
    public void sendReservationCancelled(User owner, Reservation reservation) {
        Context ctx = new Context();

        ctx.setVariable("name", owner.getFirstName());
        String clientFullName = reservation.getClient().getFirstName() + " " + reservation.getClient().getLastName();
        ctx.setVariable("clientName", clientFullName);
        ctx.setVariable("stationName", reservation.getStation().getLocation().getName());
        ctx.setVariable("startDate", reservation.getStartDate()); // LocalDateTime
        ctx.setVariable("endDate", reservation.getEndDate());     // LocalDateTime
        ctx.setVariable("reservationId", reservation.getId());

        String linkToDashboard = frontBaseUrl + "/dashboard";
        ctx.setVariable("linkToDashboard", linkToDashboard);

        String html = templateEngine.process("email/reservation-cancelled-owner", ctx);

        MimeMessage mime = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );
            helper.setTo(owner.getEmail());
            helper.setFrom(myEmail);
            helper.setSubject("Volt à vous : Réservation annulée");
            helper.setText(html, true); // HTML

            mailSender.send(mime);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }

    // Notify owner about new reservation request
    public void sendNewReservationRequest(User owner, Reservation saved) {
        Context ctx = new Context();

        ctx.setVariable("name", owner.getFirstName());
        ctx.setVariable("clientName", saved.getClient().getFullName());
        ctx.setVariable("stationName", saved.getStation().getLocation().getName());
        ctx.setVariable("startDate", saved.getStartDate()); // LocalDateTime
        ctx.setVariable("endDate", saved.getEndDate());     // LocalDateTime
        ctx.setVariable("reservationId", saved.getId());
        String linkToDashboard = frontBaseUrl + "/dashboard";
        ctx.setVariable("linkToDashboard", linkToDashboard);


        String htmlContent = templateEngine.process("email/new-reservation-request-to-owner", ctx);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );
            helper.setTo(owner.getEmail());
            helper.setFrom(myEmail);
            helper.setSubject("Volt à vous : Nouvelle demande de réservation");
            helper.setText(htmlContent, true); // true => HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }

    // Send confirmation to client that request was received
    public void sendReservationRequestReceived(User client, Reservation saved) {
        Context ctx = new Context();
        ctx.setVariable("name", client.getFullName());
        ctx.setVariable("stationName", saved.getStation().getLocation().getName());
        ctx.setVariable("reservationId", saved.getId());
        ctx.setVariable("startDate", saved.getStartDate());
        ctx.setVariable("endDate", saved.getEndDate());

        String htmlContent = templateEngine.process("email/reservation-request-received", ctx);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setTo(client.getEmail());
            helper.setFrom(myEmail);
            helper.setSubject("Volt à vous : Demande de réservation reçue");
            helper.setText(htmlContent, true); // true => HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }

    // Send email to client: reservation accepted, please proceed to payment
    public void sendReservationAcceptedPleasePayRequest(User client, Reservation updated) {
        Context ctx = new Context();

        ctx.setVariable("name", client.getFirstName());
        ctx.setVariable("stationName", updated.getStation().getLocation().getName());
        ctx.setVariable("startDate", updated.getStartDate());
        ctx.setVariable("endDate", updated.getEndDate());
        ctx.setVariable("reservationId", updated.getId());
        String linkToDashboard = frontBaseUrl + "/dashboard";
        ctx.setVariable("linkToDashboard", linkToDashboard);
        ctx.setVariable("amountEuros", updated.getTotalCostInCents() / 100);

        String htmlContent = templateEngine.process("email/reservation-accepted-please-pay", ctx);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );
            helper.setTo(client.getEmail());
            helper.setFrom(myEmail);
            helper.setSubject("Volt à vous : Réservation acceptée — paiement requis");
            helper.setText(htmlContent, true); // true => HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }

}
