package pl.eurokawa.storage;

public enum FileType {
    PHOTO("photos","image/jpeg"),
    TERMS("terms","application/pdf");

    private final String folder;
    private final String contentType;


    FileType(String folder, String contentType) {
        this.folder = folder;
        this.contentType = contentType;
    }

    public static FileType findTypeByFolderName(String folder) {
        for (FileType type : values()){
            if (type.getFolder().equalsIgnoreCase(folder)){
                return type;
            }
        }

        throw new IllegalArgumentException("There is no FileType with this folder name: " + folder);
    }

    public String getFolder() {
        return folder;
    }

    public String getContentType() {
        return contentType;
    }
}
