package pl.eurokawa.balance;

import org.hibernate.sql.ast.tree.expression.Over;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class BalanceServiceImpl implements BalanceService{
    private final BalanceRepository balanceRepository;

    public BalanceServiceImpl(BalanceRepository balanceRepository){
        this.balanceRepository = balanceRepository;
    }

    @Override
    @Transactional
    public void updateBalance(User user, BigDecimal value, TransactionType transactionType) {
        BigDecimal currentBalance = getCurrentBalance();

        BigDecimal newBalance = switch (transactionType) {
            case DEPOSIT -> currentBalance.add(value);
            case CHECKOUT -> currentBalance.subtract(value);
            case MANUAL -> value;
            default -> throw new IllegalArgumentException("Nieznany typ transakcji " + transactionType);
        };

        balanceRepository.save(new Balance(user, newBalance));
    }

    @Override
    public BigDecimal getCurrentBalance() {
        return Optional.ofNullable(balanceRepository.findLastBalanceValue())
                .map(Balance::getAmount)
                .orElse(BigDecimal.ZERO);
    }
}
