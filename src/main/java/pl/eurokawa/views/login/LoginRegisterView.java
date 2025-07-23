package pl.eurokawa.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.eurokawa.email.EmailType;
import pl.eurokawa.security.PasswordValidator;
import pl.eurokawa.email.EmailService;
import pl.eurokawa.security.S3Config;
import pl.eurokawa.storage.FileType;
import pl.eurokawa.storage.S3Service;
import pl.eurokawa.terms.TermsOfServiceRepository;
import pl.eurokawa.terms.TermsOfServiceService;
import pl.eurokawa.token.Token;
import pl.eurokawa.token.TokenRepository;
import pl.eurokawa.token.TokenService;
import pl.eurokawa.token.TokenType;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserRepository;
import pl.eurokawa.views.layouts.EmptyLayout;
import pl.eurokawa.user.UserService;
import org.apache.commons.validator.routines.EmailValidator;
import pl.eurokawa.views.layouts.LayoutForDialog;

import java.util.Optional;


@AnonymousAllowed
@Route(value = "login", layout = EmptyLayout.class)
@RouteAlias(value = "", layout = EmptyLayout.class)
@RouteAlias(value = "register",layout = EmptyLayout.class)
public class LoginRegisterView extends Div {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(LoginRegisterView.class);
    private final PasswordValidator passwordValidator;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final TermsOfServiceRepository termsOfServiceRepository;
    private final TermsOfServiceService termsOfServiceService;
    private final S3Service s3Service;
    private final S3Config s3Config;

    public LoginRegisterView(AuthenticationManager authenticationManager, UserService userService, UserRepository userRepository, PasswordValidator passwordValidator, TokenService tokenService, EmailService emailService, TokenRepository tokenRepository, TermsOfServiceRepository termsOfServiceRepository, TermsOfServiceService termsOfServiceService, S3Service s3Service, S3Config s3Config) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordValidator = passwordValidator;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.termsOfServiceRepository = termsOfServiceRepository;
        this.termsOfServiceService = termsOfServiceService;
        this.s3Service = s3Service;
        this.s3Config = s3Config;

        HorizontalLayout layout = pageView();

