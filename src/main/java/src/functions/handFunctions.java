package src.functions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class handFunctions {
    public static Supplier<Integer> getHandSize(int initialHandSize, int minimumHandSize, String handSizeMode){
        AtomicInteger handSize = new AtomicInteger(initialHandSize);
        switch (handSizeMode){
            case "decreasing":
                return handSize::decrementAndGet;
            case "decreasingCyclic":
                return () -> handSize.get() == minimumHandSize + 1 ? handSize.getAndSet(initialHandSize) : handSize.get();
            default:
                return () -> initialHandSize;
        }
    }
}
