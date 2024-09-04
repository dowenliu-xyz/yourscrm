package cn.yourscrm.mono.time;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

public final class Nower {
    private Nower() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Supplier<Instant> supplier = Instant::now;

    // Not thread-safe. Unusually, this method is used only in tests.
    @NotNull
    public static Supplier<Instant> setSupplier(@NotNull Supplier<Instant> supplier) {
        Objects.requireNonNull(supplier);
        Supplier<Instant> oldSupplier = Nower.supplier;
        Nower.supplier = supplier;
        return oldSupplier;
    }

    public static Instant now() {
        return supplier.get();
    }
}
