package cn.yourscrm.common.id;

import org.jetbrains.annotations.NotNull;

public interface IdGenerator {
    @NotNull
    ID nextId();

    @NotNull
    ID zero();
}
