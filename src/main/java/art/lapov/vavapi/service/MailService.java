package art.lapov.vavapi.service;

import art.lapov.vavapi.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@AllArgsConstructor
class MailService {

    private JavaMailSender mailSender;

    public void sendEmailValidation(User user, String token) {
        String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        String message = """
                    Pour valider votre compte, cliquez <a href="%s">ici</a>
                    """
                .formatted(serverUrl+"/api/account/validate/"+token);
        sendMailBase(user.getEmail(), message, "Volt à vous : Confirmation de l'adresse e-mail");
    }

    private void sendMailBase(String to, String message, String subject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setTo(to);
            helper.setFrom("springholiday@human-booster.fr");
            helper.setSubject(subject);

            helper.setText(message,true); //Temporaire, email à remplacer par un JWT
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            throw new RuntimeException("Unable to send mail", e);
        }
    }

    public void sendResetPassword(User user, String token) {
        String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        String message = """
                    To reset your password click on <a href="%s">this link</a>
                    """
                .formatted(serverUrl+"/reset-password.html?token="+token);
        sendMailBase(user.getEmail(), message, "Spring Holiday Reset Password");
    }

}
