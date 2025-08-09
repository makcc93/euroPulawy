package pl.eurokawa.balance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.balance.strategy.BalanceStrategy;
import pl.eurokawa.balance.strategy.CheckoutBalanceStrategy;
import pl.eurokawa.balance.strategy.DepositBalanceStrategy;
import pl.eurokawa.balance.strategy.ManualBalanceStrategy;
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceImplTest {

    @Test
    void updateBalance_ByDeposit(){
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
    void updateBalance_ByCheckout(){
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
    void updateBalance_ByManual(){
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
    void updateBalance_NegativeValueInDeposit(){
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
    void updateBalance_NegativeValueInCheckout(){
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
    void updateBalance_UserIsNull(){
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
    void updateBalance_ValueIsNull(){
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
    void updateBalance_TransactionTypeIsNull(){
        User user = new User();
        BigDecimal value = new BigDecimal("1.00");
        TransactionType transactionType = null;

        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        List<BalanceStrategy> strategies = List.of(new DepositBalanceStrategy(),new ManualBalanceStrategy(),new CheckoutBalanceStrategy());

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, strategies);

        BalanceServiceImpl spy = spy(service);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> spy.updateBalance(user, value, transactionType));

        assertEquals("Transaction type cannot be null", exception.getMessage());
    }

    @Test
    void getCurrentBalance_WorkingTest(){
        BigDecimal currentBalance = new BigDecimal("100.00");
        BalanceRepository balanceRepository = mock(BalanceRepository.class);
        when(balanceRepository.findLastBalance()).thenReturn(new Balance(new User(), currentBalance));

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, Collections.emptyList());

        assertEquals(currentBalance,service.getCurrentBalance());
    }

    @Test
    void getCurrentBalance_LastBalanceIsNull(){
        BalanceRepository balanceRepository = mock(BalanceRepository.class);

        when(balanceRepository.findLastBalance()).thenReturn(null);

        BalanceServiceImpl service = new BalanceServiceImpl(balanceRepository, Collections.emptyList());

        BigDecimal expectedValue = new BigDecimal("0.00");

        assertEquals(0,service.getCurrentBalance().compareTo(expectedValue));
    }

    @Test
    void findUserLastBalanceOperation_WorkingTest(){
        User user = mock(User.class);
        when(user.getId()).thenReturn(123);

        BalanceRepository repository = mock(BalanceRepository.class);
        Balance balance = new Balance(user,new BigDecimal("111.00"));

        when(repository.findUserLastBalanceOperation(user.getId())).thenReturn(balance);

        BalanceServiceImpl service = new BalanceServiceImpl(repository,Collections.emptyList());

        assertEquals(balance,service.findUserLastBalanceOperation(user.getId()));
    }

    @Test
    void findUserLastBalanceOperation_UserIdIsNull(){
        BalanceRepository repository = mock(BalanceRepository.class);
       BalanceServiceImpl service = new BalanceServiceImpl(repository,Collections.emptyList());

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.findUserLastBalanceOperation(null));

        assertEquals("User ID cannot be null",exception.getMessage());
    }

    @Test
    void findUserLastBalanceOperation_UserNotFound(){
        BalanceRepository repository = mock(BalanceRepository.class);
        BalanceServiceImpl service = new BalanceServiceImpl(repository,Collections.emptyList());

        when(repository.findUserLastBalanceOperation(1)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.findUserLastBalanceOperation(1));

        assertEquals("Cannot find user last balance by id: 1",exception.getMessage());
    }
}
