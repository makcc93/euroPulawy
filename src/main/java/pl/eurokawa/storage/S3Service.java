package pl.eurokawa.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import pl.eurokawa.purchase.Purchase;
import pl.eurokawa.security.S3Config;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class S3Service {
    private static final Logger log = LogManager.getLogger(S3Service.class);
    private final S3Config s3Config;
    private final S3Client s3Client;
    private final FileConversionService fileConversionService;

    @Autowired
    public S3Service(S3Config s3Config, S3Client s3Client, FileConversionService fileConversionService) {
        this.s3Config = s3Config;
        this.s3Client = s3Client;
        this.fileConversionService = fileConversionService;
    }

    public void uploadFile(FileType fileType, String fileName, byte[] data){
        if (fileType == null || fileType == null || data == null){
            log.warn("s3Service, upload file, sth is null:( {} | {} | {}",fileType,fileName,data);
            throw new IllegalArgumentException("Illegal upload request!");
        }

        String fullS3key = fileType.getFolder() + "/" + fileName;
        String bucket = s3Config.getBucketName();

        log.info("S3Service, uploadFile, fullS3key = {}",fullS3key);
        log.info("S3Service, uploadFile, bucket = {}",bucket);
        log.info("S3Service, uploadFile, fileSize = {}",data.length);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fullS3key)
                .contentType(fileType.getContentType())
                .build();

        s3Client.putObject(request,RequestBody.fromBytes(data));
    }

    public byte[] downloadFileFromS3(FileType fileType, String key){
        if (key == null){
            log.info("s3 service, downloadFILEfromS3 klucz jest pusty!!!!!");
        }

        String fullS3key = fileType.getFolder() + "/" + key;

        log.info("s3 service, downloadFILEfromS3 fullS3key= {}",fullS3key);

        ResponseBytes<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(s3Config.getBucketName())
                        .key(fullS3key)
                        .build(),
                ResponseTransformer.toBytes()
        );

        return response.asByteArray();
    }

    public void deleteFileFromS3(FileType fileType,String key){
        if (key == null){
            log.info("s3 service, deleteFileFromS3 klucz jest pusty!!!!!");
        }

        String fullS3key = fileType.getFolder() + "/" + key;

        log.info("s3 service, deleteFileFromS3 fullS3key= {}",fullS3key);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(fullS3key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
