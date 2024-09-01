package cn.yourscrm.commons.concurrent.singleFlight;

import org.jetbrains.annotations.VisibleForTesting;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * SingleFlight 单飞并发模式。
 * <p/>
 * 示例：
 * <pre>
 * public Result expensiveOperation(final Parameters parameters) throws Exception {
 *     return singleFlight.execute(parameters, new Callable&lt;Result&gt;() {
 *         &#64;Override
 *         public Result call() {
 *             return expensiveOperationImpl(parameters);
 *         }
 *     });
 * }
 *
 * private Result expensiveOperationImpl(Parameters parameters) {
 *     // the real implementation
 * }
 * </pre>
 * <p>Create at 0204/09/01</p>
 *
 * @author dowenliu-xyz
 * @since 0.0.1
 */
public class SingleFlight {
    @SuppressWarnings({"ClassEscapesDefinedScope", "rawtypes"})
    @VisibleForTesting
    protected final ConcurrentMap<Object, Call> calls = new ConcurrentHashMap<>();

    /**
     * 在没有其他相同 {@code key} 调用时执行 {@link Callable} 调用 {@code callable} 。
     * 当存在相同 {@code key} 并发调用时，只有一个 {@link Callable} 被执行，所有当前并发请求将共享其执行结果。
     * <p/>
     * 调用结果并没有缓存，只是当前并发的调用共享当前调用的结果。
     *
     * @param key      方法调用唯一标识。 {@code key} 必须重写
     *                 {@link Object#hashCode()} 和 {@link Object#equals(Object)}
     *                 方法以保证唯一标识判断正确。
     * @param callable 用于获取计算结果的 {@link Callable} 。
     * @param <V>      {@link Callable} 调用结果类型。
     * @return 执行 {@link Callable} 的结果。
     * @throws Throwable {@link Callable} 抛出的 {@link Exception}；
     *                   或者等待共享结果的 {@link Thread} 因响应中断请求时抛出的
     *                   {@link InterruptedException} 。
     */
    public <V> V execute(Object key, Callable<V> callable) throws Throwable {
        // noinspection unchecked
        Call<V> call = calls.get(key);
        if (call != null) return call.await();
        call = new Call<>();
        // noinspection unchecked
        Call<V> other = calls.putIfAbsent(key, call);
        if (other != null) return other.await();
        try {
            return call.exec(callable);
        } finally {
            calls.remove(key);
        }
    }

    private static class Call<V> {
        private final Object lock = new Object();
        private boolean finished;
        private V result;
        private Throwable thr;

        void finished(V result, Throwable thr) {
            synchronized (lock) {
                this.finished = true;
                this.result = result;
                this.thr = thr;
                lock.notifyAll();
            }
        }

        V await() throws Throwable {
            synchronized (lock) {
                while (!finished) {
                    lock.wait();
                }
                if (thr != null) throw thr;
                return result;
            }
        }

        V exec(Callable<V> callable) throws Exception {
            V result = null;
            Throwable thr = null;
            try {
                result = callable.call();
                return result;
            } catch (Throwable t) {
                thr = t;
                throw t;
            } finally {
                finished(result, thr);
            }
        }
    }
}
