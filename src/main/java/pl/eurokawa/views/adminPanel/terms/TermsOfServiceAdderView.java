package pl.eurokawa.views.adminPanel.terms;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.storage.FileAdderService;
import pl.eurokawa.storage.FileConversionService;
import pl.eurokawa.storage.FileType;
import pl.eurokawa.terms.TermsOfService;
import pl.eurokawa.terms.TermsOfServiceRepository;

import java.io.InputStream;

@Route("terms-adder")
@RolesAllowed("ADMIN")
public class TermsOfServiceAdderView extends Div {

    private final FileAdderService fileAdderService;
    private final SecurityService securityService;
    private final TermsOfServiceRepository termsOfServiceRepository;
    private final FileConversionService fileConversionService;

    public TermsOfServiceAdderView(FileAdderService fileAdderService, SecurityService securityService, TermsOfServiceRepository termsOfServiceRepository, FileConversionService fileConversionService){
        this.fileAdderService = fileAdderService;
        this.securityService = securityService;
        this.termsOfServiceRepository = termsOfServiceRepository;
        this.fileConversionService = fileConversionService;

        VerticalLayout layout = new VerticalLayout();
        H2 header = new H2("DODAWANIE NOWEGO REGULAMINU");
        header.getStyle()
                .set("text-align", "center")
                .set("font-size", "72px")
                .set("margin-top", "auto");

        MemoryBuffer memoryBuffer = new MemoryBuffer();

        Upload upload = new Upload(memoryBuffer);
        proceedUpload(upload,memoryBuffer);

        layout.add(header,upload);

        add(layout);
    }

    private void proceedUpload(Upload upload,MemoryBuffer memoryBuffer){
        upload.setHeight("200px");
        upload.setWidth("300px");
        upload.setAcceptedFileTypes(".pdf");
        upload.setMaxFileSize(10*1024*1024);
        upload.setMaxFiles(1);

        upload.addSucceededListener(succeededEvent ->{
            try(InputStream inputStream = memoryBuffer.getInputStream()){
                String secureFileName = fileConversionService.generateSecureFileName(succeededEvent.getFileName());

                TermsOfService termsOfService = new TermsOfService(secureFileName,securityService.getLoggedUser());
                termsOfServiceRepository.save(termsOfService);

                fileAdderService.uploadFile(FileType.TERMS,inputStream,secureFileName);
                Notification.show("Regulamin dodany prawidlowo",5000, Notification.Position.BOTTOM_CENTER);

                upload.setVisible(false);
            }
            catch (Exception e){
                Notification.show(e.toString(),5000, Notification.Position.BOTTOM_CENTER);

                throw new IllegalArgumentException("InputStream is wrong!");
            }
        });

        upload.addFileRejectedListener(fileRejectedEvent -> {
            Notification.show("Błąd dodawania pliku, sprawdź plik oraz jego format",5000, Notification.Position.BOTTOM_CENTER);

            throw new IllegalArgumentException("Upload faileeeeeeeeeeeeeed!");
        });
    }
}
