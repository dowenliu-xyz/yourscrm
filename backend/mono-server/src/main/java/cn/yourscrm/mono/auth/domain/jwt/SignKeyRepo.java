package cn.yourscrm.mono.auth.domain.jwt;

import cn.yourscrm.common.id.ID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface SignKeyRepo {
    @NotNull
    Collection<SignKey> loadAll();

    void newKey(@NotNull SignKey signKey);

    void dropKeys(@NotNull Collection<ID> ids);
}
