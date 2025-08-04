package pl.eurokawa.purchase;

import com.vaadin.flow.component.Component;
import pl.eurokawa.file.FileType;
import pl.eurokawa.purchase.DTO.CreatePurchaseRequest;
import pl.eurokawa.storage.S3Service;
import pl.eurokawa.user.User;

import java.util.List;

public interface PurchaseService {
    void createPurchase(User user, CreatePurchaseRequest request);
    Component getPurchasePhoto(FileType fileType, Purchase purchase, S3Service s3Service);
    List<Purchase> getSavedNotConfirmedPurchases();
    List<Purchase> findAllConfirmedPurchases();
    List<Purchase> findUserConfirmedPurchases(Integer userId);
    List<Purchase> findUserSavedPurchases(Integer userId);
    List<Purchase> getConfirmedPurchases();
}
