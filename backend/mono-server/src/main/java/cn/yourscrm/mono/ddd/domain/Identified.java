package cn.yourscrm.mono.ddd.domain;

import cn.yourscrm.common.id.ID;
import org.jetbrains.annotations.NotNull;

public interface Identified {
    @NotNull
    ID getId();
}
