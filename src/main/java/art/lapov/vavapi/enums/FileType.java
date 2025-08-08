package art.lapov.vavapi.enums;

public enum FileType {
    AVATAR("avatars"),
    STATION("stations"),
    LOCATION("locations");

    private final String folderName;

    FileType(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }
}

