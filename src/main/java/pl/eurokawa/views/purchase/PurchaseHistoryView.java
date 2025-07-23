package pl.eurokawa.views.purchase;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Data;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import pl.eurokawa.other.DateFormatter;
import pl.eurokawa.purchase.Purchase;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.purchase.PurchaseService;
import pl.eurokawa.storage.FileType;
import pl.eurokawa.storage.S3Service;
import pl.eurokawa.user.UserType;

import java.util.List;

@UIScope
@Data
@PageTitle("Historia zakupów")
@Route("orderhistory")
@Menu(order = 1.1, icon = LineAwesomeIconUrl.GRIP_LINES_SOLID)
public class PurchaseHistoryView extends Div implements BeforeEnterObserver {
    private PurchaseService purchaseService;
    private final SecurityService securityService;
    private final S3Service s3Service;
    private final DateFormatter dateFormatter;

    public PurchaseHistoryView(PurchaseService purchaseService, SecurityService securityService, S3Service s3Service, DateFormatter dateFormatter){
        this.purchaseService = purchaseService;
        this.securityService = securityService;
        this.s3Service = s3Service;
        this.dateFormatter = dateFormatter;

        List<Purchase> purchases = purchaseService.getConfirmedPurchases();

        Grid<Purchase> grid = new Grid<>(Purchase.class,false);
        grid.setItems(purchases);
        grid.setAllRowsVisible(true);

        grid.addColumn(Purchase::getId).setHeader("NR").setWidth("50px");

        grid.addColumn(Purchase::getUser).setHeader("OSOBA").setAutoWidth(true);

        grid.addColumn(Purchase::getProduct).setHeader("PRODUKT").setAutoWidth(true);

        grid.addColumn(Purchase::getQuantity).setHeader("ILOŚĆ").setWidth("50px");

        grid.addColumn(Purchase::getPrice).setHeader("CENA").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(purchase -> new Span(dateFormatter.formatToEuropeWarsaw(String.valueOf(purchase.getCreatedAt())))))
                .setHeader("DATA").setAutoWidth(true);

        grid.addColumn(Purchase::getTotal).setHeader("WARTOŚĆ").setWidth("50px");

        grid.addColumn(new ComponentRenderer<>(purchase -> purchaseService.getPurchasePhoto(FileType.PHOTO,purchase,s3Service)))
                .setHeader("DOWÓD ZAKUPU").setAutoWidth(true);

        add(grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event){
        if (!(securityService.loggedUserHasRole(UserType.ADMIN.name()) || securityService.loggedUserHasRole(UserType.USER.name()))){
            Notification notification = Notification.show("Twoje uprawnienia nie pozwają korzystać z tej zakładki!", 3000, Notification.Position.BOTTOM_CENTER);

            add(notification);
            event.rerouteTo("home");
        }
    }
}
