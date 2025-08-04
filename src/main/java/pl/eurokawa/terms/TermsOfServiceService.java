package pl.eurokawa.terms;

import com.vaadin.flow.component.Component;
import pl.eurokawa.file.FileType;
import pl.eurokawa.storage.S3Service;

import java.util.List;

public interface TermsOfServiceService {
    Component getTermsOfServiceLink(FileType fileType, TermsOfService termsOfService, S3Service s3Service);
    Component getTermsOfServiceLink(FileType fileType, TermsOfService termsOfService, S3Service s3Service, String message);
    TermsOfService findCurrentActual();
    List<TermsOfService> termsOfServiceList();
}
