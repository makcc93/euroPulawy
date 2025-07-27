package pl.eurokawa.file.name;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NameConversionServiceImpl implements NameConversionService {

    @Override
    public String generateSecureFileName(String input) {

        return randomizeName(normalizeName(input));
    }

    private String normalizeName(String fileName){
        return fileName
                .toLowerCase()
                .replaceAll("[\\s]","_")
                .replaceAll("[^a-zA-Z0-9._-]","");
    }

    private String randomizeName(String fileName){
        return UUID.randomUUID() + "_" + fileName;
    }
}
