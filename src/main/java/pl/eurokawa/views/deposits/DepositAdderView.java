package pl.eurokawa.views.deposits;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Data;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import pl.eurokawa.product.ProductService;
import pl.eurokawa.purchase.PurchaseRepository;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.transaction.Transaction;
import pl.eurokawa.transaction.TransactionRepository;
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserRepository;
import pl.eurokawa.user.UserService;
import pl.eurokawa.user.UserType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@UIScope
@Data
@PageTitle("Wpłaty")
@Route("deposit")
@Menu(order = 2, icon = LineAwesomeIconUrl.DOLLAR_SIGN_SOLID)
public class DepositAdderView extends VerticalLayout implements BeforeEnterObserver {
    private final TransactionRepository transactionRepository;
    private final SecurityService securityService;
    private final ProductService productService;
    private final PurchaseRepository purchaseRepository;
    private final UserService userService;
    private final UserRepository userRepository;
//    private MoneyService moneyService;
//    private final MoneyRepository moneyRepository;
    private final Grid<Transaction> grid;
    private final ListDataProvider<Transaction> dataProvider;
    private final List<Transaction> transactions = new ArrayList<>();

    public DepositAdderView(TransactionRepository transactionRepository, SecurityService securityService, ProductService productService,
                            PurchaseRepository purchaseRepository, UserService userService,
                            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.securityService = securityService;
        this.productService = productService;
        this.purchaseRepository = purchaseRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        dataProvider = new ListDataProvider<>(transactions);

        grid = new Grid<> (Transaction.class,false);
        grid.setDataProvider(dataProvider);
        grid.setAllRowsVisible(true);
//        grid.setVisible(securityService.loggedUserHasRole("ADMIN"));
        
//        if (!securityService.loggedUserHasRole("ADMIN")){
//
//            TextArea information = new TextArea();
//            information.setValue("""
//            Nie posiadasz dostępu do dodawania wpłat, aby zobaczyć ich historię przejdź do zakładki niżej \"Historia wpłat\".
//            Jeśli dokonałeś wpłaty, ale jej nie widzisz zgłoś ten fakt skarbnikowi.
//            """);
//            information.setWidthFull();
//            information.setAutofocus(true);
//            information.setHeightFull();
//            information.setReadOnly(true);
//            information.getStyle().set("font-size","48px");
//
//            add(information);
//        }

        createUserColumn(grid);
        createDepositColumn(grid);
        createActionsButtons(grid);

        addEmptyRow(grid);
        
        add(grid);
    }

    private void addEmptyRow(Grid<Transaction> grid) {
        Transaction transaction = new Transaction();

        transactions.add(transaction);
        refreshGrid(grid);
    }

    private void createUserColumn(Grid<Transaction> grid){
        grid.addColumn(new ComponentRenderer<>(transaction ->{
            ComboBox<User> comboBox = new ComboBox<>();
            comboBox.setItemLabelGenerator(User::toString);
            comboBox.setClearButtonVisible(true);
            comboBox.setTooltipText("Wybierz osobę");

            if (!securityService.getLoggedUser().getRole().equals(UserType.ADMIN.name())){
                comboBox.setItems(securityService.getLoggedUser());
                comboBox.setReadOnly(true);
            }
            else {
                comboBox.setItems(userRepository.findOnlyConfirmedUsers());
                comboBox.setReadOnly(false);
            }

            comboBox.addValueChangeListener(event -> {
                User selectedUser = event.getValue();
                if (selectedUser != null) {
                    transaction.setUser(selectedUser);
                }
            });

            return comboBox;
        })).setHeader("OSOBA").setAutoWidth(true);
    }

    private void createDepositColumn(Grid<Transaction> grid){
        grid.addColumn(new ComponentRenderer<>(transaction ->{
            TextField depositField = new TextField();
            depositField.setPlaceholder("Wprowadź wpłatę");
            depositField.setClearButtonVisible(true);

            depositField.addValueChangeListener(event ->{
                String value = event.getValue().replace(",",".");

                depositField.setValue(value);
                try{
                    BigDecimal amount = new BigDecimal(value);
                    if (amount.compareTo(BigDecimal.ZERO) < 0.00) {
                        Notification.show("Cena nie może być ujemna!",3000, Notification.Position.MIDDLE);
                    }
                    else {
                        transaction.setAmount(amount);
                    }
                }
                catch (NumberFormatException e) {
                    Notification n = Notification.show("Wprowadź poprawną cenę");
                    n.setPosition(Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            return depositField;

        })).setHeader("WPŁATA").setAutoWidth(true);
    }

    private void createActionsButtons(Grid<Transaction> grid){
        grid.addColumn(new ComponentRenderer<>(transaction ->{
            Button save = new Button(new Icon(VaadinIcon.CHECK));
            save.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
            save.setTooltipText("Zapisz");

            save.addClickListener(event ->{
                transaction.setType(TransactionType.DEPOSIT);
                transaction.setSaved(true);
                transaction.setConfirmed(false);
                transactionRepository.save(transaction);

                Notification.show("Twoja wpłata została poprawnie zarejestrowana.",3000, Notification.Position.MIDDLE);

                transactions.add(transaction);

                refreshGrid(grid);
            });

            Button reset = new Button(new Icon(VaadinIcon.ARROWS_CROSS));
            reset.addThemeVariants(ButtonVariant.LUMO_WARNING,ButtonVariant.LUMO_PRIMARY);
            reset.setTooltipText("Resetuj");

            reset.addClickListener(event ->{
                transactionRepository.delete(transaction);
                transactions.remove(transaction);

                refreshGrid(grid);
            });

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.add(save);

            return horizontalLayout;
        })).setHeader("AKCJE");
    }

    private void refreshGrid(Grid<Transaction> grid) {
        grid.getDataProvider().refreshAll();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        User loggedUser = securityService.getLoggedUser();

        if (!securityService.hasAccessToCoffee(loggedUser)){
            beforeEnterEvent.rerouteTo("home");
        }
    }
}
