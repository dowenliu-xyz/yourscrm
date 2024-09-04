package cn.yourscrm.common.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArbitraryLongIDTest {
    @Test
    void asString() {
        ArbitraryLongID arbitraryLongID = new ArbitraryLongID(1L);
        assertEquals("1", arbitraryLongID.asString());
    }

    @Test
    void asLong() {
        ArbitraryLongID arbitraryLongID = new ArbitraryLongID(1L);
        assertEquals(1L, arbitraryLongID.asLong());
    }

    @Test
    void isZero() {
        assertTrue(new ArbitraryLongID(0L).isZero());
        assertFalse(new ArbitraryLongID(1L).isZero());
    }

    @Test
    void equalsAndHash_genAndRestore() {
        var g = SnowflakeIdGenerator.builder().build();
        var generated = g.nextId();
        var restored = new ArbitraryLongID(generated.asLong());
        assertEquals(generated, restored);
        assertEquals(generated.hashCode(), restored.hashCode());
    }
}