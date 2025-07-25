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
import pl.eurokawa.balance.*;
import pl.eurokawa.email.EmailService;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.token.*;
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;
import pl.eurokawa.views.layouts.LayoutForDialog;

import java.math.BigDecimal;

@RolesAllowed("ADMIN")
@Route("manual-balance-setter")
public class BalanceManualSetterView extends Div {
    private final SecurityService securityService;
    private final EmailService emailService;
    private TextField newBalance;
    private final TokenService tokenService;
    private final BalanceService balanceService;

    public BalanceManualSetterView(SecurityService securityService, EmailService emailService, TokenService tokenService, BalanceService balanceService){

        this.securityService = securityService;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.balanceService = balanceService;

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
        newBalance.setValue(String.format("%.2f",balanceService.getCurrentBalance()));
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
            Token token = tokenService.generateToken(loggedUser, TokenType.BALANCE_SETTER);

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
