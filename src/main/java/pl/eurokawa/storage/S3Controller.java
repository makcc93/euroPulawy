package pl.eurokawa.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.eurokawa.purchase.Purchase;
import pl.eurokawa.purchase.PurchaseRepository;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;

@RestController
@RequestMapping("/s3")
public class S3Controller {
    private static final Logger log = LogManager.getLogger(S3Controller.class);
    private final S3Service s3Service;
    private final PurchaseRepository purchaseRepository;

    @Autowired
    public S3Controller(S3Service s3Service, PurchaseRepository purchaseRepository){
        this.s3Service = s3Service;
        this.purchaseRepository = purchaseRepository;
    }

    @GetMapping("/download/{folderName}/{fileKey:.+}")
    public ResponseEntity<byte[]> getFile(@PathVariable String folderName,@PathVariable String fileKey){
        log.info("-_-_-_-_-_-_s3Controller, getmapping, filekey = {}", fileKey);

        FileType fileType = FileType.findTypeByFolderName(folderName);

        byte[] fileBytes = s3Service.downloadFileFromS3(fileType,fileKey);

        String mimeType = URLConnection.guessContentTypeFromName(fileKey);

        if (mimeType == null){
            mimeType = "application/octet-stream";
        }

        log.info("mimetype = {}, size = {}",mimeType,fileBytes.length);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileKey + "\"")
                .body(fileBytes);
    }

    @PutMapping("/upload/{folderName}/{fileKey:.+}")
    public ResponseEntity<String> uploadFile(@PathVariable String folderName,@PathVariable String fileKey, @RequestParam("file") MultipartFile file) throws IOException {
        log.info("putmapping, file = {}, mime = {}, size = {}, folderName = {}",file.getOriginalFilename(),file.getContentType(),file.getSize(),folderName);
        try{
            FileType fileType = FileType.findTypeByFolderName(folderName);
            s3Service.uploadFile(fileType,fileKey,file.getBytes());

            log.info("""
                SUCCESS S3Controller PutMapping, 
                fileType = {}
                file = {}
                mime = {}
                size = {}
                """,
                    fileType,file.getOriginalFilename(),file.getContentType(),file.getSize());
        }
        catch (NoSuchKeyException keyException){
            log.warn("S3Controller PutMapping wrong key ", keyException);
            return ResponseEntity.notFound().build();
        }
        catch (S3Exception s3Exception){
            log.warn("S3Controller PutMapping s3 problem ", s3Exception);
        }

        return ResponseEntity.ok().body("File " + fileKey + " uploaded successfully");
    }

    @DeleteMapping("/delete/{folderName}/{fileKey:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable String folderName,@PathVariable String fileKey){
        try {
            FileType fileType = FileType.findTypeByFolderName(folderName);
            s3Service.deleteFileFromS3(fileType, fileKey);
            log.info("DELETE SUCCESS, S3Controller DeleteMapping, fileType = {}, fileKey = {}",fileType,fileKey);
        }
        catch (NoSuchKeyException keyException){
            log.warn("S3Controller DeleteMapping wrong key ", keyException);
            return ResponseEntity.notFound().build();
        }
        catch (S3Exception s3Exception){
            log.warn("S3Controller DeleteMapping s3 problem ", s3Exception);
        }

        return ResponseEntity.ok().body("File " + fileKey + " deleted successfully");
    }
}
