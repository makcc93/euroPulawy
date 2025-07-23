package pl.eurokawa.storage;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileMimeTypeService {

    public String getMimeTypeFromBytes(InputStream inputStream) throws IOException {
        Tika tika = new Tika();
        return tika.detect(inputStream);
    }
}
