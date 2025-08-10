package pl.eurokawa.file.conversion;

import org.springframework.stereotype.Service;
import pl.eurokawa.exception.ArgumentNullChecker;

import java.util.UUID;

@Service
public class NameConversionServiceImpl implements NameConversionService {

    @Override
    public String generateSecureFileName(String fileName) {
        ArgumentNullChecker.check(fileName,"File name");

        return randomizeName(normalizeName(fileName));
    }

    private String normalizeName(String fileName){
        ArgumentNullChecker.check(fileName,"File name");

        return fileName
                .toLowerCase()
                .replaceAll("[\\s]","_")
                .replaceAll("[^a-zA-Z0-9._-]","");
    }

    private String randomizeName(String fileName){
        ArgumentNullChecker.check(fileName,"File name");

        return UUID.randomUUID() + "_" + fileName;
    }
}
