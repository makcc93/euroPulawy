package pl.eurokawa.purchase;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.VaadinService;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.Logger;
import pl.eurokawa.product.Product;
import pl.eurokawa.product.ProductRepository;
import pl.eurokawa.security.S3Config;
import pl.eurokawa.storage.FileType;
import pl.eurokawa.storage.S3Service;
import pl.eurokawa.user.User;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseService{
    private PurchaseRepository purchaseRepository;
    private ProductRepository productRepository;
    private final S3Service s3Service;
    private final S3Config s3Config;

    private static final Logger logger = LogManager.getLogger(PurchaseService.class);

    public PurchaseService(PurchaseRepository purchaseRepository, ProductRepository productRepository, S3Service s3Service, S3Config s3Config){
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.s3Service = s3Service;
        this.s3Config = s3Config;
    }

    public Purchase addPurchase(User user, Integer productId, BigDecimal price, Integer quantity){
        Product product = productRepository.findById(productId).orElseThrow();
        Purchase purchase = new Purchase(user, product, price, quantity);
        purchase.setSaved(true);

        return purchaseRepository.save(purchase);
    }

    public Purchase addPurchase(User user, Integer productId, BigDecimal price, Integer quantity,String receiptImagePath){
        Product product = productRepository.findById(productId).orElseThrow();
        Purchase purchase = new Purchase(user, product, price, quantity,receiptImagePath);
        purchase.setSaved(true);

        return purchaseRepository.save(purchase);
    }


    public Purchase confirmPurchase(Product product, Integer quantity, BigDecimal price, BigDecimal total){
        Purchase purchase =  new Purchase();
        purchase.setProduct(product);
        purchase.setQuantity(quantity);
        purchase.setPrice(price);
        purchase.setTotal(total);

        if (purchase.isSaved() && !purchase.isConfirmed()){
            purchase.setConfirmed(true);
        }

        return purchaseRepository.save(purchase);
    }

    public List<Purchase> getConfirmedPurchases(){

        return purchaseRepository.findAllConfirmedPurchases();
    }

    public Component getPurchasePhoto(FileType fileType,Purchase purchase, S3Service s3Service){
        String folder = fileType.getFolder();
        String key = purchase.getReceiptImagePath();
        logger.info("PURCHASE SERVICE getPurchasePhoto, FOLDER = {}, KEY = {}",folder,key);

        if (key == null) {
            return new H4("Brak obrazu");
        }

        String urlForFrontend = s3Config.getControllerDownloadPrefix() + folder + "/" + key;
        logger.info("PURCHASE SERVICE getPurchasePhoto, urlForFrontend = {}",urlForFrontend);


        s3Service.downloadFileFromS3(FileType.PHOTO,key);

        Image image = new Image(VaadinService.getCurrentRequest().getContextPath() + urlForFrontend,"Photo");
        image.setWidth("90px");
        image.setHeight("90px");
        image.getStyle()
                .set("object-fit","cover")
                .set("border-radius","4px");

        Div wrapper = new Div(image);
        wrapper.getStyle()
                .set("overflow","hidden")
                .set("display","flex")
                .set("justify-content","center")
                .set("align-items", "center");

        image.addClickListener(event ->{
            Dialog dialog = new Dialog();
            dialog.setModal(true);
            dialog.setDraggable(true);
            dialog.setWidth("70vw");
            dialog.setHeight("70vh");

            Div container = new Div();
            container.setWidthFull();
            container.setHeightFull();
            container.getStyle()
                    .set("display", "flex")
                    .set("justify-content", "center")
                    .set("align-items", "center")
                    .set("overflow", "hidden");
            container.add(closeDialogButton(dialog));

            Image fullImageView = new Image(urlForFrontend,"Full view");
            fullImageView.setMaxHeight("100%");
            fullImageView.setMaxWidth("100%");
            fullImageView.getStyle()
                    .set("object-fit","contain")
                    .set("max-height", "70vh");

            container.add(fullImageView);
            dialog.add(container);

            dialog.open();
        });

        return wrapper;
    }

    public List<Purchase> getSavedNotConfirmedPurchases(){

        return purchaseRepository.findAllSavedNotConfirmedPurchases();
    }

    private Button closeDialogButton(Dialog dialog){
        Button button = new Button(VaadinIcon.CLOSE_CIRCLE.create());
        button.addClickListener(event ->{
            dialog.close();
        });

        dialog.add(button);

        button.getStyle()
                .set("position", "absolute")
                .set("top", "var(--lumo-space-s)")
                .set("right", "var(--lumo-space-s)")
                .set("z-index", "1")
                .set("padding", "0.5em")
                .set("min-width", "0");

        return button;
    }
}
