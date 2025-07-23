package pl.eurokawa.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    private String accessKey;

    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    private final String controllerDownloadPrefix = "/s3/download/";

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getControllerDownloadPrefix() {
        return controllerDownloadPrefix;
    }

    @Bean
    public S3Client s3Client(){
//        AwsBasicCredentials basicCredentials = AwsBasicCredentials.create(accessKey,secretKey);

        return S3Client.builder()
                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .build();
    }
}
