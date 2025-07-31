package pl.eurokawa.balance;

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
    void updateBalanceByDepositTest(){
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

}
