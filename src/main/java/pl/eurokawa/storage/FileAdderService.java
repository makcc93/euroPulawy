package pl.eurokawa.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.im4java.core.IM4JavaException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.eurokawa.purchase.Purchase;

@Service
public class FileAdderService {
    private final S3Service s3Service;
    private final FileConversionService fileConversionService;
    private final FileMimeTypeService fileMimeTypeService;

    private static final Logger log = LogManager.getLogger(FileAdderService.class);

    public FileAdderService(S3Service s3Service, FileConversionService fileConversionService, FileMimeTypeService fileMimeTypeService) {
        this.s3Service = s3Service;
        this.fileConversionService = fileConversionService;
        this.fileMimeTypeService = fileMimeTypeService;
    }


//    public byte[] uploadFileToS3(InputStream inputStream, Purchase purchase, String remoteFileName) throws IOException, InterruptedException, IM4JavaException {
//        byte[] imageBytes = inputStream.readAllBytes();
//
//        String mimeTypeFromBytes = fileMimeTypeService.getMimeTypeFromBytes(new ByteArrayInputStream(imageBytes));
//
//        byte[] converted = fileConversionService.convertFileToJpegIfNeeded(mimeTypeFromBytes, imageBytes,remoteFileName);
//
//        s3Service.uploadPurchasePhoto(purchase,remoteFileName,imageBytes);
//
//        return imageBytes;
//    }

    public byte[] uploadFile(FileType fileType,InputStream inputStream, String remoteFileName) throws IOException{
        byte[] fileBytes = inputStream.readAllBytes();

        String mimeTypeFromBytes = fileMimeTypeService.getMimeTypeFromBytes(new ByteArrayInputStream(fileBytes));

        s3Service.uploadFile(fileType,remoteFileName,fileBytes);

        return fileBytes;
    }

}
