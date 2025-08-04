//package pl.eurokawa.views.adminPanel.product;
//
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.html.Div;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.data.provider.ListDataProvider;
//import com.vaadin.flow.data.renderer.ComponentRenderer;
//import com.vaadin.flow.router.Route;
//import jakarta.annotation.security.RolesAllowed;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import pl.eurokawa.email.EmailService;
//import pl.eurokawa.product.Product;
//import pl.eurokawa.product.ProductRepository;
//import pl.eurokawa.security.SecurityService;
//import pl.eurokawa.token.Token;
//import pl.eurokawa.token.TokenRepository;
//import pl.eurokawa.token.TokenServiceImpl;
//import pl.eurokawa.token.TokenType;
//import pl.eurokawa.user.User;
//import pl.eurokawa.views.layouts.LayoutForDialog;
//
//@Route("product-manager")
//@RolesAllowed("ADMIN")
//public class ProductManagerView extends Div {
//
//    private static final Logger log = LoggerFactory.getLogger(ProductManagerView.class);
//    private final Grid<Product> grid;
//    private final ListDataProvider<Product> dataProvider;
//    private final TokenServiceImpl tokenServiceImpl;
//    private final SecurityService securityService;
//    private final EmailService emailService;
//    private final TokenRepository tokenRepository;
//
//    public ProductManagerView(ProductRepository productRepository, TokenServiceImpl tokenServiceImpl, SecurityService securityService, EmailService emailService, TokenRepository tokenRepository){
//        this.tokenServiceImpl = tokenServiceImpl;
//        this.securityService = securityService;
//        this.emailService = emailService;
//        this.tokenRepository = tokenRepository;
//
//        dataProvider = new ListDataProvider<>(productRepository.findAllProducts());
//        grid = new Grid<>(Product.class,false);
//        grid.setDataProvider(dataProvider);
//        grid.setAllRowsVisible(true);
//
//        createGridColumns(grid,productRepository);
//
//        Button addProductButton = new Button();
//        addProductButton.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
//        addProductButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
//        addProductButton.setTooltipText("Dodaj nowy produkt");
//        addNewProductDialog(addProductButton,grid,productRepository);
//
//        add(grid,addProductButton);
//    }
//
//    private void addNewProductDialog(Button addProductButton, Grid<Product> grid,ProductRepository productRepository) {
//        addProductButton.addClickListener(clickEvent ->{
//            Dialog dialog = new Dialog();
//            LayoutForDialog layoutForDialog = new LayoutForDialog("DODAWANIE NOWEGO PRODUKTU", "Wprowadź nazwę nowego produktu");
//            dialog.add(layoutForDialog);
//
//            layoutForDialog.getSaveButton().addClickListener(saveEvent ->{
//                String userInputValue = layoutForDialog.getTextField().getValue();
//
//                if (userInputValue != null) {
//                    Product product = new Product();
//                    product.setName(userInputValue.strip());
//
//                    productRepository.save(product);
//                    Notification.show("Produkt: " + product.getName().strip() + " został poprawnie dodany", 5000, Notification.Position.MIDDLE);
//                    dialog.close();
//                }
//                else {
//                    Notification.show("Wprowadź nazwę dodawanego produktu",5000, Notification.Position.MIDDLE);
//                }
//
//                refreshGrid(grid,productRepository);
//            });
//
//            layoutForDialog.getCancelButton().addClickListener(cancelEvent ->{
//                dialog.close();
//            });
//
//            dialog.open();
//
//        });
//        refreshGrid(grid,productRepository);
//    }
//
//    private void createGridColumns(Grid<Product> grid,ProductRepository productRepository) {
//        createProductIdColumn(grid);
//        createProductNameColumn(grid);
//        createActionColumn(grid,productRepository);
//    }
//
//    private void createActionColumn(Grid<Product> grid,ProductRepository productRepository) {
//        grid.addColumn(new ComponentRenderer<>(product -> {
//            Button edit = new Button();
//            edit.setIcon(VaadinIcon.TOOLS.create());
//            edit.addClickListener(editClick ->{
//                Dialog dialog = new Dialog();
//                LayoutForDialog layoutForDialog = new LayoutForDialog("EDYCJA PRODUKTU: " + product.getName(), "Edytuj nazwę produktu");
//                dialog.add(layoutForDialog);
//
//                layoutForDialog.getTextField().setValue(product.getName());
//
//                layoutForDialog.getSaveButton().addClickListener(saveClick ->{
//                    product.setName(layoutForDialog.getTextField().getValue());
//                    productRepository.save(product);
//
//                    Notification.show("Poprawnie zmieniono nazwę produktu",5000, Notification.Position.MIDDLE);
//                    dialog.close();
//                    refreshGrid(grid,productRepository);
//                });
//
//                layoutForDialog.getCancelButton().addClickListener(cancelClick -> {
//                    dialog.close();
//                });
//
//                dialog.open();
//                refreshGrid(grid,productRepository); //try to delete this
//            });
//
//            Button delete = new Button();
//            delete.setIcon(VaadinIcon.TRASH.create());
//            delete.addClickListener(deleteClick ->{
//                User user = securityService.getLoggedUser();
//
//                Token token = tokenServiceImpl.generateToken(user, TokenType.SIX_NUMBERS);
//                tokenRepository.save(token);
//                emailService.sendSixNumbersCode(user.getEmail(),token.getValue());
//
//                Dialog dialog = new Dialog();
//                LayoutForDialog layoutForDialog = new LayoutForDialog("USUWANIE PRODUKTU: " + product.getName(), "W celu usunięcia towaru wprowadź kod autoryzacji z emaila");
//                dialog.add(layoutForDialog);
//
//                layoutForDialog.getSaveButton().addClickListener(saveClick ->{
//                    if (layoutForDialog.getTextField().getValue().equals(token.getValue())){
//                        productRepository.delete(product);
//
//                        Notification.show("Produkt poprawnie usunięty",5000, Notification.Position.MIDDLE);
//                        dialog.close();
//                    }
//                    else {
//                        Notification.show("Błędy kod autoryzacji",5000, Notification.Position.MIDDLE);
//                    }
//
//                    refreshGrid(grid,productRepository);
//                });
//
//                dialog.open();
//                refreshGrid(grid,productRepository); //try to delete this
//            });
//
//            HorizontalLayout layout = new HorizontalLayout();
//            layout.add(edit,delete);
//
//            return layout;
//        })).setHeader("Akcje").setAutoWidth(true);
//        refreshGrid(grid,productRepository);
//    }
//
//    private void refreshGrid(Grid<Product> grid,ProductRepository productRepository) {
//        grid.setDataProvider(new ListDataProvider<>(productRepository.findAllProducts()));
//        grid.getDataProvider().refreshAll();
//    }
//
//    private void createProductNameColumn(Grid<Product> grid) {
//        grid.addColumn(product -> product.getName() != null ? product.getName() : "").setHeader("Nazwa").setAutoWidth(true);
//    }
//
//    private void createProductIdColumn(Grid<Product> grid) {
//        grid.addColumn(product -> product.getId() != null ? product.getId() : "").setHeader("ID").setAutoWidth(true);
//    }
//}
