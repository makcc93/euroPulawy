package pl.eurokawa.views.adminPanel.balance;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import pl.eurokawa.balance.Balance;
import pl.eurokawa.balance.BalanceBroadcaster;
import pl.eurokawa.balance.BalanceRepository;
import pl.eurokawa.balance.BalanceService;
import pl.eurokawa.email.EmailService;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.token.Token;
import pl.eurokawa.token.TokenRepository;
import pl.eurokawa.token.TokenService;
import pl.eurokawa.token.TokenType;
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;
import pl.eurokawa.views.layouts.LayoutForDialog;

import java.math.BigDecimal;

@RolesAllowed("ADMIN")
@Route("manual-balance-setter")
public class BalanceManualSetterView extends Div {
    private final BalanceRepository balanceRepository;
    private final BalanceService balanceService;
    private final SecurityService securityService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private TextField newBalance;

    public BalanceManualSetterView(BalanceRepository balanceRepository, BalanceService balanceService, SecurityService securityService, TokenService tokenService, EmailService emailService, TokenRepository tokenRepository){
        this.balanceRepository = balanceRepository;
        this.balanceService = balanceService;
        this.securityService = securityService;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;

        H4 header = new H4 ("USTAL WARTOŚĆ DOSTĘPNYCH ŚRODKÓW");
        header.getStyle().set("text-align", "center")
                .set("font-size", "60px")
                .set("margin-top", "auto");

        VerticalLayout layout = new VerticalLayout();
        Dialog changeConfirmationDialog = new Dialog();

        newBalance = new TextField();
        newBalance.setLabel("Wpisz kwotę");
        newBalance.setWidth("400px");
        newBalance.setHeight("200px");
        newBalance.getStyle()
                .set("text-align", "center")
                .set("font-size", "36px")
                .set("margin-top", "auto");
        newBalance.setValue(String.format("%.2f",balanceRepository.findLastBalanceValue().getAmount()));
        valueChangeListener(newBalance);

        Button save = new Button("Zatwierdź");
        save.setWidth("400px");
        save.setHeight("60px");
        save.addThemeVariants(ButtonVariant.LUMO_LARGE,ButtonVariant.LUMO_SUCCESS);
        save.getStyle()
                .set("text-align", "center")
                .set("font-size", "36px")
                .set("margin-top", "auto");
        save.addThemeVariants(ButtonVariant.LUMO_LARGE);
        clickListener(save,newBalance,changeConfirmationDialog);

        layout.add(newBalance,save,changeConfirmationDialog);

        add(header,layout);
    }

    private void valueChangeListener(TextField textField){
        textField.addValueChangeListener(event -> {
            try {
                double parsed = Double.parseDouble(event.getValue().replace(",", "."));
                textField.setValue(String.format("%.2f",parsed));
            }
            catch (NumberFormatException e){
                textField.setValue("0.00");
            }
        });

    }

    private void clickListener(Button button, TextField textField, Dialog dialogWindow){
        button.addClickListener(event ->{
            dialogWindow.open();
            LayoutForDialog layoutForDialog = new LayoutForDialog("W celu potwierdzenia ręcznej zmiany środków potrzeba dodatkowej autoryzacji.");
            dialogWindow.add(layoutForDialog);

            User loggedUser = securityService.getLoggedUser();
            Token token = tokenService.generateToken(loggedUser, TokenType.SIX_NUMBERS);
            tokenRepository.save(token);
            emailService.sendSixNumbersCode(loggedUser.getEmail(),token.getValue());

            layoutForDialog.getSaveButton().addClickListener(confirm ->{
                if (layoutForDialog.getTextField().getValue().equals(token.getValue())){
                    BigDecimal newValue = BigDecimal.valueOf(Double.parseDouble(textField.getValue()));

                    Balance changedBalance = new Balance(loggedUser,newValue);

                    newBalance.setValue(changedBalance.toString());

                    balanceService.updateBalance(loggedUser,newValue, TransactionType.MANUAL);

                    BalanceBroadcaster.broadcast(balanceService.getCurrentBalance());

                    Notification.show("Nowa wartość ustawiona poprawnie",5000, Notification.Position.BOTTOM_CENTER);
                    dialogWindow.close();
                }
                else {
                    Notification.show("Błędny kod autoryzacji. Spróbuj ponownie.");
                }
            });

            layoutForDialog.getCancelButton().addClickListener(cancel ->{
                dialogWindow.close();
            });
        });
       }
}
