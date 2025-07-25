package pl.eurokawa.views.layouts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import pl.eurokawa.balance.BalanceBroadcaster;
import pl.eurokawa.balance.BalanceServiceImpl;
import pl.eurokawa.security.SecurityService;
import pl.eurokawa.user.UserType;
import pl.eurokawa.views.HomeView;
import pl.eurokawa.views.account.UserAccountView;
import pl.eurokawa.views.adminPanel.balance.BalanceManualSetterView;
import pl.eurokawa.views.adminPanel.product.ProductManagerViewSOLID;
import pl.eurokawa.views.adminPanel.terms.TermsOfServiceAdderView;
import pl.eurokawa.views.adminPanel.terms.TermsOfServiceListView;
import pl.eurokawa.views.deposits.DepositAdderView;
import pl.eurokawa.views.deposits.DepositConfirmationView;
import pl.eurokawa.views.deposits.DepositListView;
import pl.eurokawa.views.purchase.PurchaseAdderView;
import pl.eurokawa.views.purchase.PurchaseConfirmationView;
import pl.eurokawa.views.purchase.PurchaseHistoryView;
import pl.eurokawa.views.user.UserView;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
public class MainLayout extends AppLayout {

    private static final Logger log = LoggerFactory.getLogger(MainLayout.class);
    private TextField balanceField;
    private final SecurityService securityService;
    private Button loggedUserButton;
    private final BalanceServiceImpl balanceServiceImpl;
    private H1 viewTitle;

