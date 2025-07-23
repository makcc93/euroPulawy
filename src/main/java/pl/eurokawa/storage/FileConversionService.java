package pl.eurokawa.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileConversionService {

    private static final Logger log = LogManager.getLogger(FileConversionService.class);

    public byte[] convertPngToJpg(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException("Input Stream can not be null!");
        }

        BufferedImage image = ImageIO.read(inputStream);
        BufferedImage newImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics2D = newImage.createGraphics();
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(newImage, "jpg", byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    public byte[] convertFileToJpegIfNeeded(String mimeType, byte[] data, String fileName) throws IOException, InterruptedException {
        log.info("""
            SpRaWdZaM czy potrzeba konwersji dla,
            fileName = {},
            mime = {},
            """,
                fileName,
                mimeType);

        if (!mimeType.matches(".*(jpg|jpeg).*") && !isRealJpg(data)){
            log.info("Nie jest jpeg/jpg (lub udwany), rozpoczynam konwersję do JPG");

            String extension = mimeType.toLowerCase().replaceAll(".*/", "");
            Path inputPath = Files.createTempFile("input", "." + extension);

            Path outputPath = Files.createTempFile("output", ".jpeg");

            log.info("JEEEEEEEEESZCZE przed try, extension = {},\n input = {},\n output = {}",extension,inputPath,outputPath);

            try {
                Files.write(inputPath, data);
                log.info("_________ rozmiar pliku input = {} bajtow",Files.size(inputPath));

                ProcessBuilder pb = new ProcessBuilder(
                        "magick",

                        inputPath.toString(),
                        "-auto-orient",
                        "-strip",
                        "-quality", "85",
                        "-colorspace","sRGB",

                        outputPath.toString()
                );
                pb.redirectErrorStream(false);

                Process process = pb.start();

                StringBuilder realErrors = new StringBuilder();
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))){
                    String line;
                    while ((line = errorReader.readLine()) != null){
                        if (!line.contains("warning/profile.c/ValidateXMPProfile")) {
                            realErrors.append(line).append("\n");
                            log.error("ImageMagick real error: {}",line);
                        }
                        else {
                            log.debug("ImageMagick ignored warning = {}",line);
                        }
                    }
                }

                int exitCode = process.waitFor();
                File outputFile = new File(outputPath.toString());

                if (exitCode == 0 && outputFile.exists() && outputFile.length() > 0) {
                    log.info("Konwersja udana, kod 0 = {}, rozmiar output = {}",exitCode,outputFile.length());
                } else {
                    throw new RuntimeException("Konwersja nie udana");
                }

                return Files.readAllBytes(outputPath);
            } catch (InterruptedException e) {
                throw new RuntimeException("Konwersja przerwana ", e);
            }
             finally {
                Files.deleteIfExists(inputPath);
                Files.deleteIfExists(outputPath);
            }
        }

        return data;
    }

    private boolean isRealJpg(byte[]data){
        if (data == null || data.length < 4){
            return false;
        }

        return  data[0] == (byte) 0xFF &&
                data[1] == (byte) 0xD8 &&
                data[2] == (byte) 0xFF;
    }

    public String generateSecureFileName(String fileName){

        return randomizeName(normalizeName(fileName));
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
