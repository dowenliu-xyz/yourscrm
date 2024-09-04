package cn.yourscrm.mono.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class NowerTest {
    @Test
    void now_system() throws InterruptedException {
        Instant now = Nower.now();
        TimeUnit.MILLISECONDS.sleep(10);
        Instant later = Nower.now();
        assertTrue(Duration.between(now, later).toMillis() >= 10);
    }

    @Test
    void now_mock() throws InterruptedException {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1);
        Supplier<Instant> oldSupplier = Nower.setSupplier(() -> now);
        try {
            assertEquals(now, Nower.now());
            TimeUnit.MILLISECONDS.sleep(10);
            assertEquals(now, Nower.now());
            Nower.setSupplier(() -> later);
            assertEquals(later, Nower.now());
        } finally {
            Nower.setSupplier(oldSupplier);
        }
    }
}