    @Autowired
    public MainLayout(BalanceServiceImpl balanceServiceImpl, SecurityService securityService) {
        try {
            this.balanceServiceImpl = balanceServiceImpl;
            this.securityService = securityService;
            this.viewTitle = new H1();

            init(balanceServiceImpl, securityService);
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void init(BalanceServiceImpl balanceServiceImpl, SecurityService securityService) {
        try {
            setPrimarySection(Section.DRAWER);
            addDrawerContent(balanceServiceImpl, securityService);
            addHeaderContent();
        }
        catch (Exception e){
            log.error("Error in postconstruct init inside MainLayout",e);
        }
    }


    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent(BalanceServiceImpl balanceServiceImpl, SecurityService securityService) {
        if (balanceServiceImpl == null || securityService == null){
            log.error("blaaaaaaaaaaaaad wewnatrz addDrawer, cos jest nullem");
        }
        BalanceBroadcaster.register(this::updateBalance);

        UI ui = UI.getCurrent();
        if (ui != null) {
            BalanceBroadcaster.register(newBalance -> {
                ui.access(() -> updateBalance(newBalance));
            });
        }

        Button appName = new Button("Home");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        appName.setAutofocus(true);
        appName.setIcon(new Icon(VaadinIcon.HOME));
        appName.getStyle()
                .set("font-size", "24px");
        appName.addClickListener(event -> {
            UI.getCurrent().navigate("/home");
        });

        Header header = new Header(appName);

        TextField balanceLabel = new TextField();
        balanceLabel.setValue("DOSTĘPNE ŚRODKI");
        balanceLabel.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        balanceLabel.setReadOnly(true);
        balanceLabel.getStyle()
                .set("color", "#05AE5C")
                .set("font-size", "18px");

        balanceField = new TextField();
        balanceField.getStyle()
                .set("font-size", "36px")
                .set("margin-top", "auto")
                .set("color", "#14AE5C");
        balanceField.addClassNames(
                LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.EXTRABOLD,
                LumoUtility.Position.STATIC, LumoUtility.Position.Bottom.XLARGE,
                LumoUtility.TextAlignment.CENTER);
        balanceField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        balanceField.setReadOnly(true);
        balanceField.getElement().getStyle().set("transition", "opacity 0.5s ease-in-out");

        balanceField.setValue(String.valueOf(balanceServiceImpl.getCurrentBalance()));

        balanceField.addValueChangeListener(event ->{
            if (!balanceField.getValue().equals(String.valueOf(balanceServiceImpl.getCurrentBalance()))){
                BalanceBroadcaster.register(this::updateBalance);
            }
        });

        Scroller scroller = new Scroller(createNavigation(securityService));

        addToDrawer(balanceLabel, balanceField, header, scroller, createFooter());
    }

    private void updateBalance(BigDecimal newBalance) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                balanceField.getElement().getStyle().set("opacity", "0");
                ui.setPollInterval(500);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ui.access(() -> {
                            balanceField.setValue(String.valueOf(newBalance));
                            balanceField.getElement().getStyle().set("opacity", "1");
                            ui.setPollInterval(-1);
                        });
                    }
                }, 500);
            });
        }
    }

    private SideNav createNavigation(SecurityService securityService) {
        SideNav navigation = new SideNav();

        SideNavItem people = new SideNavItem("Ludzie", UserView.class, VaadinIcon.MALE.create());

        SideNavItem coffeeMembers = new SideNavItem("Kawosze", HomeView.class,VaadinIcon.COFFEE.create());

        SideNavItem deposit = new SideNavItem("Wpłaty", DepositAdderView.class, VaadinIcon.DOLLAR.create());
        deposit.addItem(new SideNavItem("Historia wpłat", DepositListView.class, VaadinIcon.BOOK_DOLLAR.create()));
        if (securityService.loggedUserHasRole(UserType.ADMIN.name())){
            deposit.addItem(new SideNavItem("Zatwierdzanie wpłat", DepositConfirmationView.class,VaadinIcon.CHECK_CIRCLE.create()));
        }

        SideNavItem shopping = new SideNavItem("Zakupy", PurchaseAdderView.class, VaadinIcon.CART.create());
        shopping.addItem(new SideNavItem("Historia zakupów", PurchaseHistoryView.class, VaadinIcon.LINES_LIST.create()));
        if (securityService.loggedUserHasRole(UserType.ADMIN.name())){
            shopping.addItem(new SideNavItem("Zatwierdzanie zamówień", PurchaseConfirmationView.class,VaadinIcon.CHECK_CIRCLE.create()));
        }

        SideNavItem adminPanel = new SideNavItem("Panel Administratora");
        adminPanel.addItem(new SideNavItem("Regulamin", TermsOfServiceAdderView.class,VaadinIcon.BOOK.create()));
        adminPanel.addItem(new SideNavItem("Historia Regulaminów", TermsOfServiceListView.class,VaadinIcon.LINES_LIST.create()));
        adminPanel.addItem(new SideNavItem("Dostępne środki", BalanceManualSetterView.class,VaadinIcon.BOOK_DOLLAR.create()));
        adminPanel.addItem(new SideNavItem("Produkty", ProductManagerViewSOLID.class,VaadinIcon.COFFEE.create()));
        adminPanel.setVisible(securityService.loggedUserHasRole(UserType.ADMIN.name()));

        coffeeMembers.addItem(deposit,shopping);
        coffeeMembers.setEnabled(securityService.hasAccessToCoffee(securityService.getLoggedUser()));

        navigation.addItem(people,coffeeMembers,adminPanel);
        return navigation;
    }

    private Component createFooter() {
        VerticalLayout verticalLayout = new VerticalLayout();
        Footer userName = new Footer();
        Footer logout = new Footer();

        loggedUserButton = new Button(securityService.getLoggedUserFirstAndLastName());
        loggedUserButton.setIcon(new Icon(VaadinIcon.NURSE));
        loggedUserButton.addClickListener(event -> {
           UI.getCurrent().navigate(UserAccountView.class);
        });
        userName.add(loggedUserButton);

        Button logoutButton = new Button(new Icon(VaadinIcon.POWER_OFF));
        logoutButton.setTooltipText("Wyloguj się");
        logoutButton.addClickListener(event -> {

            Notification notification = Notification.show("Do zobaczenia następnym razem!",2500, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

            UI.getCurrent().getPage().setLocation("/logout");
        });

        logout.add(logoutButton);

        verticalLayout.add(userName,logout);

        return verticalLayout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        setContent(getContent());

        if (loggedUserButton != null){
            loggedUserButton.setText(securityService.getLoggedUserFirstAndLastName());
        }
    }

}
