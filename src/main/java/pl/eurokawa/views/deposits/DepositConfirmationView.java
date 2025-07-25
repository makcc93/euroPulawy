package pl.eurokawa.views.deposits;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import pl.eurokawa.balance.BalanceBroadcaster;
import pl.eurokawa.balance.BalanceServiceImpl;
import pl.eurokawa.other.DateFormatter;
import pl.eurokawa.transaction.Transaction;
import pl.eurokawa.transaction.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

@Route("depositConfirmation")
@RolesAllowed("ADMIN")
@Menu(order = 2, icon = LineAwesomeIconUrl.CHECK_CIRCLE)
public class DepositConfirmationView extends Div {
    private final Grid<Transaction> grid;
    private final ListDataProvider<Transaction> dataProvider;
    private List<Transaction> notConfirmedTransactions = new ArrayList<>();
    private final TransactionRepository transactionRepository;
    private final DateFormatter dateFormatter;
    private final BalanceServiceImpl balanceServiceImpl;

    public DepositConfirmationView(TransactionRepository transactionRepository, DateFormatter dateFormatter, BalanceServiceImpl balanceServiceImpl){
        this.transactionRepository = transactionRepository;
        this.notConfirmedTransactions = transactionRepository.findAllSavedNotConfirmedTransactions();
        this.dateFormatter = dateFormatter;
        this.balanceServiceImpl = balanceServiceImpl;

        dataProvider = new ListDataProvider<>(notConfirmedTransactions);
        grid = new Grid<>(Transaction.class,false);
        grid.setItems(dataProvider);
        grid.setAllRowsVisible(true);

        grid.addColumn(Transaction::getId).setHeader("NR").setAutoWidth(true);
        grid.addColumn(Transaction::getUser).setHeader("OSOBA").setAutoWidth(true);
        grid.addColumn(Transaction::getAmount).setHeader("WPŁATA").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(transaction ->
                new Span(dateFormatter.formatToEuropeWarsaw(String.valueOf(transaction.getCreatedAt())))))
        .setHeader("DATA").setAutoWidth(true);

        createActionButtons(grid);

        add(grid);
    }

    private void createActionButtons(Grid<Transaction> grid){
        grid.addColumn(new ComponentRenderer<>(transaction -> {
            Button save = new Button(new Icon(VaadinIcon.CHECK));
            save.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
            save.setTooltipText("Zatwierdź");
            save.addClickListener(event ->{
                transaction.setConfirmed(true);

                transactionRepository.save(transaction);

                balanceServiceImpl.updateBalance(transaction.getUser(),transaction.getAmount(),transaction.getType());

                BalanceBroadcaster.broadcast(balanceServiceImpl.getCurrentBalance());

                notConfirmedTransactions.remove(transaction);

                dataProvider.refreshAll();

                Notification.show("Wpłata użytkownika " + transaction.getUser().toString() + " w wysokości " + transaction.getAmount() + " została poprawnie zarejestrowana!", 5000, Notification.Position.MIDDLE);
            });

            Button delete = new Button(new Icon(VaadinIcon.TRASH));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_CONTRAST);
            delete.setTooltipText("Usuń");
            delete.addClickListener(event ->{
                transactionRepository.delete(transaction);

                notConfirmedTransactions.remove(transaction);

                dataProvider.refreshAll();

                Notification.show("Wpłata użytkownika " + transaction.getUser().toString() + " w wysokości " + transaction.getAmount() + " nie została zatwierdzona!", 5000, Notification.Position.MIDDLE);

            });

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.add(save,delete);

            return horizontalLayout;
        }));

    }

}
