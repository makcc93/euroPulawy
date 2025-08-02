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
import pl.eurokawa.transaction.TransactionType;
import pl.eurokawa.user.User;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceImplTest {

    @Mock
    BalanceRepository balanceRepository;

    @InjectMocks
    BalanceServiceImpl balanceServiceImpl;

    @Test
    void updateBalanceByDeposit(){
        User user = mock(User.class);

        BalanceServiceImpl spy = spy(balanceServiceImpl);
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
        BalanceServiceImpl spy = spy(balanceServiceImpl);

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
        BalanceServiceImpl spy = spy(balanceServiceImpl);

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
        BigDecimal minusValue = new BigDecimal("-10.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> balanceServiceImpl.updateBalance(user, minusValue, TransactionType.DEPOSIT));

        assertEquals("Value cannot be negative", exception.getMessage());
    }

    @Test
    void updateBalanceCheckMinusValueInCheckout(){
        User user = mock(User.class);
        BigDecimal minusValue = new BigDecimal("-10.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> balanceServiceImpl.updateBalance(user, minusValue, TransactionType.CHECKOUT));

        assertEquals("Value cannot be negative", exception.getMessage());
    }

    @Test
    void updateBalanceCheckUserIsNull(){
        User user = null;

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> balanceServiceImpl.updateBalance(user, new BigDecimal("1.00"), TransactionType.MANUAL));

        assertEquals("User cannot be null",exception.getMessage());
    }

    @Test
    void updateBalanceCheckValueIsNull(){
        User user = new User();
        BigDecimal value = null;

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> balanceServiceImpl.updateBalance(user, value, TransactionType.MANUAL));

        assertEquals("Value cannot be null", exception.getMessage());
    }

    @Test
    void updateBalanceCheckTransactionTypeIsNull(){
        User user = new User();
        BigDecimal value = new BigDecimal("1.00");
        TransactionType transactionType = null;

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> balanceServiceImpl.updateBalance(user, value, transactionType));

        assertEquals("TransactionType cannot be null", exception.getMessage());
    }

}
