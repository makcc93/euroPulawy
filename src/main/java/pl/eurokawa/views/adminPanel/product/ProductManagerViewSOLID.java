package pl.eurokawa.views.adminPanel.product;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.eurokawa.email.EmailService;
import pl.eurokawa.product.Product;
import pl.eurokawa.product.ProductService;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.token.Token;
import pl.eurokawa.token.TokenRepository;
import pl.eurokawa.token.TokenServiceImpl;
import pl.eurokawa.token.TokenType;
import pl.eurokawa.user.User;
import pl.eurokawa.views.layouts.LayoutForDialog;

@Route("product-manager-solid")
@RolesAllowed("ADMIN")
public class ProductManagerViewSOLID extends Div {
    private static final Logger log = LoggerFactory.getLogger(ProductManagerViewSOLID.class);
    private final Grid<Product> grid = new Grid<>(Product.class,false);
    private final ListDataProvider<Product> dataProvider;
    private final TokenServiceImpl tokenServiceImpl;
    private final SecurityService securityService;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final ProductService productService;

    public ProductManagerViewSOLID(TokenServiceImpl tokenServiceImpl, SecurityService securityService, EmailService emailService, TokenRepository tokenRepository, ProductService productService){
        this.tokenServiceImpl = tokenServiceImpl;
        this.securityService = securityService;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.productService = productService;

        dataProvider = new ListDataProvider<>(productService.getAllProducts());
        grid.setDataProvider(dataProvider);
        grid.setAllRowsVisible(true);

        createGridColumns(grid,productService);

        Button addProductButton = new Button();
        addProductButton.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
        addProductButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
        addProductButton.setTooltipText("Dodaj nowy produkt");
        addNewProductDialog(addProductButton,grid,productService);

        add(grid,addProductButton);
    }

    private void addNewProductDialog(Button addProductButton, Grid<Product> grid,ProductService productService) {
        addProductButton.addClickListener(clickEvent ->{
            Dialog dialog = new Dialog();
            LayoutForDialog layoutForDialog = new LayoutForDialog("DODAWANIE NOWEGO PRODUKTU", "Wprowadź nazwę nowego produktu");
            dialog.add(layoutForDialog);

            layoutForDialog.getSaveButton().addClickListener(saveEvent ->{
                String userInputValue = layoutForDialog.getTextField().getValue();

                if (userInputValue != null) {
                    productService.create(userInputValue);
                    Notification.show("Produkt: " + userInputValue + " został poprawnie dodany", 5000, Notification.Position.MIDDLE);
                    dialog.close();
                }
                else {
                    Notification.show("Wprowadź nazwę dodawanego produktu",5000, Notification.Position.MIDDLE);
                }

                refreshGrid(grid,productService);
            });

            layoutForDialog.getCancelButton().addClickListener(cancelEvent ->{
                dialog.close();
            });

            dialog.open();

        });
        refreshGrid(grid,productService);
    }

    private void createGridColumns(Grid<Product> grid,ProductService productService) {
        createProductIdColumn(grid);
        createProductNameColumn(grid);
        createActionColumn(grid,productService);
    }

    private void createActionColumn(Grid<Product> grid,ProductService productService) {
        grid.addColumn(new ComponentRenderer<>(product -> {
            Button edit = new Button();
            edit.setIcon(VaadinIcon.TOOLS.create());
            edit.addClickListener(editClick ->{
                Dialog dialog = new Dialog();
                LayoutForDialog layoutForDialog = new LayoutForDialog("EDYCJA PRODUKTU: " + product.getName(), "Edytuj nazwę produktu");
                dialog.add(layoutForDialog);

                layoutForDialog.getTextField().setValue(product.getName());

                layoutForDialog.getSaveButton().addClickListener(saveClick ->{
                    product.setName(layoutForDialog.getTextField().getValue());
                    productService.save(product);

                    Notification.show("Poprawnie zmieniono nazwę produktu",5000, Notification.Position.MIDDLE);
                    dialog.close();
                    refreshGrid(grid,productService);
                });

                layoutForDialog.getCancelButton().addClickListener(cancelClick -> {
                    dialog.close();
                });

                dialog.open();
            });

            Button delete = new Button();
            delete.setIcon(VaadinIcon.TRASH.create());
            delete.addClickListener(deleteClick ->{
                User user = securityService.getLoggedUser();

                Token token = tokenServiceImpl.generateToken(user, TokenType.SIX_NUMBERS);
                tokenRepository.save(token);
                emailService.sendSixNumbersCode(user.getEmail(),token.getValue());

                Dialog dialog = new Dialog();
                LayoutForDialog layoutForDialog = new LayoutForDialog("USUWANIE PRODUKTU: " + product.getName(), "W celu usunięcia towaru wprowadź kod autoryzacji z emaila");
                dialog.add(layoutForDialog);

                layoutForDialog.getSaveButton().addClickListener(saveClick ->{
                    if (layoutForDialog.getTextField().getValue().equals(token.getValue())){
                        productService.delete(product);

                        Notification.show("Produkt poprawnie usunięty",5000, Notification.Position.MIDDLE);
                        dialog.close();
                    }
                    else {
                        Notification.show("Błędy kod autoryzacji",5000, Notification.Position.MIDDLE);
                    }

                    refreshGrid(grid,productService);
                });

                dialog.open();
            });

            HorizontalLayout layout = new HorizontalLayout();
            layout.add(edit,delete);

            return layout;
        })).setHeader("Akcje").setAutoWidth(true);
        refreshGrid(grid,productService);
    }

    private void refreshGrid(Grid<Product> grid,ProductService productService) {
        grid.setDataProvider(new ListDataProvider<>(productService.getAllProducts()));
        grid.getDataProvider().refreshAll();
    }

    private void createProductNameColumn(Grid<Product> grid) {
        grid.addColumn(product -> product.getName() != null ? product.getName() : "").setHeader("Nazwa").setAutoWidth(true);
    }

    private void createProductIdColumn(Grid<Product> grid) {
        grid.addColumn(product -> product.getId() != null ? product.getId() : "").setHeader("ID").setAutoWidth(true);
    }
}

