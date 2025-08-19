package pl.eurokawa.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.storage.S3Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    S3Service s3Service;

    @InjectMocks
    FileServiceImpl service;

    @Test
    void uploadFile_workingTest() throws IOException {
        FileType fileType = FileType.PHOTO;
        byte[] data = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);
        String remoteFileName = "test";

        byte[] uploadFile = service.uploadFile(fileType, inputStream, remoteFileName);

        assertArrayEquals(data,uploadFile);
    }

    @Test
    void uploadFile_FileTypeIsNull() throws IOException {
        FileType fileType = null;
        byte[] data = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);
        String remoteFileName = "test";

        assertThrows(NullPointerException.class,() -> service.uploadFile(fileType,inputStream,remoteFileName));
    }

    @Test
    void uploadFile_InputStreamIsNull() throws IOException {
        FileType fileType = FileType.PHOTO;
        InputStream inputStream = null;
        String remoteFileName = "test";

        assertThrows(NullPointerException.class,() -> service.uploadFile(fileType,inputStream,remoteFileName));
    }

    @Test
    void uploadFile_FileNameIsNull() throws IOException {
        FileType fileType = FileType.PHOTO;
        byte[] data = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);
        String remoteFileName = null;

        assertThrows(NullPointerException.class,() -> service.uploadFile(fileType,inputStream,remoteFileName));
    }

    @Test
    void uploadFile_s3ServiceWorkingTest() throws IOException {
        FileType fileType = FileType.PHOTO;
        byte[] data = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);
        String remoteFileName = "test";

        service.uploadFile(fileType,inputStream,remoteFileName);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);

        verify(s3Service).uploadFileToS3(eq(fileType),eq(remoteFileName),captor.capture());

        assertArrayEquals(data,captor.getValue());
    }

    @Test
    void uploadFile_ExceptionThrow() throws IOException {
        FileType fileType = FileType.PHOTO;
        String remoteFileName = "test";
        byte[] data = "data".getBytes();

        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Error");
            }
        };

        assertThrows(IOException.class,() -> service.uploadFile(fileType,inputStream,remoteFileName));
        verify(s3Service,never()).uploadFileToS3(fileType,remoteFileName,data);
    }

    @Test
    void deleteFile_workingTest(){
        FileType fileType = FileType.TERMS;
        String fileName = "test";

        service.deleteFile(fileType,fileName);
        verify(s3Service,times(1)).deleteFileFromS3(fileType,fileName);
    }

    @Test
    void deleteFile_FileTypeIsNull(){
        FileType fileType = null;
        String fileName = "test";

        assertThrows(NullPointerException.class,() -> service.deleteFile(fileType,fileName));
    }

    @Test
    void deleteFile_FileNameIsNull(){
        FileType fileType = FileType.TERMS;
        String fileName = null;

        assertThrows(NullPointerException.class,() -> service.deleteFile(fileType,fileName));
    }

    @Test
    void getFile_workingTest(){
        FileType fileType = FileType.TERMS;
        String fileName = "test";
        byte[] file = "data".getBytes();
        when(s3Service.downloadFileFromS3(fileType,fileName)).thenReturn(file);

        byte[] downloadedFile = service.getFile(fileType, fileName);

        assertArrayEquals(file,downloadedFile);
    }

    @Test
    void getFile_FileTypeIsNull(){
        FileType fileType = null;
        String fileName = "test";

        assertThrows(NullPointerException.class,() -> service.getFile(fileType,fileName));
        verify(s3Service,never()).downloadFileFromS3(fileType,fileName);
    }

    @Test
    void getFile_FileNameIsNull(){
        FileType fileType = FileType.PHOTO;
        String fileName = null;

        assertThrows(NullPointerException.class,() -> service.getFile(fileType,fileName));
        verify(s3Service,never()).downloadFileFromS3(fileType,fileName);
    }
}