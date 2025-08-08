package art.lapov.vavapi.service;

import art.lapov.vavapi.enums.FileType;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final int MAX_SIZE = 1600;
    private static final int MINI_SIZE = 300;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String saveImage(MultipartFile file, FileType fileType, String entityId) throws IOException {
        validateFile(file);

        // Create directory if it does not exist
        Path dirPath = Paths.get(uploadDir, fileType.getFolderName());
        Files.createDirectories(dirPath);

        // Delete the old photo if it exists
        deleteExistingImage(entityId, fileType);

        // Generate file name
        String fileName = entityId + "_" + UUID.randomUUID().toString() + ".jpg";

        // Processing the image
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

        // Resize and save the main image
        BufferedImage resizedImage = resizeToSquare(originalImage, MAX_SIZE);
        File mainFile = dirPath.resolve(fileName).toFile();
        ImageIO.write(resizedImage, "jpg", mainFile);

        // Creating and saving a thumbnail
        BufferedImage miniImage = resizeToSquare(originalImage, MINI_SIZE);
        File miniFile = dirPath.resolve("mini_" + fileName).toFile();
        ImageIO.write(miniImage, "jpg", miniFile);

        return fileName;
    }

    private BufferedImage resizeToSquare(BufferedImage original, int targetSize) {
        // Determining the size for a square crop
        int size = Math.min(original.getWidth(), original.getHeight());

        // Cropped to square (centering)
        BufferedImage cropped = Scalr.crop(original,
                (original.getWidth() - size) / 2,
                (original.getHeight() - size) / 2,
                size, size);

        // Resize to size
        return Scalr.resize(cropped, Scalr.Method.QUALITY, targetSize, targetSize);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("The file must be an image");
        }
    }

    //TODO
    private void deleteExistingImage(String entityId, FileType fileType) {
        try {
            Path dirPath = Paths.get(uploadDir, fileType.getFolderName());
            if (Files.exists(dirPath)) {
                Files.list(dirPath)
                        .filter(path -> path.getFileName().toString().startsWith(entityId + "_"))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ignored) {}
                        });
            }
        } catch (IOException ignored) {}
    }

    //TODO
    public void deleteImage(String fileName, FileType fileType) {
        try {
            Path dirPath = Paths.get(uploadDir, fileType.getFolderName());
            Files.deleteIfExists(dirPath.resolve(fileName));
            Files.deleteIfExists(dirPath.resolve("mini_" + fileName));
        } catch (IOException ignored) {}
    }

    public String getImagePath(String fileName, FileType fileType, boolean isMini) {
        String prefix = isMini ? "mini_" : "";
        return String.format("/%s/%s/%s%s", uploadDir, fileType.getFolderName(), prefix, fileName);
    }
}
