package pl.eurokawa.views.adminPanel.terms;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.eurokawa.other.DateFormatter;
import pl.eurokawa.storage.FileType;
import pl.eurokawa.storage.S3Service;
import pl.eurokawa.terms.TermsOfService;
import pl.eurokawa.terms.TermsOfServiceRepository;
import pl.eurokawa.terms.TermsOfServiceService;

import java.util.ArrayList;
import java.util.List;

@Route("terms-list")
public class TermsOfServiceListView extends Div {
    private static final Logger log = LoggerFactory.getLogger(TermsOfServiceListView.class);
    private final TermsOfServiceRepository termsOfServiceRepository;
    private final DateFormatter dateFormatter;
    private final TermsOfServiceService termsOfServiceService;
    private List<TermsOfService> termsOfServiceList = new ArrayList<>();
    private final S3Service s3Service;
    private final ListDataProvider<TermsOfService> dataProvider;

    public TermsOfServiceListView(TermsOfServiceRepository termsOfServiceRepository, DateFormatter dateFormatter, TermsOfServiceService termsOfServiceService, S3Service s3Service){
        this.termsOfServiceRepository = termsOfServiceRepository;
        this.dateFormatter = dateFormatter;
        this.termsOfServiceService = termsOfServiceService;
        this.termsOfServiceList = termsOfServiceRepository.termsOfServiceList();
        this.s3Service = s3Service;

        dataProvider = new ListDataProvider<>(termsOfServiceList);
        Grid<TermsOfService> grid = new Grid<>(TermsOfService.class, false);
        grid.setDataProvider(dataProvider);
        createColumns(grid,s3Service,termsOfServiceRepository);

        grid.setItems(termsOfServiceList);
        grid.setAllRowsVisible(true);

        log.info("TermsOfServiceListView, rozmiar listy = {}",termsOfServiceList.size());

        add(grid);
    }


    private void createColumns(Grid<TermsOfService> grid,S3Service s3Service,TermsOfServiceRepository termsOfServiceRepository){
        grid.addColumn(TermsOfService::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(TermsOfService::getFileName).setHeader("SZYFROWANA NAZWA").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(termsOfService -> new Span(dateFormatter.formatToEuropeWarsaw(String.valueOf(termsOfService.getCreatedAt())))))
                .setHeader("DATA DODANIA").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(termsOfService -> termsOfServiceService.getTermsOfServiceLink(FileType.TERMS,termsOfService,s3Service))).setHeader("PODGLĄD REGULAMINU").setAutoWidth(true);
        createIsActualColumn(grid);
        createActionButtons(grid,termsOfServiceRepository);
    }

    private void createIsActualColumn(Grid<TermsOfService> grid){
            grid.addColumn(new ComponentRenderer<>(termsOfService -> {
                if (termsOfService.getActual()){
                    return new Icon(VaadinIcon.CHECK_SQUARE);
                }
                return new Icon(VaadinIcon.CLOSE_SMALL);
            })).setHeader("OBECNIE OBOWIĄZUJĄCY").setAutoWidth(true);
    }

    private void createActionButtons(Grid<TermsOfService> grid,TermsOfServiceRepository termsOfServiceRepository){
        grid.addColumn(new ComponentRenderer<>(termsOfService ->{
            Button setActualButton = new Button(new Icon(VaadinIcon.CHECK));
            setActualButton.setTooltipText("Ustaw jako obowiązujacy");

            setActualButton.addClickListener(clickEvent -> {
                List<TermsOfService> terms = termsOfServiceRepository.termsOfServiceList();
                for (TermsOfService term : terms){
                    term.setActual(false);
                    termsOfServiceRepository.save(term);
                }
                termsOfService.setActual(true);
                termsOfServiceRepository.save(termsOfService);

                refreshGrid(grid,termsOfServiceRepository);

                Notification.show("Poprawnie zmieniono aktualny regulamin!",5000, Notification.Position.MIDDLE);
            });

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setTooltipText("Usuń regulamin");

            deleteButton.addClickListener(clickEvent -> {
                termsOfServiceRepository.delete(termsOfService);
                s3Service.deleteFileFromS3(FileType.TERMS,termsOfService.getFileName());

                termsOfServiceList.remove(termsOfService);
                refreshGrid(grid,termsOfServiceRepository);


                Notification.show("Regulamin usunięty poprawnie.",5000, Notification.Position.MIDDLE);
            });

            HorizontalLayout layout = new HorizontalLayout();
            layout.add(setActualButton,deleteButton);

            return layout;
        }));
    }

    private void refreshGrid(Grid<TermsOfService> grid,TermsOfServiceRepository termsOfServiceRepository){
        grid.setDataProvider(new ListDataProvider<>(termsOfServiceRepository.termsOfServiceList()));
        grid.getDataProvider().refreshAll();
    }
}
