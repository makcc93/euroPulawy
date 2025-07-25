package pl.eurokawa.views.account;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import pl.eurokawa.token.*;
import pl.eurokawa.user.User;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.email.EmailService;
import pl.eurokawa.user.UserService;
import pl.eurokawa.views.HomeView;
import pl.eurokawa.views.layouts.MainLayout;
import pl.eurokawa.views.layouts.LayoutForDialog;

import java.util.Optional;

@Route(value = "/account/:userId?", layout = MainLayout.class)
public class UserAccountView extends Div implements BeforeEnterObserver {

    private static final Logger log = LogManager.getLogger(UserAccountView.class);
    private final UserService userService;
    private final SecurityService securityService;
    private String USER_ID = "userId";
    private final User loggedUser;
    private final BeanValidationBinder<User> binder;
    private final EmailService emailService;
    private final TokenService tokenService;


    public UserAccountView(UserService userService, SecurityService securityService, EmailService emailService, TokenService tokenService) {
        this.userService = userService;
        this.securityService = securityService;
        loggedUser = securityService.getLoggedUser();
        this.emailService = emailService;
        this.tokenService = tokenService;


        binder = new BeanValidationBinder<>(User.class);

//        HorizontalLayout verticalLayout = new HorizontalLayout();
        VerticalLayout verticalLayout = new VerticalLayout();

        VerticalLayout userContent = createUserInformation(loggedUser);
        VerticalLayout changePassword = changeUserPassword(loggedUser);

        H1 h1 = new H1("Cześć, " + loggedUser.toString() + "!");
        h1.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "var(--lumo-space-l)");

        verticalLayout.add(userContent,changePassword);

