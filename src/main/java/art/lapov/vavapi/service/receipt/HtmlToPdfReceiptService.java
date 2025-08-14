package art.lapov.vavapi.service.receipt;

import art.lapov.vavapi.dto.ReservationDTO;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class HtmlToPdfReceiptService {

    private final TemplateEngine templateEngine;

    /**
     * Render Thymeleaf HTML and convert to PDF using OpenHTMLtoPDF.
     */
    public byte[] generateReceiptPdf(ReservationDTO dto) {
        try {
            // 1) Prepare Thymeleaf context
            Context ctx = new Context(Locale.FRANCE);
            ctx.setVariable("r", dto);

            String html = templateEngine.process("receipt", ctx);
            String baseUrl = new ClassPathResource("static/").getURL().toExternalForm();

            // 2) Build PDF from HTML
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, baseUrl);
                // Register Unicode fonts so accents are rendered correctly
                builder.useFont(() -> {
                    try {
                        return new ClassPathResource("fonts/Montserrat-Regular.ttf").getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, "Montserrat", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
                builder.useFont(() -> {
                    try {
                        return new ClassPathResource("fonts/Montserrat-Bold.ttf").getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, "Montserrat", 700, BaseRendererBuilder.FontStyle.NORMAL, true);

                builder.toStream(out);
                builder.run();
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render PDF via OpenHTMLtoPDF", e);
        }
    }

}
