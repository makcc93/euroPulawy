package pl.eurokawa.file;

import com.vaadin.flow.router.NotFoundException;
import org.springframework.stereotype.Service;
import pl.eurokawa.exception.ArgumentNullChecker;
import pl.eurokawa.storage.S3Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class FileServiceImpl implements FileService{
    private final S3Service s3Service;

    public FileServiceImpl(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    public byte[] uploadFile(FileType fileType, InputStream inputStream, String remoteFileName) throws IOException {
        ArgumentNullChecker.check(fileType,"File type");
        ArgumentNullChecker.check(inputStream,"Input stream");
        ArgumentNullChecker.check(remoteFileName,"File name");

        try{
            byte[] fileBytes = inputStream.readAllBytes();
            s3Service.uploadFileToS3(fileType,remoteFileName,fileBytes);

            return fileBytes;
        }
        catch (IOException e){
            throw new IOException("Failed to read input stream!");
        }
    }

    @Override
    public void deleteFile(FileType fileType, String fileName) {
        ArgumentNullChecker.check(fileType,"File type");
        ArgumentNullChecker.check(fileName,"File name");

        s3Service.deleteFileFromS3(fileType,fileName);
    }

    @Override
    public byte[] getFile(FileType fileType, String fileKey) {
        ArgumentNullChecker.check(fileType,"File type");
        ArgumentNullChecker.check(fileKey,"File key");

        return Optional.ofNullable(s3Service.downloadFileFromS3(fileType,fileKey))
                .orElseThrow(() -> new NotFoundException("Cannot find this file!"));
    }

}
