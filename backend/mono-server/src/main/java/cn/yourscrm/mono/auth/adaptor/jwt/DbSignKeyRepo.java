package cn.yourscrm.mono.auth.adaptor.jwt;

import cn.yourscrm.common.id.ID;
import cn.yourscrm.mono.auth.domain.jwt.SignKey;
import cn.yourscrm.mono.auth.domain.jwt.SignKeyRepo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;

@RequiredArgsConstructor
@Component
public class DbSignKeyRepo implements SignKeyRepo {
    private final SignKeyEntityRepo entityRepo;

    @Override
    public @NotNull Collection<SignKey> loadAll() {
        return entityRepo.findAll().stream().map(SignKeyEntity::toDomain).toList();
    }

    @Override
    public void newKey(@NotNull SignKey signKey) {
        entityRepo.save(SignKeyEntity.fromDomain(signKey));
    }

    @Override
    public void dropKeys(@NotNull Collection<ID> ids) {
        entityRepo.deleteAllById(ids.stream().map(ID::asLong).toList());
    }
}
