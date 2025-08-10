package pl.eurokawa.frontend.purchase;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import pl.eurokawa.file.FileService;
import pl.eurokawa.file.FileType;
import pl.eurokawa.file.conversion.NameConversionService;
import pl.eurokawa.product.Product;
import pl.eurokawa.product.ProductService;
import pl.eurokawa.purchase.DTO.CreatePurchaseRequest;
import pl.eurokawa.purchase.Purchase;
import pl.eurokawa.purchase.PurchaseService;
import pl.eurokawa.storage.S3Service;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Zakupy")
@Route("order")
@Menu(order = 1, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
@EnableConfigurationProperties
public class PurchaseAdderView extends VerticalLayout {
    private final ProductService productService;
    private final UserService userService;
    private final FileService fileService;
    private final S3Service s3Service;
    private final NameConversionService nameConversionService;
    private final PurchaseService purchaseService;

    private static final Logger logger = LogManager.getLogger(PurchaseAdderView.class);

    private final Grid<Purchase> addPurchaseGrid;
    private final Grid<Purchase> savedPurchasesGrid;
    private final List<Purchase> notSavedPurchases = new ArrayList<>();
    private List<Purchase> savedPurchases = new ArrayList<>();
    private final ListDataProvider<Purchase> dataProviderForAddPurchase;
    private final ListDataProvider<Purchase> dataProviderForUserPurchases;

    public PurchaseAdderView(ProductService productService, UserService userService, FileService fileService, S3Service s3Service, NameConversionService nameConversionService, PurchaseService purchaseService) {
        this.productService = productService;
        this.userService = userService;
        this.fileService = fileService;
        this.s3Service = s3Service;
        this.nameConversionService = nameConversionService;
        this.purchaseService = purchaseService;
        Component topHeader = firstGridHeader();

        dataProviderForAddPurchase = new ListDataProvider<>(notSavedPurchases);

        addPurchaseGrid = new Grid<>(Purchase.class,false);
            addPurchaseGrid.setDataProvider(dataProviderForAddPurchase);
            addPurchaseGrid.setItems(dataProviderForAddPurchase);
            addPurchaseGrid.setAllRowsVisible(true);

        createProductColumn(addPurchaseGrid);
        createQuantityColumn(addPurchaseGrid);
        createPriceColumn(addPurchaseGrid);
        createSumColumn(addPurchaseGrid);
        createPhotoAdderColumn(addPurchaseGrid, s3Service);

        createActionsButtons(addPurchaseGrid);
        addEmptyRow();

        Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
            plusButton.setTooltipText("Dodaj kolejne zamówienie");
            plusButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
            plusButton.addClickListener(event -> addEmptyRow());

        Component bottomHeader = secondGridHeader();

        savedPurchases = purchaseService.findUserSavedPurchases(loggedUser().getId());
        dataProviderForUserPurchases = new ListDataProvider<>(savedPurchases);

        savedPurchasesGrid = new Grid<>(Purchase.class,false);
            savedPurchasesGrid.setDataProvider(dataProviderForUserPurchases);
            savedPurchasesGrid.setItems(savedPurchases);

        createColumnsUserSavedPurchases(savedPurchasesGrid, s3Service);

        add(topHeader,addPurchaseGrid, plusButton, bottomHeader, savedPurchasesGrid);
    }

    private void createColumnsUserSavedPurchases(Grid<Purchase> grid, S3Service s3Service){
        grid.addColumn(Purchase::getId).setHeader("NR").setWidth("50px");

        grid.addColumn(Purchase::getProduct).setHeader("PRODUKT").setAutoWidth(true);

        grid.addColumn(Purchase::getQuantity).setHeader("ILOŚĆ").setWidth("50px");

        grid.addColumn(Purchase::getPrice).setHeader("CENA").setAutoWidth(true);

        grid.addColumn(Purchase::getTotal).setHeader("WARTOŚĆ").setWidth("50px");

        grid.addColumn(new ComponentRenderer<>(purchase -> purchaseService.getPurchasePhoto(FileType.PHOTO,purchase, s3Service)))
                .setHeader("DOWÓD ZAKUPU").setAutoWidth(true);
    }

    private Component firstGridHeader(){
        return new H2("WPROWADZANIE ZAKUPU");
    }

    private Component secondGridHeader(){
        return new H2("TWOJE ZAKUPY OCZEKUJĄCE NA ZATWIERDZENIE");
    }

    private void createPhotoAdderColumn(Grid<Purchase> grid, S3Service s3Service){
        grid.addColumn(new ComponentRenderer<>(purchase ->{

            if (purchase.isSaved() && purchase.getReceiptImagePath() != null){
                return purchaseService.getPurchasePhoto(FileType.PHOTO,purchase, s3Service);
            }

            MemoryBuffer memoryBuffer = new MemoryBuffer();
            Upload upload = new Upload(memoryBuffer);
                configureUpload(upload,purchase);

            upload.addSucceededListener(succeededEvent -> {
                try(InputStream inputStream = memoryBuffer.getInputStream()) {
                    String secureFileName = nameConversionService.generateSecureFileName(succeededEvent.getFileName());

                    fileService.uploadFile(FileType.PHOTO,inputStream,secureFileName);
                    purchase.setReceiptImagePath(secureFileName);

                    Notification.show("Zdjęcie dodano prawidłowo",5000, Notification.Position.TOP_CENTER);
                } catch (IOException e) {
                    new Notification("Błąd dodania pliku!",5000, Notification.Position.TOP_CENTER);
                    throw new RuntimeException(e);
                }
            });

            upload.addStartedListener(startedEvent -> {
                logger.info("UPLOAD STARTED: {}", startedEvent.getFileName());
            });

            upload.addFailedListener(failedEvent -> {
                logger.info("UPLOAD FAILED: {}", String.valueOf(failedEvent.getReason()));
            });

            upload.addFinishedListener(finishedEvent -> {
                logger.info("UPLOAD FINISHED");
            });

            return upload;
        })).setHeader("ZDJĘCIE").setAutoWidth(true);
    }

    private void addEmptyRow() {
        Purchase purchase = new Purchase();
        purchase.setQuantity(1);
        purchase.setPrice(BigDecimal.valueOf(0.00));
        purchase.setTotal(BigDecimal.valueOf(0.00));

        notSavedPurchases.add(purchase);
        refreshGrid(addPurchaseGrid);
    }

    private void createProductColumn(Grid<Purchase> grid) {
        grid.addColumn(new ComponentRenderer<>(purchase -> {
        ComboBox<Product> comboBox = new ComboBox<>();
        comboBox.setItemLabelGenerator(Product::getName);
        comboBox.setItems(productService.getAllProducts());
        comboBox.setClearButtonVisible(true);
        comboBox.setValue(purchase.getProduct());
        comboBox.setReadOnly(purchase.isSaved());

        comboBox.addValueChangeListener(event -> {
            Product selectedProduct = event.getValue();
            purchase.setProduct(selectedProduct);

            if (!productService.getAllProducts().contains(selectedProduct)) {
                Notification n = Notification.show("Błędnie wybrany towar");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            refreshGrid(grid);
        });

        return comboBox;
    })).setHeader("PRODUKT").setAutoWidth(true);
}

    private void createQuantityColumn(Grid<Purchase> grid) {
        grid.addColumn(new ComponentRenderer<>(purchase -> {
            Button addNewProductButton = new Button();
            addNewProductButton.setIcon(new Icon(VaadinIcon.PLUS));

            ComboBox<Integer> comboBox = new ComboBox<>();
            comboBox.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            comboBox.setValue(1);
            comboBox.setValue(purchase.getQuantity());
            comboBox.setReadOnly(purchase.isSaved());

            comboBox.addValueChangeListener(event -> {
                purchase.setQuantity(event.getValue());
                purchase.updateTotal();
                refreshGrid(grid);
            });

            return comboBox;
        })).setHeader("ILOŚĆ").setAutoWidth(true);
    }

    private void createPriceColumn(Grid<Purchase> grid) {
        grid.addColumn(new ComponentRenderer<>(purchase -> {
            TextField priceField = new TextField();
            priceField.setClearButtonVisible(true);
            priceField.setPlaceholder("Wpisz kwotę");
            priceField.setValue(String.format("%.2f",purchase.getPrice()));
            priceField.setReadOnly(purchase.isSaved());

            priceField.addValueChangeListener(event -> {
                String value = event.getValue().replace(",", ".");
                priceField.setValue(value);

                try {
                    BigDecimal price = new BigDecimal(value);
                    if (price.compareTo(BigDecimal.ZERO) < 0) {
                        Notification notification = Notification.show("Cena nie może być ujemna!");
                        notification.setPosition(Notification.Position.BOTTOM_CENTER);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                    else {
                        purchase.setPrice(price);
                    }
                } catch (NumberFormatException e) {
                    Notification n = Notification.show("Wprowadź poprawną cenę");
                    n.setPosition(Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                purchase.updateTotal();
                refreshGrid(grid);
            });

            return priceField;
        })).setHeader("CENA").setAutoWidth(true);
    }

    private void createSumColumn(Grid<Purchase> grid) {
        grid.addColumn(purchase -> String.format("%.2f", purchase.getTotal()))
                .setHeader("SUMA").setAutoWidth(true).setAutoWidth(true);
    }

    private void configureUpload(Upload upload, Purchase purchase){
        upload.setVisible(uploadButtonVisibilityCheck(purchase));
        upload.setAcceptedFileTypes("image/*", ".jpg", ".jpeg", ".png", ".heic", ".heif", ".webp");
        upload.setMaxFileSize(10 * 1024 * 1024);
        upload.setUploadButton(VaadinIcon.CAMERA.create());
        upload.setDropAllowed(false);
        upload.setAutoUpload(true);
    }

    private void createActionsButtons(Grid<Purchase> grid){
        grid.addColumn(new ComponentRenderer<>(purchase -> {
            Button save = new Button(new Icon(VaadinIcon.CHECK));
            save.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
            save.setTooltipText("Zapisz");
            save.setVisible(!purchase.isSaved());

            save.addClickListener(event -> {
                if (isCorrectChosen(purchase)){
                    onSaveButtonClick(loggedUser(),purchase);

                    Notification.show("Poprawnie dodano zakup",3000, Notification.Position.MIDDLE);
                }
                else {
                    Notification.show("Uzupełnij poprawnie wszystkie dane",3000, Notification.Position.MIDDLE);
                }
            });

            Button reset = new Button(new Icon(VaadinIcon.CLOSE));
            reset.addThemeVariants(ButtonVariant.LUMO_ERROR);
            reset.setTooltipText("Resetuj");
            reset.setVisible(!purchase.isSaved());

            reset.addClickListener(event -> {
                onResetButtonClick(purchase);

               Notification.show("Zresetowano",3000, Notification.Position.MIDDLE);
            });

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.add(save, reset);

            return horizontalLayout;
        }
        )).setHeader("AKCJE").setAutoWidth(true);

        refreshGrid(grid);
    }

    private void onSaveButtonClick(User user, Purchase purchase){
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                purchase.getProduct().getId(),
                purchase.getPrice(),
                purchase.getQuantity(),
                purchase.getReceiptImagePath());

        purchaseService.createPurchase(user,request);

        notSavedPurchases.remove(purchase);
        savedPurchases.add(purchase);

        refreshListGrid(savedPurchasesGrid,purchaseService);

        addEmptyRow();
    }

    private void onResetButtonClick(Purchase purchase){
        purchase.setProduct(null);
        purchase.setQuantity(1);
        purchase.setPrice(BigDecimal.valueOf(0.00));
        purchase.updateTotal();

        if (purchase.getReceiptImagePath() != null){
            s3Service.deleteFileFromS3(FileType.PHOTO,purchase.getReceiptImagePath());
            purchase.setReceiptImagePath(null);
        }

        refreshGrid(addPurchaseGrid);
        dataProviderForAddPurchase.refreshItem(purchase);
    }

    private boolean isCorrectChosen(Purchase purchase){
        if (purchase.getProduct() == null || purchase.getQuantity() == null || purchase.getPrice().compareTo(BigDecimal.ZERO) > 0){
            return false;
        }

        return true;
    }

    private void refreshGrid(Grid<Purchase> grid){
        grid.getDataProvider().refreshAll();
    }

    private User loggedUser(){
        Authentication authentication = VaadinSession.getCurrent().getAttribute(Authentication.class);
        String userEmail = authentication.getName();

        return userService.getByEmail(userEmail);
    }

    private boolean uploadButtonVisibilityCheck(Purchase purchase){
        return !purchase.isSaved() && !purchase.isConfirmed() && purchase.getPrice().compareTo(BigDecimal.ZERO) != 0 && purchase.getQuantity() != 0;
    }

    private void refreshListGrid(Grid<Purchase> grid, PurchaseService purchaseService){
        grid.setDataProvider(new ListDataProvider<>(purchaseService.findUserSavedPurchases(loggedUser().getId())));
        grid.getDataProvider().refreshAll();
    }
}
