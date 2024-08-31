package cn.yourscrm.common.id;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static cn.yourscrm.common.id.SnowflakeIdGenerator.Builder.*;
import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {
    @Test
    void builder_default() {
        var g = SnowflakeIdGenerator.builder().build();
        assertNotNull(g);
        assertEquals(SnowflakeIdGenerator.Builder.DEFAULT_EPOCH_BITS, g.getEpochBits());
        assertEquals(DEFAULT_NODE_ID_BITS, g.getNodeIdBits());
        assertEquals(SnowflakeIdGenerator.Builder.DEFAULT_SEQUENCE_BITS, g.getSequenceBits());
        assertEquals((1L << g.getSequenceBits()) - 1, g.getMaxSequence());
        assertTrue(g.getNodeId() >= 0);
        assertTrue(g.getNodeId() < (1L << g.getNodeIdBits()));
        assertTrue(g.getCustomEpoch() >= 0);
        assertTrue(g.getCustomEpoch() <= Instant.now().toEpochMilli());
        assertEquals(-1L, g.getLastTimestamp());
        assertEquals(0L, g.getSequence());
    }

    @Test
    void builder_nodeIdBitsTooSmall() {
        assertDoesNotThrow(() -> SnowflakeIdGenerator.builder().nodeIdBits(4).build());
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder().nodeIdBits(3).build());
    }

    @Test
    void builder_nodeIdBitsTooLarge() {
        assertDoesNotThrow(() ->
                SnowflakeIdGenerator.builder().nodeIdBits(DEFAULT_NODE_ID_BITS).build());
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder().nodeIdBits(DEFAULT_NODE_ID_BITS + 1).build());
    }

    @Test
    void builder_sequenceBitsNotPositive() {
        assertDoesNotThrow(() -> SnowflakeIdGenerator.builder().sequenceBits(1).build());
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder().sequenceBits(0).build());
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder().sequenceBits(-1).build());
    }

    @Test
    void builder_epochBitsTooSmall() {
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder().sequenceBits(DEFAULT_SEQUENCE_BITS + 1).build());
    }

    @Test
    void builder_customEpochShouldNotBeInFuture() {
        assertDoesNotThrow(() ->
                SnowflakeIdGenerator.builder().customEpoch(Instant.now().toEpochMilli()).build());
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder().customEpoch(Instant.now().toEpochMilli() + 1).build());
    }

    @Test
    void builder_customEpochTooOld() {
        assertDoesNotThrow(() ->
                SnowflakeIdGenerator.builder().customEpoch(Instant.now().toEpochMilli()).build());
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder()
                        .customEpoch(Instant.now().toEpochMilli() - (1L << DEFAULT_EPOCH_BITS))
                        .build());
    }

    @Test
    void builder_nodeId() {
        long nodeId = 123;
        SnowflakeIdGenerator g = SnowflakeIdGenerator.builder().nodeId(nodeId).build();
        assertEquals(nodeId, g.getNodeId());
    }

    @Test
    void builder_nodeIdShouldNotBeNegative() {
        assertDoesNotThrow(() -> SnowflakeIdGenerator.builder().nodeId(0).build());
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder().nodeId(-1).build());
    }

    @Test
    void builder_nodeIdTooLarge() {
        assertDoesNotThrow(() ->
                SnowflakeIdGenerator.builder().nodeId((1L << DEFAULT_NODE_ID_BITS) - 1).build());
        assertThrows(IllegalArgumentException.class, () ->
                SnowflakeIdGenerator.builder().nodeId(1L << DEFAULT_NODE_ID_BITS).build());
    }

    @Test
    void nextId_checkParts() {
        int nodeId = 123;
        SnowflakeIdGenerator g = SnowflakeIdGenerator.builder().nodeId(nodeId).build();
        ID id = g.nextId();
        assertInstanceOf(SnowflakeIdGenerator.SnowflakeID.class, id);
        SnowflakeIdGenerator.SnowflakeID sid = (SnowflakeIdGenerator.SnowflakeID) id;
        assertTrue(sid.timestamp() <= Instant.now().toEpochMilli());
        assertEquals(nodeId, sid.nodeId());
        assertTrue(sid.sequence() >= 0 && sid.sequence() <= g.getMaxSequence());
        assertEquals(g.getNodeIdBits(), sid.nodeIdBits());
        assertEquals(g.getSequenceBits(), sid.sequenceBits());
        assertEquals(g.getCustomEpoch(), sid.customEpoch());
        assertTrue(g.getLastTimestamp() > 0);
        assertTrue(g.getSequence() >= 0);

        assertTrue(sid.canBeLong());
        assertTrue(sid.asLong() > 0);
        assertEquals("" + sid.asLong(), sid.asString());
    }

    @Test
    void nextId_unique() {
        var g = SnowflakeIdGenerator.builder().build();
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 100_000; i++) {
            long id = g.nextId().asLong();
            assertFalse(ids.contains(id));
            ids.add(id);
        }
        assertEquals(100_000, ids.size());
    }

    @Test
    void nextId_concurrentUnique() throws InterruptedException {
        var g = SnowflakeIdGenerator.builder().build();
        Set<Long> ids = new HashSet<>();
        Lock idsLock = new ReentrantLock();
        CountDownLatch latch = new CountDownLatch(100);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100; i++) {
                executor.execute(() -> {
                    for (int j = 0; j < 1_000; j++) {
                        long id = g.nextId().asLong();
                        idsLock.lock();
                        try {
                            assertFalse(ids.contains(id));
                            ids.add(id);
                        } finally {
                            idsLock.unlock();
                        }
                    }
                    latch.countDown();
                });
            }
            latch.await();
            assertEquals(100_000, ids.size());
        }
    }
}