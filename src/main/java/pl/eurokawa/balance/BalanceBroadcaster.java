package pl.eurokawa.balance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class BalanceBroadcaster {
    private final static ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final static List<Consumer<BigDecimal>> listeners = new ArrayList<>();

    public static synchronized void register (Consumer<BigDecimal> listener){
        listeners.add(listener);
    }

    public static synchronized void broadcast(BigDecimal newBalance){
        for(Consumer<BigDecimal> listener : listeners){
            executorService.execute(() -> listener.accept(newBalance));
        }
    }
}
