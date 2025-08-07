package pl.eurokawa.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Value;
import pl.eurokawa.user.User;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.user.UserType;
import pl.eurokawa.views.layouts.EmptyLayout;

@UIScope
@Route(value = "notconfirmed", layout = EmptyLayout.class)
public class RegisteredNotAcceptedUser extends Div {
    private final SecurityService securityService;

    @Value("${spring.mail.username}")
    String emailAddress;

    public RegisteredNotAcceptedUser(@Value("${spring.mail.username}") String emailAddress, SecurityService securityService){
        this.emailAddress = emailAddress;
        this.securityService = securityService;

        User user = securityService.getLoggedUser();
        TextArea emailConfirmationInfo = emailConfirmationMessage(user);
        TextArea accessConfirmationInfo = accesssConfirmationMessage(user);
        TextArea contactInfo = contactInfo();

        add(emailConfirmationInfo,accessConfirmationInfo,contactInfo);
    }

    private TextArea emailConfirmationMessage(User user){
        TextArea text = new TextArea();
        textLayoutConfiguration(text);

        if (user.getEmailConfirmed()) {
            text.setValue("Twój email został potwierdzony.");
            setTextColorGreen(text);
        } else {
            text.setValue("Twój email nie został jeszcze potwierdzony.\nSprawdź email i kliknij w link aktywacyjny.");
            setTextColorRed(text);
        }

        return text;
    }

    private TextArea accesssConfirmationMessage(User user){
        TextArea text = new TextArea();
        textLayoutConfiguration(text);

        if (!user.getRole().equals(UserType.NOTCONFIRMED.name())) {
            text.setValue("Twoje konto zostało pomyślnie potwierdzone przez administratora.");
            setTextColorGreen(text);
        } else {
            text.setValue("Twoje konto nie zostało jeszcze potwierdzone przez administratora.\nZazwyczaj dostęp po pozytywnej weryfikacji nadawany jest jeszcze tego samego dnia.");
            setTextColorRed(text);
        }

        return text;
    }

    private TextArea contactInfo(){
        TextArea text = new TextArea();
        textLayoutConfiguration(text);

        text.setValue("W przypadku problemów prosimy o kontakt pod skrzynką:\n" + emailAddress);

        return text;
    }

    private void textLayoutConfiguration(TextArea text){
        text.setReadOnly(true);
        text.addThemeVariants(TextAreaVariant.LUMO_ALIGN_CENTER);
        text.setMinHeight("200px");
        text.setAutofocus(true);
        text.setWidthFull();
        text.getStyle()
                .set("text-align", "center")
                .set("font-size", "48px")
                .set("margin-top", "auto");
    }

    private void setTextColorGreen(TextArea text){
        text.getStyle().setBackgroundColor("#0a6310");
    }

    private void setTextColorRed(TextArea text){
        text.getStyle().setBackgroundColor("#8c0e34");
    }
}
