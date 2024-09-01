package cn.yourscrm.commons.concurrent.singleFlight;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SingleFlightTest {
    @Test
    void execute_should_behave_as_same_single_thread() {
        SingleFlight singleFlight = new SingleFlight();
        Object key = new Object();
        String result = "result";
        String actual = assertDoesNotThrow(() -> singleFlight.execute(key, () -> result));
        assertEquals(result, actual);
        assertThrows(IllegalStateException.class, () -> singleFlight.execute(key, () -> {
            throw new IllegalStateException();
        }));
        assertTrue(singleFlight.calls.isEmpty());
    }

    @Test
    void execute_different_key_should_not_share_result() {
        SingleFlight singleFlight = new SingleFlight();
        Object key1 = new Object();
        Object key2 = new Object();
        String result1 = "result1";
        String result2 = "result2";
        AtomicInteger count = new AtomicInteger();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var enter1 = new CountDownLatch(1);
            var block1 = new CountDownLatch(1);
            var enter2 = new CountDownLatch(1);
            var done = new CountDownLatch(2);
            executor.execute(() -> {
                try {
                    assertEquals(result1, singleFlight.execute(key1, () -> {
                        enter1.countDown();
                        block1.await();
                        count.incrementAndGet();
                        return result1;
                    }));
                } catch (Throwable e) {
                    fail(e);
                } finally {
                    done.countDown();
                }
            });
            enter1.await();
            executor.execute(() -> {
                enter2.countDown();
                try {
                    assertEquals(result2, singleFlight.execute(key2, () -> {
                        count.incrementAndGet();
                        return result2;
                    }));
                } catch (Throwable e) {
                    fail(e);
                } finally {
                    done.countDown();
                }
            });
            enter2.await();
            block1.countDown();
            done.await();
        } catch (InterruptedException e) {
            fail(e);
        }
        assertEquals(2, count.get());
        assertTrue(singleFlight.calls.isEmpty());
    }

    @Test
    void execute_should_share_result_concurrently() {
        SingleFlight singleFlight = new SingleFlight();
        Object key = new Object();
        String result = "result";
        AtomicInteger count = new AtomicInteger();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var enter1 = new CountDownLatch(1);
            var block1 = new CountDownLatch(1);
            var enter2 = new CountDownLatch(1);
            var done = new CountDownLatch(2);
            executor.execute(() -> {
                try {
                    assertEquals(result, singleFlight.execute(key, () -> {
                        enter1.countDown();
                        block1.await();
                        count.incrementAndGet();
                        return result;
                    }));
                } catch (Throwable e) {
                    fail(e);
                } finally {
                    done.countDown();
                }
            });
            enter1.await();
            executor.execute(() -> {
                enter2.countDown();
                try {
                    assertEquals(result, singleFlight.execute(key, () -> {
                        count.incrementAndGet();
                        return result;
                    }));
                } catch (Throwable e) {
                    fail(e);
                } finally {
                    done.countDown();
                }
            });
            enter2.await();
            TimeUnit.MILLISECONDS.sleep(10);
            block1.countDown();
            done.await();
        } catch (InterruptedException e) {
            fail(e);
        }
        assertEquals(1, count.get());
        assertTrue(singleFlight.calls.isEmpty());
    }

    @Test
    void execute_should_share_throwable_concurrently() {
        SingleFlight singleFlight = new SingleFlight();
        Object key = new Object();
        Exception exception = new IllegalStateException();
        AtomicInteger count = new AtomicInteger();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var enter1 = new CountDownLatch(1);
            var block1 = new CountDownLatch(1);
            var enter2 = new CountDownLatch(1);
            var done = new CountDownLatch(2);
            executor.execute(() -> {
                try {
                    assertThrows(IllegalStateException.class, () -> singleFlight.execute(key, () -> {
                        enter1.countDown();
                        block1.await();
                        count.incrementAndGet();
                        throw exception;
                    }));
                } catch (Throwable e) {
                    fail(e);
                } finally {
                    done.countDown();
                }
            });
            enter1.await();
            executor.execute(() -> {
                enter2.countDown();
                try {
                    assertThrows(IllegalStateException.class, () -> singleFlight.execute(key, () -> {
                        count.incrementAndGet();
                        throw exception;
                    }));
                } catch (Throwable e) {
                    fail(e);
                } finally {
                    done.countDown();
                }
            });
            enter2.await();
            TimeUnit.MILLISECONDS.sleep(10);
            block1.countDown();
            done.await();
        } catch (InterruptedException e) {
            fail(e);
        }
        assertEquals(1, count.get());
        assertTrue(singleFlight.calls.isEmpty());
    }
}