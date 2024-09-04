package cn.yourscrm.mono.ddd.domain;

import cn.yourscrm.common.id.ID;
import cn.yourscrm.mono.time.Nower;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@Getter
public abstract class AggregateRoot implements Identified {
    @NotNull
    private final ID id;
    @NotNull
    private final ID createdBy;
    @NotNull
    private final Instant createdAt;
    @NotNull
    private ID lastModifiedBy;
    @NotNull
    private Instant lastModifiedAt;
    private boolean deleted;
    private final long version;

    // 全功能构造函数，通常用于从数据源恢复数据时使用。
    protected AggregateRoot(@NotNull ID id,
                            @NotNull ID createdBy,
                            @NotNull Instant createdAt,
                            @NotNull ID lastModifiedBy,
                            @NotNull Instant lastModifiedAt,
                            boolean deleted,
                            long version) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedAt = lastModifiedAt;
        this.deleted = deleted;
        this.version = version;
    }

    // 创建新的聚合根时使用
    protected AggregateRoot(@NotNull ID id, @NotNull ID createdBy) {
        this(id, createdBy, Nower.now(), createdBy, Nower.now(), false, 0);
    }

    @Override
    public @NotNull ID getId() {
        return id;
    }

    protected void updateBy(@NotNull ID lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedAt = Nower.now();
    }

    public void deletedBy(@NotNull ID lastModifiedBy) {
        updateBy(lastModifiedBy);
        this.deleted = true;
    }
}
