package pl.eurokawa.views.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import pl.eurokawa.purchase.PurchaseRepository;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.email.EmailService;
import pl.eurokawa.token.Token;
import pl.eurokawa.token.TokenRepository;
import pl.eurokawa.token.TokenService;
import pl.eurokawa.token.TokenType;
import pl.eurokawa.transaction.TransactionRepository;
import pl.eurokawa.user.UserRepository;
import pl.eurokawa.user.UserService;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserType;
import pl.eurokawa.views.layouts.MainLayout;

import java.math.BigDecimal;
import java.util.Optional;

@UIScope
@PageTitle("Ludzie")
@Route(value = "users/:peopleID?/:action?(edit)", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.ANKH_SOLID)
public class UserView extends Div implements BeforeEnterObserver {

    private final SecurityService securityService;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private static final Logger logger = LogManager.getLogger(UserView.class);

    private String PEOPLE_ID = "peopleID";
    private String PEOPLE_EDIT_ROUTE_TEMPLATE = "users/%s/edit";

    private final Grid<User> grid = new Grid<>(User.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private ComboBox<String> role;
    private ComboBox<Boolean> isCoffeeMember;
    private ComboBox<Boolean> emailConfirmed;
    private final Button cancel = new Button("Anuluj");
    private final Button save = new Button("Zapisz");
    private final Button delete = new Button("Usuń");

    private final BeanValidationBinder<User> binder;
    private User user;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;

    public UserView(SecurityService securityService, TokenService tokenService, TokenRepository tokenRepository, EmailService emailService, UserService userService, TransactionRepository transactionRepository, PurchaseRepository purchaseRepository, UserRepository userRepository) {
        this.securityService = securityService;
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;

        addClassNames("ludzie-view");
        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayoutForAdmin(splitLayout);

        binder = new BeanValidationBinder<>(User.class);
        binder.bindInstanceFields(this);

        add(splitLayout);

        createGridColumns(grid);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                populateForm(event.getValue());
                UI.getCurrent().navigate(String.format(PEOPLE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(UserView.class);
            }
        });

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.user != null) {
                    binder.writeBean(this.user);
                    userService.save(this.user);

                    refreshGrid();
                    clearForm();

                    Notification notification = Notification.show("Dane zaktualizowane pomyślnie");
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notification.setPosition(Position.MIDDLE);
                    UI.getCurrent().navigate(UserView.class);
                } else {
                    Notification notification = Notification.show("Błędne dane!", 3000, Position.BOTTOM_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

                    refreshGrid();
                    clearForm();
                }
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Błąd w aktualizacji danych. W miedzyczasie ktoś inny próbował aktualizować dane");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException | DataIntegrityViolationException validationException) {
                Notification n = Notification.show("Błąd w aktualizacji danych. Sprawdź czy wszystkie dane są poprawne");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.setVisible(securityService.loggedUserHasRole(UserType.ADMIN.name()));

        delete.addClickListener(e -> {
            if (this.user != null
                    && purchaseRepository.findUserConfirmedPurchases(this.user.getId()).isEmpty()
                    && (transactionRepository.findAllUserConfirmedTransactions(this.user.getId()).isEmpty()))
            {

                ConfirmDialog deleteConfirmDialog = new ConfirmDialog();
                deleteConfirmDialog.setHeader("USUNIĘCIE UŻYTKOWNIKA");
                deleteConfirmDialog.setText("Czy potwierdzasz całkowite i nieodwracalne usunięcie użytkownika " + this.user.toString() + "?");
                deleteConfirmDialog.setCancelable(true);
                deleteConfirmDialog.setCancelText("Anuluj");
                deleteConfirmDialog.addCancelListener(event -> {
                    deleteConfirmDialog.close();
                });

                deleteConfirmDialog.setConfirmText("Potwierdź");
                deleteConfirmDialog.addConfirmListener(event ->{
                    logger.info(",,,, delete, this user = {}", this.user.toString());
                    logger.info(",,,, delete, isEmpty = {}, sum = {}",purchaseRepository.findUserConfirmedPurchases(this.user.getId()).isEmpty(),transactionRepository.getSumOfUserDeposit(this.user.getId()));

                    try {
                        binder.writeBean(this.user);
                    } catch (ValidationException ex) {
                        throw new RuntimeException(ex);
                    }
                    userService.delete(this.user.getId());
                    clearForm();
                    refreshGrid();

                    Notification notification = Notification.show("Osoba została usunięta poprawnie");
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notification.setPosition(Position.MIDDLE);
                });

                deleteConfirmDialog.open();
            } else {
                logger.info(",,,, delete, else");
                Notification.show("Nie możesz usunąć osoby, która posiada wpłaty lub zakupy!", 3000, Position.TOP_CENTER);
            }

        });
        delete.setVisible(securityService.loggedUserHasRole(UserType.ADMIN.name()));
    }
    private void createGridColumns(Grid<User> grid){
        grid.addColumn("firstName").setAutoWidth(true).setHeader("IMIĘ");
        grid.addColumn("lastName").setAutoWidth(true).setHeader("NAZWISKO");
        grid.addColumn(user -> {
            BigDecimal sum = transactionRepository.getSumOfUserDeposit(user.getId());

            return sum != null ? String.format("%.2f", sum) : "0.00";
        }).setAutoWidth(true).setHeader("SUMA WPŁAT");

        if (securityService.loggedUserHasRole(UserType.ADMIN.name())){
            grid.setItems(userRepository.findAll());
        }
        else {
            grid.setItems(userRepository.findOnlyConfirmedUsers());
        }
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    }
    private void createEditorLayoutForAdmin(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");
        editorLayoutDiv.setVisible(securityService.loggedUserHasRole(UserType.ADMIN.name()));

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();

        firstName = new TextField("Imię");

        lastName = new TextField("Nazwisko");

        email = new TextField("Email");
        email.setPlaceholder("Wprowadź maila..");
        email.setEnabled(false);

        role = new ComboBox<>("Rodzaj użytkownika");
        role.setAllowCustomValue(false);
        role.setItems(UserType.NOTCONFIRMED.name(),UserType.USER.name(),UserType.ADMIN.name());
        role.setHelperText("Rozwiń listę, aby nadać uprawnienia");

        isCoffeeMember = new ComboBox<>("Należy do grupy kawoszy");
        isCoffeeMember.setHelperText("Rozwiń listę, aby nadać uprawnienia");
        isCoffeeMember.setItems(true,false);
        isCoffeeMember.setItemLabelGenerator(value -> value ? "Tak" : "Nie");

        emailConfirmed = new ComboBox<>("Email został potwierdzony");
        emailConfirmed.setHelperText("Definiuje czy użytkownik potwierdził e-maila klikając w link");
        emailConfirmed.setItems(true,false);
        emailConfirmed.setItemLabelGenerator(value -> value ? "Tak" : "Nie");

        Button emailSender = new Button("Wyślij ponownie link aktywacyjny",event ->{
            if (!userService.getUserByEmail(email.getValue()).orElseThrow().getEmailConfirmed()) {
                Token token = tokenService.generateToken(userRepository.findUserByEmail(email.getValue()).orElseThrow(), TokenType.REGISTRATION);
                tokenRepository.save(token);
                emailService.sendEmailConfirmationLink(email.getValue(), token.getValue());

                Notification.show("Wysłano link potwierdzający e-mail dla " + firstName.getValue() + " " + lastName.getValue(), 5000, Position.BOTTOM_CENTER);
            } else {
                Notification.show("Email użytkownika " + firstName.getValue() + " " + lastName.getValue() + " został już potwierdzony.", 5000, Position.BOTTOM_CENTER);
            }
        });


        formLayout.add(firstName, lastName, email,role,isCoffeeMember,emailConfirmed,emailSender);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        buttonLayout.add(save, cancel,delete);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(User value) {
        this.user = value;

        if (value != null){
            binder.readBean(value);
        }
        else {
            binder.removeBean();
            firstName.clear();
            lastName.clear();
            email.clear();
            isCoffeeMember.clear();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> peopleId = event.getRouteParameters().get(PEOPLE_ID).map(Integer::parseInt);

        if (peopleId.isPresent()) {
            Optional<User> peopleFromBackend = userService.get(peopleId.get());

            if (peopleFromBackend.isPresent()) {
                grid.select(peopleFromBackend.get());
                populateForm(peopleFromBackend.get());
            } else {
                Notification.show(String.format("The requested user was not found, ID = %s", peopleId.get()), 3000,
                        Notification.Position.BOTTOM_START);

                refreshGrid();
                event.forwardTo(UserView.class);
            }
        }
        else {
            clearForm();
        }
    }
}