        H1 h1 = new H1("Witaj!");
        h1.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "var(--lumo-space-l)");
        H1 h2 = new H1("Zaloguj się lub załóż konto");
        h2.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "var(--lumo-space-l)");

        VerticalLayout fullLayout = new VerticalLayout();
        fullLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        fullLayout.getStyle().set("margin-top", "5rem");
        Span footerText = new Span("by Mateusz Kruk © All rights reserved | frontend created with vaadin.com");
        footerText.getStyle()
                .set("font-size", "very small")
                .set("margin-top", "auto")
                .set("padding-top", "1em")
                .set("color", "#666669");

        fullLayout.add(h1, h2, layout, footerText);

        add(fullLayout);
    }

    private HorizontalLayout pageView(){
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setWidth("auto");
        layout.setHeight("550px");
        layout.setSpacing(false);
        layout.getStyle()
                .set("margin", "0 auto")
                .set("background", "dark")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 8px rgba(0,4,0,0.1)");

        Div formsContainer = new Div();
        formsContainer.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("gap", "var(--lumo-space-xl)");

        VerticalLayout loginPanel = createLoginView();
        Div divider = divider();
        VerticalLayout registerPanel = createRegisterView(emailService);

        layout.add(loginPanel,divider,registerPanel);

        return layout;
    }

    private Div divider(){
        Div div = new Div();
        div.getStyle()
                .set("border-left", "2px solid var(--lumo-contrast-20pct)")
                .set("height", "300px")
                .set("background", "dark")
                .set("margin", "0 2rem")
                .set("align-self", "center");

        return div;
    }


    private VerticalLayout createRegisterView(EmailService emailService) {
        VerticalLayout registerPanel = new VerticalLayout();
        registerPanel.setPadding(true);
        registerPanel.setWidth("450px");
        registerPanel.setHeight("100%");
        registerPanel.getStyle().set("align-self", "flex-start");
        registerPanel.getStyle().set("padding", "2rem");

        H2 registerHeader = new H2("Rejestracja");
        registerHeader.getStyle()
                .set("margin", "0 auto")
                .set("margin-top", "0")
                .set("text-align", "center");


        TextField firstNameField = new TextField("Imię");

        TextField lastNameField = new TextField("Nazwisko");

        TextField emailField = new TextField("Email");

        PasswordField passwordField = new PasswordField("Hasło");
        passwordField.setPlaceholder("Minimum 6 znaków");

        PasswordField confirmPasswordField = new PasswordField("Powtórz hasło");
        confirmPasswordField.getStyle().set("margin-bottom", "0.5em");
        confirmPasswordField.setPlaceholder("Wprowadź hasło ponownie");

        Button registerButton = new Button("Zarejestruj się", event -> {
            String firstName = normalizeName(firstNameField.getValue());
            String lastName = normalizeName(lastNameField.getValue());
            String email = emailField.getValue();
            String password = passwordField.getValue();
            String confirmPassword = confirmPasswordField.getValue();

            if (validateRegistration(firstName,lastName,email,password,confirmPassword)){
                User registeredUser = userService.registerUser(firstName, lastName, email, password);
                Token token = tokenService.generateToken(registeredUser, TokenType.REGISTRATION);
                tokenRepository.save(token);

                emailService.sendEmailConfirmationLink(registeredUser.getEmail(),token.getValue());

                Notification.show("Rejestracja udana!\n Na Twoją skrzynkę email" + registeredUser.getEmail() + " został wysłany link aktywacyjny.",
                        5000, Notification.Position.MIDDLE);

                clearForm(firstNameField,lastNameField,emailField);
                clearPassword(passwordField,confirmPasswordField);

                Token accountConfirmationToken = tokenService.generateToken(registeredUser,TokenType.ACCOUNT_CONFIRMATION);
                tokenRepository.save(accountConfirmationToken);

                emailService.sendEmailNotificationToAdmins(EmailType.NEW_USER_REGISTER,registeredUser, accountConfirmationToken.getValue());
            }
        });
        registerButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        registerButton.setEnabled(false);

        Checkbox termsAndConditions = new Checkbox();
        termsAndConditions.setHeight("80px");
        termsAndConditions.setWidth("80px");
        termsAndConditions.setLabel("Zapoznałem się i akceptuję regulamin serwisu");
        termsAndConditions.addClickListener(checkboxClickEvent -> {
            registerButton.setEnabled(termsAndConditions.getValue());
        });
        termsAndConditions.setLabelComponent(termsOfServiceService.getTermsOfServiceLink(FileType.TERMS,termsOfServiceRepository.findCurrentActual(),s3Service,"Zapoznałem się z regulaminem serwisu"));

        FormLayout registerForm = new FormLayout();
        registerForm.add(firstNameField,lastNameField,emailField,passwordField,confirmPasswordField,registerButton,termsAndConditions);

        registerForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        registerPanel.add(registerHeader,registerForm);

        return registerPanel;
    }

    private void clearForm(TextField... fields) {
        for (TextField field : fields){
            field.clear();
        }
    }

    private void clearPassword(PasswordField... passwords){
        for (PasswordField password : passwords){
            password.clear();
        }
    }

    private boolean validateRegistration(String firstName, String lastName, String email,String password, String repeatedPassword){
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||password.isEmpty() || repeatedPassword.isEmpty()){
            Notification.show("Uzupełnij poprawnie wszystkie pola",5000, Notification.Position.BOTTOM_CENTER);

            return false;
        }

        if (!passwordValidator.validatePassword(password)){
            Notification.show("Hasło jest za krótkie!",5000, Notification.Position.TOP_CENTER);

            return false;
        }

        if (!passwordValidator.doPasswordsMatch(password,repeatedPassword)){
            Notification.show("Hasła do siebie nie pasują!",5000, Notification.Position.TOP_CENTER);

            return false;
        };

        if (userService.getUserByEmail(email).isPresent()){
            Notification.show("Użytkownik z podanym mailem już istnieje!",5000, Notification.Position.BOTTOM_CENTER);

            return false;
        }

        if (!emailIsValid(email)){
            Notification.show("Błędny email!",5000, Notification.Position.BOTTOM_CENTER);

            return false;
        }

        if (!nameIsValid(firstName) || !nameIsValid(lastName)){
            Notification.show("Niedozwolone znaki w imieniu lub nazwisku!",5000, Notification.Position.BOTTOM_CENTER);

            return false;
        }

        return true;
    }

    private String normalizeName(String input){
        String workingOnName = input.trim().toLowerCase();
        char[] array = workingOnName.toCharArray();

        String firstCharUpperCase = String.valueOf(array[0]).toUpperCase();
        StringBuilder sb = new StringBuilder(firstCharUpperCase);

        for (int i = 1; i < array.length ;i++) {

            sb.append(array[i]);
        }

        return sb.toString();
    }

    private boolean emailIsValid(String email){

        return EmailValidator.getInstance().isValid(email);
    }

    private boolean nameIsValid(String name){
        if (name.matches("^[A-Za-z-']+$")){
            return true;
        };

        return false;
    }

    private VerticalLayout createLoginView(){

        VerticalLayout loginPanel = new VerticalLayout();

        loginPanel.setPadding(true);
        loginPanel.setWidth("450px");
        loginPanel.setHeight("120%");
        loginPanel.getStyle().set("flex-grow", "1");
        loginPanel.getStyle().set("align-self", "flex-start");
        loginPanel.getStyle().set("padding", "2rem");

        H2 loginHeader = new H2();
        loginHeader.setText("Logowanie");
        loginHeader.getStyle()
                .set("margin", "0 auto")
                .set("margin-top", "0")
                .set("text-align", "center");

        LoginForm loginForm = new LoginForm();
        loginForm.getStyle().set("margin-top", "1.5rem");
        configureLoginForm(loginForm);

        loginPanel.add(loginHeader,loginForm);

        return loginPanel;
    }

    private void configureLoginForm(LoginForm loginForm){
        LoginI18n i18n = new LoginI18n();
        LoginI18n.Form form = new LoginI18n.Form();

        form.setUsername("Email");
        form.setPassword("Hasło");
        form.setSubmit("Zaloguj się");
        form.setForgotPassword("Nie pamiętasz hasła?");
        i18n.setForm(form);

        LoginI18n.ErrorMessage errorMessage = new LoginI18n.ErrorMessage();
        errorMessage.setTitle("Błąd");
        errorMessage.setMessage("Sprawdź poprawność danych.");
        i18n.setErrorMessage(errorMessage);

        loginForm.setI18n(i18n);
        loginForm.addLoginListener(event -> {
            try{
                authenticate(event.getUsername(),event.getPassword());
                UI.getCurrent().navigate("/home");
            }
            catch(Exception e){
                loginForm.setError(true);
            }
        });

        loginForm.addForgotPasswordListener(event -> {
            setNewPassword();
        });
    }

    private void setNewPassword(){
        VerticalLayout verticalLayout = new VerticalLayout();
        VerticalLayout tokenLayout = new VerticalLayout();
        Dialog dialog = new Dialog();

        TextField emailField = new TextField("Email");
        PasswordField newPasswordField = new PasswordField("Nowe hasło");
        newPasswordField.setPlaceholder("Minimum 6 znaków");
        PasswordField repeatedNewPasswordField = new PasswordField("Powtórz nowe hasło");

        FormLayout layout = new FormLayout();
        layout.add(emailField,newPasswordField,repeatedNewPasswordField);

        Button confirmButton = new Button("Zapisz nowe hasło", event ->{
            String email = emailField.getValue();
            String password = newPasswordField.getValue();
            String repeatedPassword = repeatedNewPasswordField.getValue();
            Optional<User> userByEmail = userRepository.findUserByEmail(email);

            if (!passwordValidator.doPasswordsMatch(password,repeatedPassword)){
                Notification.show("Hasła do siebie nie pasują!\nSpróbuj ponownie.",5000, Notification.Position.BOTTOM_CENTER);

                return;
            };

            if (!passwordValidator.validatePassword(password)){
                Notification.show("Hasło jest zbyt krótkie, minimalna długość to " + PasswordValidator.getPasswordLengthRequired() + " znaków.\nSpróbuj ponownie.",5000, Notification.Position.BOTTOM_CENTER);

                return;
            }

            if (userByEmail.isEmpty()){
                Notification.show("Użytkownik o podanym emailu " + email + " nie istnieje!\nSpróbuj ponownie lub zarejestruj się.",5000, Notification.Position.BOTTOM_CENTER);
            }
            else {
                Token token = tokenService.generateToken(userByEmail.orElseThrow(), TokenType.PASSWORD_RESET);

                emailService.sendSixNumbersCode(email,token.getValue());
                Notification.show("Kod autoryzacji wysłano na emaila " + email,3000, Notification.Position.TOP_CENTER);
                tokenRepository.save(token);

                Dialog tokenDialog = new Dialog();

                LayoutForDialog layoutForDialog = new LayoutForDialog("Wpisz KOD otrzymany na emaila");

                layoutForDialog.getSaveButton().addClickListener(saveEvent ->{
                    String inputValue = layoutForDialog.getTextField().getValue();
                    String emailTokenValue = tokenRepository.findFirstByUserIdOrderByIdDesc(userByEmail.orElseThrow().getId()).getValue();

                    if (inputValue.equals(emailTokenValue)) {
                        userService.setUserNewPassword(email, password);

                        tokenDialog.close();
                        dialog.close();

                        Notification.show("Gratulacje!\nHasło zmienione poprawnie.", 3000, Notification.Position.BOTTOM_CENTER);
                        logger.info("UserAccount, changeUserPassword, Zmiana hasła dla {}", userByEmail.orElseThrow());
                    }
                    else{
                        Notification.show("Błędny kod autoryzacji! Spróbuj ponownie.",3000, Notification.Position.BOTTOM_CENTER);
                    }
                });

                layoutForDialog.getCancelButton().addClickListener(cancelEvent ->{
                    tokenRepository.delete(token);

                    tokenDialog.close();
                });

                tokenDialog.add(layoutForDialog);
                tokenDialog.open();
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelResetButton = new Button("Anuluj",event ->{
            dialog.close();
        });
        cancelResetButton.addThemeVariants(ButtonVariant.LUMO_WARNING);

        verticalLayout.add(layout,confirmButton,cancelResetButton);

        dialog.add(verticalLayout,tokenLayout);
        dialog.setHeaderTitle("Ustalenie nowego hasła");
        dialog.open();
    }


    private void authenticate(String email,String password) throws AuthenticationException{
        Authentication authentication = new UsernamePasswordAuthenticationToken(email,password);

        Authentication authenticated = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        VaadinSession.getCurrent().setAttribute(Authentication.class,authenticated);
    }

}