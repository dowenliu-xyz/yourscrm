package cn.yourscrm.common.id;

public interface ID {
    default boolean canBeLong() {
        return false;
    }

    default long asLong() {
        throw new UnsupportedOperationException("This ID does not support conversion to long");
    }

    String asString();

    boolean equals(Object o);

    int hashCode();
}
