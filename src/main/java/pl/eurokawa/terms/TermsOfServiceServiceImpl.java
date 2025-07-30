package pl.eurokawa.terms;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.eurokawa.security.S3Config;
import pl.eurokawa.file.FileType;
import pl.eurokawa.storage.S3Service;

import java.util.List;

@Service
public class TermsOfServiceServiceImpl implements TermsOfServiceService{
    private static final Logger log = LoggerFactory.getLogger(TermsOfServiceServiceImpl.class);
    private final S3Config s3Config;
    private final S3Service s3Service;
    private final TermsOfServiceRepository termsOfServiceRepository;

    public TermsOfServiceServiceImpl(S3Config s3Config, S3Service s3Service, TermsOfServiceRepository termsOfServiceRepository) {
        this.s3Config = s3Config;
        this.s3Service = s3Service;
        this.termsOfServiceRepository = termsOfServiceRepository;
    }

    @Override
    public Component getTermsOfServiceLink(FileType fileType, TermsOfService termsOfService, S3Service s3Service){
        if (termsOfService.getFileName() == null){
            return new H4("Brak obrazu");
        }

        String url = generateUrl(fileType, termsOfService);

        s3Service.downloadFileFromS3(FileType.TERMS,termsOfService.getFileName());

        Anchor link = new Anchor(url,"Otwórz");
        link.setTarget("_blank");

        return link;
    }

    @Override
    public Component getTermsOfServiceLink(FileType fileType, TermsOfService termsOfService, S3Service s3Service, String message){
        if (termsOfService.getFileName() == null){
            return new H4("Brak obrazu");
        }

        String url = generateUrl(fileType, termsOfService);

        s3Service.downloadFileFromS3(FileType.TERMS,termsOfService.getFileName());

        Anchor link = new Anchor(url,message);
        link.setTarget("_blank");

        return link;
    }

    @Override
    public TermsOfService findCurrentActual() {
        return termsOfServiceRepository.findCurrentActual();
    }

    @Override
    public List<TermsOfService> termsOfServiceList() {
        return termsOfServiceRepository.termsOfServiceList();
    }

    private String generateUrl(FileType fileType,TermsOfService termsOfService){
        String folder = fileType.getFolder();
        String key = termsOfService.getFileName();
        log.info("TermsOfServiceService, getTermsOfServiceFile, folder = {},key = {}",folder,key);


        return s3Config.getControllerDownloadPrefix() + folder + "/" + key;
    }

    private Button closeDialogButton(Dialog dialog){
        Button button = new Button(VaadinIcon.CLOSE_CIRCLE.create());
        button.addClickListener(event ->{
            dialog.close();
        });

        dialog.add(button);

        button.getStyle()
                .set("position", "absolute")
                .set("top", "var(--lumo-space-s)")
                .set("right", "var(--lumo-space-s)")
                .set("z-index", "1")
                .set("padding", "0.5em")
                .set("min-width", "0");

        return button;
    }
}
