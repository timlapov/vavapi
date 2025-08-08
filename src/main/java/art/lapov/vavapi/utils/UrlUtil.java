package art.lapov.vavapi.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlUtil {

    @Value("${app.base.url}")
    private String baseUrl;

    private static String staticBaseUrl;

    @PostConstruct
    public void init() {
        staticBaseUrl = baseUrl;
    }

    public static String getBaseUrl() {
        return staticBaseUrl;
    }

    public static String buildImageUrl(String fileName, String folderName, boolean isMini) {
        if (fileName == null) return null;

        String prefix = isMini ? "mini_" : "";
        return String.format("%s/uploads/%s/%s%s",
                staticBaseUrl, folderName, prefix, fileName);
    }
}

