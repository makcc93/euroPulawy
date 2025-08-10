package pl.eurokawa.file;

import org.apache.commons.validator.Arg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Test
    void uploadFile_workingTest() throws IOException {
        FileType fileType = FileType.PHOTO;
        byte[] data = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);
        String remoteFileName = "test";

        FileServiceImpl mock = mock(FileServiceImpl.class);

        mock.uploadFile(fileType,inputStream,remoteFileName);
        ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass(InputStream.class);
        verify(mock).uploadFile(eq(fileType),captor.capture(),eq(remoteFileName));

        assertArrayEquals(data,captor.getValue().readAllBytes());
    }

    @Test
    void uploadFile_FileTypeIsNull() throws IOException {
        FileType fileType = null;
        byte[] data = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);
        String remoteFileName = "test";

        FileServiceImpl fileService = new FileServiceImpl(s3Service);
        assertThrows(NullPointerException.class,() -> fileService.uploadFile(fileType,inputStream,remoteFileName));
    }

    @Test
    void uploadFile_InputStreamIsNull() throws IOException {
        FileType fileType = FileType.PHOTO;
        InputStream inputStream = null;
        String remoteFileName = "test";

        FileServiceImpl fileService = new FileServiceImpl(s3Service);
        assertThrows(NullPointerException.class,() -> fileService.uploadFile(fileType,inputStream,remoteFileName));
    }

    @Test
    void uploadFile_FileNameIsNull() throws IOException {
        FileType fileType = FileType.PHOTO;
        byte[] data = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);
        String remoteFileName = null;

        FileServiceImpl fileService = new FileServiceImpl(s3Service);
        assertThrows(NullPointerException.class,() -> fileService.uploadFile(fileType,inputStream,remoteFileName));
    }

    @Test
    void uploadFile_s3ServiceWorkingTest() throws IOException {
        FileType fileType = FileType.PHOTO;
        byte[] data = "test data".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);
        String remoteFileName = "test";

        FileServiceImpl fileService = new FileServiceImpl(s3Service);
        fileService.uploadFile(fileType,inputStream,remoteFileName);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);

        verify(s3Service).uploadFileToS3(eq(fileType),eq(remoteFileName),captor.capture());

        assertArrayEquals(data,captor.getValue());
    }
}