package src.functions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class handSize {
    Supplier<Integer> getHandSize(int initialHandSize, String handSizeMode){
        AtomicInteger handSize = new AtomicInteger(initialHandSize);
        switch (handSizeMode){
            case "decreasing":
                return handSize::decrementAndGet;
            case "decreasingCyclic":
                return () -> handSize.get() == 1 ? handSize.getAndSet(initialHandSize) : handSize.get();
            default:
                return () -> initialHandSize;
        }
    }
}
