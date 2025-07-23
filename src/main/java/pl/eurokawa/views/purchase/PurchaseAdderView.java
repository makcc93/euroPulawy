package pl.eurokawa.views.purchase;

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
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import pl.eurokawa.product.Product;
import pl.eurokawa.product.ProductService;
import pl.eurokawa.purchase.Purchase;
import pl.eurokawa.purchase.PurchaseRepository;
import pl.eurokawa.purchase.PurchaseService;
import pl.eurokawa.storage.FileAdderService;
import pl.eurokawa.storage.FileConversionService;
import pl.eurokawa.storage.FileType;
import pl.eurokawa.storage.S3Service;
import pl.eurokawa.transaction.TransactionRepository;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserRepository;
import pl.eurokawa.user.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@PageTitle("Zakupy")
@Route("order")
@Menu(order = 1, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
@EnableConfigurationProperties
public class PurchaseAdderView extends VerticalLayout {
    private final ProductService productService;
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FileAdderService fileAdderService;
    private final S3Service s3Service;
    private final TransactionRepository transactionRepository;
    private final FileConversionService fileConversionService;

    private static final Logger logger = LogManager.getLogger(PurchaseAdderView.class);

    private final Grid<Purchase> addPurchaseGrid;
    private final Grid<Purchase> savedPurchasesGrid;
    private final List<Purchase> notSavedUserPurchases = new ArrayList<>();
    private List<Purchase> savedUserPurchases = new ArrayList<>();
    private final ListDataProvider<Purchase> dataProviderForAddPurchase;
    private final ListDataProvider<Purchase> dataProviderForUserPurchases;

    public PurchaseAdderView(ProductService productService, PurchaseService purchaseService, UserService userService,
                             PurchaseRepository purchaseRepository, UserRepository userRepository, FileAdderService fileAdderService, S3Service s3Service, TransactionRepository transactionRepository, FileConversionService fileConversionService) {
        this.productService = productService;
        this.purchaseService = purchaseService;
        this.purchaseRepository = purchaseRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.fileAdderService = fileAdderService;
        this.s3Service = s3Service;
        this.transactionRepository = transactionRepository;
        this.fileConversionService = fileConversionService;
        Component topHeader = firstGridHeader();

        dataProviderForAddPurchase = new ListDataProvider<>(notSavedUserPurchases);

        addPurchaseGrid = new Grid<>(Purchase.class,false);
            addPurchaseGrid.setDataProvider(dataProviderForAddPurchase);
            addPurchaseGrid.setItems(dataProviderForAddPurchase);
            addPurchaseGrid.setAllRowsVisible(true);

        createProductColumn(addPurchaseGrid);
        createQuantityColumn(addPurchaseGrid);
        createPriceColumn(addPurchaseGrid);
        createSumColumn(addPurchaseGrid);
        createPhotoAdderColumn(addPurchaseGrid,s3Service);

        createActionsButtons(addPurchaseGrid);
        addEmptyRow();

        Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
            plusButton.setTooltipText("Dodaj kolejne zamówienie");
            plusButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
            plusButton.addClickListener(event -> addEmptyRow());

        Component bottomHeader = secondGridHeader();

        savedUserPurchases = purchaseRepository.findUserSavedPurchases(loggedUser().getId());
        dataProviderForUserPurchases = new ListDataProvider<>(savedUserPurchases);

        savedPurchasesGrid = new Grid<>(Purchase.class,false);
            savedPurchasesGrid.setDataProvider(dataProviderForUserPurchases);
            savedPurchasesGrid.setItems(savedUserPurchases);

        createColumnsUserSavedPurchases(savedPurchasesGrid,s3Service);

        add(topHeader,addPurchaseGrid, plusButton, bottomHeader, savedPurchasesGrid);
    }

    private void createColumnsUserSavedPurchases(Grid<Purchase> grid, S3Service s3Service){
        grid.addColumn(Purchase::getId).setHeader("NR").setWidth("50px");

        grid.addColumn(Purchase::getProduct).setHeader("PRODUKT").setAutoWidth(true);

        grid.addColumn(Purchase::getQuantity).setHeader("ILOŚĆ").setWidth("50px");

        grid.addColumn(Purchase::getPrice).setHeader("CENA").setAutoWidth(true);

        grid.addColumn(Purchase::getTotal).setHeader("WARTOŚĆ").setWidth("50px");

        grid.addColumn(new ComponentRenderer<>(purchase -> purchaseService.getPurchasePhoto(FileType.PHOTO,purchase,s3Service))) //testtest
                .setHeader("DOWÓD ZAKUPU").setAutoWidth(true);
    }

    private Component firstGridHeader(){
        return new H2("WPROWADZANIE ZAKUPU");
    }

    private Component secondGridHeader(){
        return new H2("TWOJE ZAKUPY OCZEKUJĄCE NA ZATWIERDZENIE");
    }

    private void createPhotoAdderColumn(Grid<Purchase> grid,S3Service s3Service){
        grid.addColumn(new ComponentRenderer<>(purchase ->{

            if (purchase.isSaved() && purchase.getReceiptImagePath() != null){
                return purchaseService.getPurchasePhoto(FileType.PHOTO,purchase,s3Service);
            }

            MemoryBuffer memoryBuffer = new MemoryBuffer();
            Upload upload = new Upload(memoryBuffer);
                upload.setVisible(uploadButtonVisibilityCheck(purchase));
                upload.setAcceptedFileTypes("image/*", ".jpg", ".jpeg", ".png", ".heic", ".heif", ".webp");
                upload.setMaxFileSize(10 * 1024 * 1024);
                upload.setUploadButton(VaadinIcon.CAMERA.create());
                upload.setDropAllowed(false);
                upload.setAutoUpload(true);

            upload.addSucceededListener(succeededEvent -> {
                try(InputStream inputStream = memoryBuffer.getInputStream()) {
                    String secureFileName = fileConversionService.generateSecureFileName(succeededEvent.getFileName());

                    fileAdderService.uploadFile(FileType.PHOTO,inputStream,secureFileName);
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

        notSavedUserPurchases.add(purchase);
        refreshGrid(addPurchaseGrid);
    }

    private void deleteRow(Purchase purchase){
        notSavedUserPurchases.remove(purchase);
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

                if (value == null) {
                    priceField.setValue("0.00");
                }
            });

            return priceField;
        })).setHeader("CENA").setAutoWidth(true);
    }

    private void createSumColumn(Grid<Purchase> grid) {
        grid.addColumn(purchase -> String.format("%.2f", purchase.getTotal()))
                .setHeader("SUMA").setAutoWidth(true).setAutoWidth(true);
    }

    private void createActionsButtons(Grid<Purchase> grid){
        grid.addColumn(new ComponentRenderer<>(purchase -> {
            Button save = new Button(new Icon(VaadinIcon.CHECK));
            save.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
            save.setTooltipText("Zapisz");

            save.setVisible(!purchase.isSaved());

            save.addClickListener(event -> {

                if (purchase.getProduct() != null && purchase.getPrice() != null && purchase.getPrice().compareTo(BigDecimal.ZERO) != 0) {
                    User user = loggedUser();

                    if (purchase.getReceiptImagePath() != null){
                        purchaseService.addPurchase(user, purchase.getProduct().getId(), purchase.getPrice(), purchase.getQuantity(),purchase.getReceiptImagePath());

                    } else {
                        purchaseService.addPurchase(user, purchase.getProduct().getId(), purchase.getPrice(), purchase.getQuantity());
                    }
                    purchase.setSaved(true);
                    notSavedUserPurchases.remove(purchase);
                    savedUserPurchases.add(purchase);

//                    savedUserPurchases = purchaseRepository.findUserSavedPurchases(loggedUser().getId());
//                    dataProviderForUserPurchases.refreshAll();

                    refreshListGrid(savedPurchasesGrid,purchaseRepository);

                    addEmptyRow();

                    Notification notification = Notification.show("Poprawnie dodano zakup");
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notification.setDuration(3000);
                }
                else {
                    Notification notification = Notification.show("Uzupełnij poprawnie wszystkie dane");
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setDuration(3000);
                }
            });

            Button reset = new Button(new Icon(VaadinIcon.CLOSE));
            reset.addThemeVariants(ButtonVariant.LUMO_ERROR);
            reset.setTooltipText("Resetuj");
            reset.setVisible(!purchase.isSaved());

            reset.addClickListener(event -> {
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

                Notification notification = Notification.show("Zresetowano");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.setDuration(1500);
            });

            Button delete = new Button(new Icon(VaadinIcon.TRASH));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            delete.setTooltipText("Usuń");
            delete.setVisible(!purchase.isConfirmed());

            delete.addClickListener(event ->{
                if (purchase.getId() == null){
                    deleteRow(purchase);
                    logger.info("SHOOPING VIEW, DELETE ADD CLICK LISTENER DLA P.ID == NULL");

                    return;
                }

                notSavedUserPurchases.remove(purchase);
                savedUserPurchases.remove(purchase);
                purchaseRepository.deletePurchaseById(purchase.getId());

                if (purchase.getReceiptImagePath() != null){
                    s3Service.deleteFileFromS3(FileType.PHOTO,purchase.getReceiptImagePath());
                    purchase.setReceiptImagePath(null);
                }

                dataProviderForAddPurchase.getItems().remove(purchase);
                dataProviderForAddPurchase.refreshAll();
                refreshGrid(addPurchaseGrid);

                refreshListGrid(savedPurchasesGrid,purchaseRepository);

                Notification notification = Notification.show("Usunięto");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(3000);
            });

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.add(save, reset,delete);

            return horizontalLayout;
        }
        )).setHeader("AKCJE").setAutoWidth(true);

        refreshGrid(grid);
    }

    private void refreshGrid(Grid<Purchase> grid){
        grid.getDataProvider().refreshAll();
    }

    private User loggedUser(){
        Authentication authentication = VaadinSession.getCurrent().getAttribute(Authentication.class);
        String userEmail = authentication.getName();

        return userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Nie rozpoznano zalogowanego użytkownika!"));
    }

    private boolean uploadButtonVisibilityCheck(Purchase purchase){
        return !purchase.isSaved() && !purchase.isConfirmed() && purchase.getPrice().compareTo(BigDecimal.ZERO) != 0 && purchase.getQuantity() != 0;
    }

    private void refreshListGrid(Grid<Purchase> grid, PurchaseRepository purchaseRepository){
        grid.setDataProvider(new ListDataProvider<>(purchaseRepository.findUserSavedPurchases(loggedUser().getId())));
        grid.getDataProvider().refreshAll();
    }
}
