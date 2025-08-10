package art.lapov.vavapi.controller;

import art.lapov.vavapi.enums.FileType;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.AccountService;
import art.lapov.vavapi.service.FileService;
import art.lapov.vavapi.service.LocationService;
import art.lapov.vavapi.service.StationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private FileService fileService;

    private AccountService accountService;

    private LocationService locationService;

    private StationService stationService;

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal User user) {
        try {
            String fileName = fileService.saveImage(file, FileType.AVATAR, user.getId());

            accountService.updateAvatar(user.getId(), fileName);

            return ResponseEntity.ok(Map.of(
                    "message", "Photo téléchargée avec succès",
                    "fileName", fileName
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erreur de téléchargement de fichier"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/location/{locationId}")
    public ResponseEntity<Map<String, String>> uploadLocationPhoto(@PathVariable String locationId,
                                                 @RequestParam("file") MultipartFile file,
                                                 @AuthenticationPrincipal User user) {
        try {
            // Checking access rights
            if (!locationService.isOwner(locationId, user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            String fileName = fileService.saveImage(file, FileType.LOCATION, locationId);
            locationService.updatePhoto(locationId, fileName);

            return ResponseEntity.ok(Map.of(
                    "message", "Photo téléchargée avec succès",
                    "fileName", fileName
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erreur de téléchargement de fichier"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/station/{stationId}")
    public ResponseEntity<?> uploadStationPhoto(@PathVariable String stationId,
                                                @RequestParam("file") MultipartFile file,
                                                @AuthenticationPrincipal User user) {
        try {
            // Checking access rights
            if (!stationService.isOwner(stationId, user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            String fileName = fileService.saveImage(file, FileType.STATION, stationId);
            stationService.updatePhoto(stationId, fileName);

            return ResponseEntity.ok(Map.of(
                    "message", "Photo téléchargée avec succès",
                    "fileName", fileName
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erreur de téléchargement de fichier"));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // TODO Delete photos
//    @DeleteMapping("/avatar")
//    public ResponseEntity<?> deleteAvatar(@AuthenticationPrincipal User user) {
//        try {
//            if (user.getAvatarUrl() != null) {
//                fileService.deleteImage(user.getAvatarUrl(), FileType.AVATAR);
//                userService.updateAvatar(user.getId(), null);
//            }
//
//            return ResponseEntity.ok(Map.of("message", "Аватар удален"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }

}

