package pl.eurokawa.views.purchase;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import pl.eurokawa.balance.BalanceBroadcaster;
import pl.eurokawa.balance.BalanceService;
import pl.eurokawa.other.DateFormatter;
import pl.eurokawa.purchase.Purchase;
import pl.eurokawa.purchase.PurchaseRepository;
import pl.eurokawa.purchase.PurchaseService;
import pl.eurokawa.storage.FileType;
import pl.eurokawa.storage.S3Service;
import pl.eurokawa.transaction.Transaction;
import pl.eurokawa.transaction.TransactionRepository;
import pl.eurokawa.transaction.TransactionType;

import java.util.ArrayList;
import java.util.List;

@Route("purchase-confirmation")
@RolesAllowed("ADMIN")
@Menu(order = 3, icon = LineAwesomeIconUrl.CHECK_CIRCLE)
public class PurchaseConfirmationView extends Div {

    private final PurchaseService purchaseService;
    private final BalanceService balanceService;
    private final PurchaseRepository purchaseRepository;
    private final TransactionRepository transactionRepository;
    private final S3Service s3Service;
    private List<Purchase> purchases = new ArrayList<>();
    private final ListDataProvider<Purchase> dataProvider;
    private final DateFormatter dateFormatter;

    public PurchaseConfirmationView(PurchaseService purchaseService, BalanceService balanceService, PurchaseRepository purchaseRepository, TransactionRepository transactionRepository, S3Service s3Service, DateFormatter dateFormatter) {
        this.purchaseService = purchaseService;
        this.balanceService = balanceService;
        this.purchaseRepository = purchaseRepository;
        this.transactionRepository = transactionRepository;
        this.purchases = purchaseService.getSavedNotConfirmedPurchases();
        this.s3Service = s3Service;
        this.dateFormatter = dateFormatter;

        Grid<Purchase> grid = new Grid<> (Purchase.class,false);
        dataProvider = new ListDataProvider<>(purchases);

        grid.setItems(dataProvider);
        grid.setAllRowsVisible(true);

        grid.addColumn(Purchase::getId).setHeader("NR").setWidth("50px");

        grid.addColumn(Purchase::getUser).setHeader("OSOBA").setAutoWidth(true);

        grid.addColumn(Purchase::getProduct).setHeader("PRODUKT").setAutoWidth(true);

        grid.addColumn(Purchase::getQuantity).setHeader("ILOŚĆ").setWidth("50px");

        grid.addColumn(Purchase::getPrice).setHeader("CENA").setWidth("50px");

        grid.addColumn(new ComponentRenderer<>(purchase -> new Span(dateFormatter.formatToEuropeWarsaw(String.valueOf(purchase.getCreatedAt()))))).setHeader("DATA")
                .setAutoWidth(true);

        grid.addColumn(Purchase::getTotal).setHeader("WARTOŚĆ").setWidth("50px");

        grid.addColumn(new ComponentRenderer<>(purchase -> purchaseService.getPurchasePhoto(FileType.PHOTO,purchase,s3Service)))
                .setHeader("DOWÓD ZAKUPU").setAutoWidth(true);

        createActionButtons(grid);

        add(grid);
    }

    private void createActionButtons(Grid<Purchase> grid) {
        grid.addColumn(new ComponentRenderer<>(purchase ->{
            Button save = new Button(new Icon(VaadinIcon.CHECK));
            save.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
            save.setTooltipText("Zatwierdź");
            save.addClickListener(click -> {
                purchase.setConfirmed(true);
                purchaseRepository.save(purchase);
                Transaction transaction = new Transaction(purchase.getUser(), TransactionType.CHECKOUT,purchase.getTotal());

                transactionRepository.save(transaction);

                balanceService.updateBalance(transaction.getUser(),transaction.getAmount(),transaction.getType());

                BalanceBroadcaster.broadcast(balanceService.getCurrentBalance());

                purchases.remove(purchase);
                dataProvider.refreshAll();
            });

            Button delete = new Button(new Icon(VaadinIcon.TRASH));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_CONTRAST);
            delete.setTooltipText("Usuń zamówienie");
            delete.addClickListener(click ->{
                purchase.setConfirmed(false);
                purchase.setSaved(false);

                purchaseRepository.delete(purchase);
                purchases.remove(purchase);
                s3Service.deleteFileFromS3(FileType.PHOTO,purchase.getReceiptImagePath());
                purchase.setReceiptImagePath(null);

                dataProvider.refreshAll();
            });

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.add(save,delete);

            return horizontalLayout;
        })).setHeader("Akcje").setAutoWidth(true);
    }
}
