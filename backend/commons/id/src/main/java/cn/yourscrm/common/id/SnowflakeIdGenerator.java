package cn.yourscrm.common.id;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;

public class SnowflakeIdGenerator implements IdGenerator {
    private final int epochBits;
    private final int nodeIdBits;
    private final int sequenceBits;
    private final long maxSequence;

    private final long nodeId;
    private final long customEpoch;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    private SnowflakeIdGenerator(
            int epochBits, int nodeIdBits, int sequenceBits, long nodeId, long customEpoch) {
        this.epochBits = epochBits;
        this.nodeIdBits = nodeIdBits;
        this.sequenceBits = sequenceBits;
        this.maxSequence = (1L << sequenceBits) - 1;
        this.nodeId = nodeId;
        this.customEpoch = customEpoch;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        // 最高位（符号位），始终保持为 0
        public static final int UNUSED_BITS = 1;
        // 时间戳 41 位。毫秒数。大约范围为 69 年。这应该超过 99.999% 的公司存活时间了。
        public static final int DEFAULT_EPOCH_BITS = 41;
        // 机器码 10 位。最多允许 1024 台机器不冲突。
        // 本项目设计的环境基本为单体部署（即使多副本部署也不会太多，2或3副本，极端情况也不会超过10）。
        // 所以可以减小，让出可空间可以调给时间戳或序列位。
        public static final int DEFAULT_NODE_ID_BITS = 10;
        // 12 位序列位。每毫秒最多单点可生成 4096 个 id。
        // 本项目设计的场景预计并发不会每秒过千，12位已经足够了。
        public static final int DEFAULT_SEQUENCE_BITS = 12;
        // 默认 EPOCH 起始：2024-08-01T00:00:00:00Z
        private static final long DEFAULT_CUSTOM_EPOCH = 1722470400000L;

        private int nodeIdBits = DEFAULT_NODE_ID_BITS;
        private int sequenceBits = DEFAULT_SEQUENCE_BITS;

        private Long nodeId = null;
        private long customEpoch = DEFAULT_CUSTOM_EPOCH;

        private Builder() {
        }

        public Builder nodeIdBits(int nodeIdBits) {
            this.nodeIdBits = nodeIdBits;
            return this;
        }

        public Builder sequenceBits(int sequenceBits) {
            this.sequenceBits = sequenceBits;
            return this;
        }

        public Builder nodeId(long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder customEpoch(long customEpoch) {
            this.customEpoch = customEpoch;
            return this;
        }

        public SnowflakeIdGenerator build() {
            if (nodeIdBits < 4) {
                throw new IllegalArgumentException("node id bits is too small");
            }
            if (nodeIdBits > DEFAULT_NODE_ID_BITS) {
                throw new IllegalArgumentException("node id bits is too large");
            }
            if (sequenceBits <= 0) {
                throw new IllegalArgumentException("sequence bits should be positive");
            }
            int epochBits = 64 - UNUSED_BITS - nodeIdBits - sequenceBits;
            if (epochBits < DEFAULT_EPOCH_BITS) {
                throw new IllegalArgumentException("epoch bits is too small");
            }
            long epochMax = (1L << epochBits) - 1;
            if (customEpoch < Instant.now().toEpochMilli() - epochMax) {
                throw new IllegalArgumentException("custom epoch is too old");
            }
            if (customEpoch > Instant.now().toEpochMilli()) {
                throw new IllegalArgumentException("custom epoch should not be in the future");
            }
            if (nodeId == null) {
                nodeId = createNodeId(nodeIdBits);
            }
            if (nodeId < 0 || nodeId >= (1L << nodeIdBits)) {
                throw new IllegalArgumentException(
                        "node id should between 0 and " + ((1L << nodeIdBits) - 1));
            }
            return new SnowflakeIdGenerator(
                    epochBits, nodeIdBits, sequenceBits, nodeId, customEpoch);
        }

        private long createNodeId(int nodeIdBits) {
            long maxNodeId = (1L << nodeIdBits) - 1;
            long nodeId;
            try {
                var sb = new StringBuilder();
                var networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    var networkInterface = networkInterfaces.nextElement();
                    var mac = networkInterface.getHardwareAddress();
                    if (mac == null) continue;
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
                long pid = ProcessHandle.current().pid();
                sb.append(pid);
                nodeId = sb.toString().hashCode();
            } catch (Throwable t) {
                nodeId = new SecureRandom().nextInt();
            }
            return nodeId & maxNodeId;
        }
    }

    @Override
    public synchronized @NotNull ID nextId() {
        long currentTimestamp = Instant.now().toEpochMilli() - customEpoch;
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                // 序列已耗尽 等到下一毫秒
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // 为新毫秒重置序列
            sequence = 0;
        }
        lastTimestamp = currentTimestamp;
        return new SnowflakeID(
                currentTimestamp + customEpoch, nodeId, sequence, nodeIdBits, sequenceBits, customEpoch);
    }

    @Override
    public @NotNull ID zero() {
        return new SnowflakeID(customEpoch, 0, 0, nodeIdBits, sequenceBits, customEpoch);
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = Instant.now().toEpochMilli() - customEpoch;
        }
        return currentTimestamp;
    }

    @Override
    public String toString() {
        return "SnowflakeIdGenerator{" +
                "epochBits=" + epochBits +
                ", nodeIdBits=" + nodeIdBits +
                ", sequenceBits=" + sequenceBits +
                ", nodeId=" + nodeId +
                ", customEpoch=" + customEpoch +
                ", lastTimestamp=" + lastTimestamp +
                ", sequence=" + sequence +
                '}';
    }

    public record SnowflakeID(
            long timestamp, long nodeId, long sequence, int nodeIdBits, int sequenceBits,
            long customEpoch
    ) implements ID {
        @Override
        public boolean canBeLong() {
            return true;
        }

        @Override
        public long asLong() {
            long currentTimestamp = timestamp - customEpoch;
            return currentTimestamp << (nodeIdBits + sequenceBits)
                    | (nodeId << sequenceBits)
                    | sequence;
        }

        @Override
        public String asString() {
            return "" + asLong();
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

    @TestOnly
    int getEpochBits() {
        return epochBits;
    }

    @TestOnly
    int getNodeIdBits() {
        return nodeIdBits;
    }

    @TestOnly
    int getSequenceBits() {
        return sequenceBits;
    }

    @TestOnly
    long getMaxSequence() {
        return maxSequence;
    }

    @TestOnly
    long getNodeId() {
        return nodeId;
    }

    @TestOnly
    long getCustomEpoch() {
        return customEpoch;
    }

    @TestOnly
    long getLastTimestamp() {
        return lastTimestamp;
    }

    @TestOnly
    long getSequence() {
        return sequence;
    }
}
