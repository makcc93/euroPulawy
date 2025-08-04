package pl.eurokawa.balance;

import org.apache.commons.validator.Arg;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;
import pl.eurokawa.balance.strategy.BalanceStrategy;
import pl.eurokawa.balance.strategy.CheckoutBalanceStrategy;
import pl.eurokawa.balance.strategy.DepositBalanceStrategy;
import pl.eurokawa.balance.strategy.ManualBalanceStrategy;
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceImplTest {
    
    @Test
    void updateBalanceByDeposit(){
        User user = mock(User.class);
        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);

        doReturn(new BigDecimal("100.00")).when(spy).getCurrentBalance();

        BigDecimal depositValue = new BigDecimal("23.45");

        spy.updateBalance(user,depositValue, TransactionType.DEPOSIT);

        ArgumentCaptor<Balance> captor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        Balance newBalance = captor.getValue();

        assertEquals(new BigDecimal("123.45"),newBalance.getAmount());
        assertEquals(user,newBalance.getUser());
    }

    @Test
    void updateBalanceByCheckout(){
        User user = mock(User.class);
        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);

        BigDecimal checkoutValue = new BigDecimal("90.00");

        doReturn(new BigDecimal("100.00")).when(spy).getCurrentBalance();

        spy.updateBalance(user,checkoutValue,TransactionType.CHECKOUT);

        ArgumentCaptor<Balance> captor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        Balance newBalance = captor.getValue();

        assertEquals(new BigDecimal("10.00"),newBalance.getAmount());
        assertEquals(user,newBalance.getUser());
    }

    @Test
    void updateBalanceByManual(){
        User user = mock(User.class);
        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);

        BigDecimal manualValue = new BigDecimal("500.00");

        doReturn(new BigDecimal("100.00")).when(spy).getCurrentBalance();

        spy.updateBalance(user,manualValue,TransactionType.MANUAL);

        ArgumentCaptor<Balance> captor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(captor.capture());
        Balance newBalance = captor.getValue();

       assertEquals(new BigDecimal("500.00"),newBalance.getAmount());
       assertEquals(user,newBalance.getUser());
    }

    @Test
    void updateBalanceCheckMinusValueInDeposit(){
        User user = mock(User.class);
        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);
        BigDecimal minusValue = new BigDecimal("-10.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> spy.updateBalance(user, minusValue, TransactionType.DEPOSIT));

        assertEquals("Transaction value cannot be negative", exception.getMessage());
    }

    @Test
    void updateBalanceCheckMinusValueInCheckout(){
        User user = mock(User.class);
        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);
        BigDecimal minusValue = new BigDecimal("-10.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> spy.updateBalance(user, minusValue, TransactionType.CHECKOUT));

        assertEquals("Transaction value cannot be negative", exception.getMessage());
    }

    @Test
    void updateBalanceCheckUserIsNull(){
        User user = null;
        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> spy.updateBalance(user, new BigDecimal("1.00"), TransactionType.MANUAL));

        assertEquals("User cannot be null",exception.getMessage());
    }

    @Test
    void updateBalanceCheckValueIsNull(){
        User user = new User();
        BigDecimal value = null;

        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> spy.updateBalance(user, value, TransactionType.MANUAL));

        assertEquals("Value cannot be null", exception.getMessage());
    }

    @Test
    void updateBalanceCheckTransactionTypeIsNull(){
        User user = new User();
        BigDecimal value = new BigDecimal("1.00");
        TransactionType transactionType = null;

        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> spy.updateBalance(user, value, transactionType));

        assertEquals("TransactionType cannot be null", exception.getMessage());
    }

}