        add(h1,verticalLayout);
    }

    private void createActionButtons(VerticalLayout layout,Dialog dialog){
        Button save = new Button("Zapisz");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Anuluj");
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);

        save.addClickListener(event -> {
           try {
               if (this.loggedUser != null) {
                   binder.writeBean(this.loggedUser);
                   userService.save(this.loggedUser);

                   UI.getCurrent().navigate(HomeView.class);

                   Notification notification = Notification
                           .show("Dane zaktualizowane pomyślnie", 3000, Notification.Position.MIDDLE);
                   notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                   dialog.close();
                   UI.getCurrent().navigate("/account");
               }
           } catch (DataIntegrityViolationException validationException){
                Notification notification = Notification
                        .show("Błąd! Sprawdź poprawność danych!",3000,Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException e) {
               throw new RuntimeException(e);
           }
        });

        cancel.addClickListener(event -> {
            binder.readBean(loggedUser);
            dialog.close();
        });

        layout.add(save,cancel);
    }

    private VerticalLayout createUserInformation(User loggedUser){
        VerticalLayout userInfo = new VerticalLayout();

        Button button = new Button("Chcę zmienić swoje dane");
        button.setSizeFull();
        button.setWidthFull();
        button.getStyle().set("font-size","30px");
        button.addThemeVariants(ButtonVariant.LUMO_LARGE);
        button.addClickListener(event ->{

            VerticalLayout insideLayout = new VerticalLayout();
            Dialog dialog = new Dialog();

            TextField firstNameField = new TextField("Imię");
                firstNameField.setValue(loggedUser.getFirstName());

            TextField lastNameField = new TextField("Nazwisko");
                lastNameField.setValue(loggedUser.getLastName());

            TextField emailField = new TextField("Email");
                emailField.setValue(loggedUser.getEmail());
                emailField.setReadOnly(true);

            insideLayout.add(firstNameField,lastNameField,emailField);
            binder.bind(firstNameField,User::getFirstName,User::setFirstName);
            binder.bind(lastNameField,User::getLastName,User::setLastName);
            binder.bind(emailField,User::getEmail,User::setEmail);

            createActionButtons(insideLayout,dialog);

            dialog.add(insideLayout);
            dialog.open();
        });

        userInfo.add(button);
        return userInfo;
    }

    private VerticalLayout changeUserPassword(User user){
        VerticalLayout layout = new VerticalLayout();

        Button changePasswordButton = new Button("Chcę zmienić swoje hasło");
            changePasswordButton.setSizeFull();
            changePasswordButton.setWidthFull();
            changePasswordButton.getStyle().set("font-size","30px");
            changePasswordButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        changePasswordButton.addClickListener(event ->{
            Dialog mainDialog = new Dialog();

            VerticalLayout insideDialogLayout = new VerticalLayout();

            PasswordField passwordField = new PasswordField("Wpisz nowe hasło");
                passwordField.setPlaceholder("Minimum 6 znaków");

            PasswordField passwordFieldRepeated = new PasswordField("Powtórz nowe hasło");

            Button confirmButton = new Button("Zapisz nowe hasło",click ->{
                if (passwordField.getValue().equals(passwordFieldRepeated.getValue())){
                    Token token = tokenService.generateToken(user, TokenType.PASSWORD_RESET);

                    emailService.sendSixNumbersCode(user.getEmail(), token.getValue());
                    Notification.show("Kod autoryzacji wysłano na emaila " + user.getEmail(),3000, Notification.Position.BOTTOM_CENTER);

                    Dialog tokenDialog = new Dialog();
                    LayoutForDialog layoutForDialog = new LayoutForDialog("Wpisz KOD otrzymany na emaila");

                    layoutForDialog.getSaveButton().addClickListener(confirmEvent -> {
                        String tokenInUserEmail = tokenService.getLastUserTokenByType(user.getId(),TokenType.PASSWORD_RESET).getValue();
                        String userTokenInputValue = layoutForDialog.getTextField().getValue();

                        if (tokenInUserEmail.equals(userTokenInputValue)){
                            userService.setUserNewPassword(user.getEmail(),passwordField.getValue());

                            tokenDialog.close();
                            mainDialog.close();

                            Notification.show("Gratulacje!\nHasło zmienione poprawnie.",5000, Notification.Position.BOTTOM_CENTER);
                            log.info("UserAccount, changeUserPassword, Zmiana hasła dla {}",user.toString());
                        }
                        else {
                            Notification.show("Błędne podany kod!\nSpróbuj ponownie",5000, Notification.Position.BOTTOM_CENTER);
                        }
                    });

                    layoutForDialog.getCancelButton().addClickListener(cancelEvent -> {
                        tokenDialog.close();
                        tokenService.delete(token);
                    });

                    tokenDialog.add(layoutForDialog);
                    tokenDialog.open();
                }
                else {
                    Notification notification = Notification.show("Hasła do siebie nie pasują", 3000, Notification.Position.BOTTOM_CENTER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

            Button cancelButton = new Button("Anuluj", click ->{
                mainDialog.close();
            });
            cancelButton.addThemeVariants(ButtonVariant.LUMO_WARNING);

            insideDialogLayout.add(passwordField,passwordFieldRepeated,confirmButton,cancelButton);

            mainDialog.add(insideDialogLayout);
            mainDialog.open();
        });

        layout.add(changePasswordButton);

        return layout;
    }

    private VerticalLayout createTokenLayout(User user, String password, Dialog insideDialog, Dialog outsideDialog){
        VerticalLayout insideTokenLayout = new VerticalLayout();

        Button tokenConfirmButton = new Button("Zatwiedź kod");
            tokenConfirmButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        Button tokenCancelButton = new Button("Anuluj");
            tokenCancelButton.addThemeVariants(ButtonVariant.LUMO_WARNING);

        H4 header = new H4("WPISZ KOD Z E-MAILA\n W CELU POTWIERDZENIA");

        TextField tokenInput = new TextField("KOD");

        tokenConfirmButton.addClickListener(confirm ->{
            Token tokenInUserEmail = tokenService.getLastUserTokenByType(user.getId(),TokenType.PASSWORD_RESET);

            if (tokenInput.getValue().equals(tokenInUserEmail.getValue())){
                userService.setUserNewPassword(user.getEmail(), password);

                insideDialog.close();
                outsideDialog.close();

                log.info("UserAccount, createTokenLayout, haslo zmienione dla {}",user.getEmail());
                log.info("userMailToken = {}",tokenInUserEmail.getValue());
            }
            else {
                Notification.show("Błędne podany kod!\nSpróbuj ponownie",3000, Notification.Position.BOTTOM_CENTER);
            }
        });

        tokenCancelButton.addClickListener(cancel ->{
            outsideDialog.close();
        });

        insideTokenLayout.add(header,tokenInput,tokenConfirmButton,tokenCancelButton);
        return insideTokenLayout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> userId = event.getRouteParameters().get(USER_ID).map(Integer::parseInt);

        if (userId.isPresent()){
            Optional<User> userFromBackend = userService.get(userId.get());
            binder.readBean(userFromBackend.get());
        }
    }
}
