package pl.eurokawa.storage;

import pl.eurokawa.file.FileType;

public interface S3Service {
    void uploadFileToS3(FileType fileType, String fileKey, byte[] data);
    void deleteFileFromS3(FileType fileType, String fileKey);
    byte[] downloadFileFromS3(FileType fileType, String fileKey);
}
