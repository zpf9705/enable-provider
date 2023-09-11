package top.osjf.assembly.utils.rxjava;

import copy.cn.hutool.v_5819.logger.StaticLog;
import io.reactivex.rxjava3.disposables.Disposable;
import org.springframework.lang.NonNull;
import top.osjf.assembly.utils.SystemUtils;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The subscription relationship generated by the distribution of producer data will occupy
 * the system memory. If it is not released in time, Memory leak will result.
 * <p>
 * Then this tool collects the subscription relationship {@link Disposable} and releases it regularly
 *
 * @author zpf
 * @since 3.1.4
 */
public abstract class DisposableUtils {

    protected static final List<Disposable> dis = new CopyOnWriteArrayList<>();

    protected static final ThreadFactory default_thread_factory = new ThreadFactory() {

        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        private final AtomicInteger threadNumber = new AtomicInteger(0);

        public Thread newThread(@NonNull Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setName("disposable-clear-thread-" + threadNumber.getAndIncrement());
            return thread;
        }
    };

    public static final String core_size_sign = "disposable.clear.core.thead.size";

    public static final String start_init_delay = "disposable.clear.start.init.delay";

    public static final String start_period = "disposable.clear.start.period";

    public static final String timeunit = "disposable.clear.start.timeunit";

    public static final ScheduledExecutorService service;

    static {
        //ScheduledExecutor init
        service = Executors.newScheduledThreadPool(
                SystemUtils.getPropertyWithConvert(core_size_sign, Integer::parseInt, 1),
                default_thread_factory);

        start();
    }

    /**
     * Pre add subscription relationships to static favorites, to be processed when confirmed
     *
     * @param disposable {@link Disposable}
     */
    public static void addDisposable(Disposable disposable) {
        if (disposable != null) {
            dis.add(disposable);
        }
    }

    /**
     * Clear the subscription relationship, free the occupied memory, and avoid Memory leak
     */
    protected static void clearDisposable() {
        if (dis.isEmpty()) {
            return;
        }
        StaticLog.info("start clean up disposable");
        //To prevent scheduled tasks from starting and clearing subscriptions that have not been completed,
        // add the currently completed subscriptions to a new list for execution first
        List<Disposable> solveDisposables = new CopyOnWriteArrayList<>(dis);
        solveDisposables.forEach(Disposable::dispose);
        //Delete completed
        dis.removeAll(solveDisposables);
    }

    /**
     * Startup clear Disposable Executor
     */
    private static void start() {
        //startup ScheduledExecutor
        service.scheduleAtFixedRate(DisposableUtils::clearDisposable,
                SystemUtils.getPropertyWithConvert(start_init_delay, Integer::parseInt, 2),
                SystemUtils.getPropertyWithConvert(start_period, Integer::parseInt, 2),
                SystemUtils.getPropertyWithConvert(timeunit, TimeUnit::valueOf, TimeUnit.MINUTES));
    }

    /**
     * loading of this class
     */
    public static void preload() {
        //no op
    }
}
