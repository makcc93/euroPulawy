package pl.eurokawa.file;

import java.io.IOException;
import java.io.InputStream;

public interface FileService {
    byte[] uploadFile(FileType fileType, InputStream inputStream, String remoteFileName) throws IOException;
    void deleteFile(FileType fileType, String fileKey);
    byte[] getFile(FileType fileType, String fileKey);
}
