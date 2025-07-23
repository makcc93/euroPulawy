package pl.eurokawa.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.user.UserType;

@SpringComponent
@Route("home")
@UIScope
public class HomeView extends VerticalLayout implements BeforeEnterObserver {
    private final SecurityService securityService;

    public HomeView(SecurityService securityService) {
        this.securityService = securityService;

        TextArea welcome = new TextArea();
        welcome.addThemeVariants(TextAreaVariant.LUMO_ALIGN_CENTER);
        welcome.setReadOnly(true);
        welcome.getStyle()
                .set("font-size", "28px")
                .set("margin-top", "auto");
        welcome.setValue("""
                Witaj na stronie!
                Miło Cię widzieć.
                
                W lewym górnym rogu strony widoczne są obecne zgromadzone środki, którymi możemy wspólnie dysponować
                
                Zakładki:
                
                W zakładce \"Ludzie\"  możesz sprawdzić wszystkie aktywne osoby oraz łączną sumę ich wpłat.
                
                W zakładce \"Wpłaty\" możesz zarejestrować przekazane skarbnikowi pieniądze,
                wszystkie potwierdzone przez administratora wpłaty są widoczne w zakładce \"Historia wpłat\".
                
                W zakładce \"Zakupy\" możesz zarejestrować dokonany przez Ciebie zakup,
                wszystkie potwierdzone przez administratora zakupy są widoczne w zakładce \"Historia zakupów\".
                
                W lewym dolnym rogu jest zakładka Twojego konta oraz przycisk wylogowania.
                
                """);
        welcome.setHeightFull();
        welcome.setWidthFull();

        add(welcome);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (securityService.loggedUserHasRole(UserType.NOTCONFIRMED.name()) || !securityService.getLoggedUser().getEmailConfirmed()){
            beforeEnterEvent.rerouteTo(RegisteredNotAcceptedUser.class);
        }
    }
}

