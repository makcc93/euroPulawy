package pl.eurokawa.views.deposits;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Data;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import pl.eurokawa.other.DateFormatter;
import pl.eurokawa.transaction.Transaction;
import pl.eurokawa.transaction.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;

@UIScope
@Data
@PageTitle("Historia wpłat")
@Route("deposithistory")
@Menu(order = 2.1, icon = LineAwesomeIconUrl.PEPPER_HOT_SOLID)
public class DepositListView extends Div {

    private final TransactionRepository transactionRepository;

    private final DateFormatter dateFormatter;

    public DepositListView(TransactionRepository transactionRepository, DateFormatter dateFormatter) {
        this.transactionRepository = transactionRepository;
        this.dateFormatter = dateFormatter;

        List<Transaction> orders = transactionRepository.findAllConfirmedTransactions();

        Grid<Transaction> orderGrid = new Grid<>(Transaction.class,false);

        orderGrid.setItems(orders);
        orderGrid.setAllRowsVisible(true);

        orderGrid.addColumn(Transaction::getUser).setHeader("OSOBA").setAutoWidth(true);

        orderGrid.addColumn(Transaction::getAmount).setHeader("WPŁATA").setAutoWidth(true);

        orderGrid.addColumn(new ComponentRenderer<>(transaction -> new Span(dateFormatter.formatToEuropeWarsaw(String.valueOf(transaction.getCreatedAt())))))
                .setHeader("DATA").setAutoWidth(true);

        add(orderGrid);
    }

}
