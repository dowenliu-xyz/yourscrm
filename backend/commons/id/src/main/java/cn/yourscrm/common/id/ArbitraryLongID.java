package cn.yourscrm.common.id;

import java.util.Objects;

public class ArbitraryLongID implements ID {
    private final long id;

    public ArbitraryLongID(long id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return "" + id;
    }

    @Override
    public long asLong() {
        return id;
    }

    @Override
    public boolean canBeLong() {
        return true;
    }

    @Override
    public boolean isZero() {
        return id == 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ID oid)) return false;
        return Objects.equals(asString(), oid.asString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(asString());
    }
}
