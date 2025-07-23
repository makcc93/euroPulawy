package pl.eurokawa.balance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BalanceService {
    private static final Logger log = LoggerFactory.getLogger(BalanceService.class);
    private final BalanceRepository balanceRepository;

    public BalanceService(BalanceRepository balanceRepository){
        this.balanceRepository = balanceRepository;
    }

    @Transactional
    public void updateBalance(User user, BigDecimal value, TransactionType transactionType) {
        BigDecimal currentBalance = getCurrentBalance();

        BigDecimal newBalance = switch (transactionType) {
            case DEPOSIT -> currentBalance.add(value);
            case CHECKOUT -> currentBalance.subtract(value);
            case MANUAL -> currentBalance = value;
            default -> throw new IllegalArgumentException("Nieznany typ transakcji " + transactionType);
        };

        Balance actualBalance = new Balance(user, newBalance);

        balanceRepository.save(actualBalance);
    }


    public BigDecimal getCurrentBalance(){
        return Optional.ofNullable(balanceRepository.findLastBalanceValue())
                .map(Balance::getAmount)
                .orElse(BigDecimal.ZERO);
    }
}
