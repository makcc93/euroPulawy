package pl.eurokawa.balance;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.eurokawa.balance.strategy.BalanceStrategy;
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BalanceServiceImpl implements BalanceService{
    private final BalanceRepository balanceRepository;
    private final List<BalanceStrategy> strategies;

    public BalanceServiceImpl(BalanceRepository balanceRepository, List<BalanceStrategy> strategies){
        this.balanceRepository = balanceRepository;
        this.strategies = strategies;
    }

    @Override
    @Transactional
    public void updateBalance(User user, BigDecimal value, TransactionType transactionType) {
        Objects.requireNonNull(user,"User cannot be null");
        Objects.requireNonNull(value,"Value cannot be null");
        Objects.requireNonNull(transactionType,"TransactionType cannot be null");

        BigDecimal currentBalance = getCurrentBalance();

        BigDecimal newValue = getNewValue(currentBalance,value,transactionType);

        balanceRepository.save(new Balance(user, newValue));
    }

    @Override
    public BigDecimal getCurrentBalance() {
        return Optional.ofNullable(balanceRepository.findLastBalance())
                .map(Balance::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public Balance findUserLastBalanceOperation(Integer userId) {
        Objects.requireNonNull(userId,"User ID cannot be null");

        return Optional.ofNullable(balanceRepository.findUserLastBalanceOperation(userId))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find user last balance record by his id: " + userId));
    }

    private BalanceStrategy findStrategy(TransactionType transactionType){
        return  strategies.stream()
                .filter(strategy -> strategy.supports(transactionType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find strategy for this transaction type"));
    }

    private BigDecimal getNewValue(BigDecimal currentBalance, BigDecimal transactionValue, TransactionType transactionType){
        return findStrategy(transactionType).generateNewBalanceValue(currentBalance,transactionValue);
    }
}
