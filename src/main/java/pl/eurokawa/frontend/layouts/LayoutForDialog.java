package pl.eurokawa.views.layouts;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class LayoutForDialog extends VerticalLayout {
    private final TextField textField = new TextField();
    private final Button saveButton = new Button("Zapisz");
    private final Button cancelButton = new Button("Anuluj");

    public LayoutForDialog(String message){
        H4 header = new H4(message);

        saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        add(header, textField,saveButton,cancelButton);
    }

    public LayoutForDialog(String dialogUpperMessage, String message){
        H2 upperMessage = new H2(dialogUpperMessage);
        H4 header = new H4(message);

        saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        add(upperMessage,header, textField,saveButton,cancelButton);
    }

    public TextField getTextField() {
        return textField;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}
