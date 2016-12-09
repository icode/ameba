package ameba.lib;

import co.paralleluniverse.fibers.DefaultFiberScheduler;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.FiberUtil;
import co.paralleluniverse.strands.SuspendableCallable;
import co.paralleluniverse.strands.SuspendableRunnable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author icode
 */
public class Fibers {
    private Fibers() {
    }

    /**
     * Turns a fiber into a {@link Future}.
     *
     * @param <V>
     * @param fiber the fiber
     * @return a {@link Future} representing the fiber.
     */
    public static <V> Future<V> toFuture(Fiber<V> fiber) {
        return FiberUtil.toFuture(fiber);
    }

    /**
     * Runs an action in a new fiber, awaits the fiber's termination, and returns its result.
     * The new fiber is scheduled by the {@link DefaultFiberScheduler default scheduler}.
     *
     * @param <V>
     * @param target the operation
     * @return the operations return value
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static <V> V runInFiber(SuspendableCallable<V> target) throws ExecutionException, InterruptedException {
        return FiberUtil.runInFiber(target);
    }

    /**
     * Runs an action in a new fiber, awaits the fiber's termination, and returns its result.
     *
     * @param <V>
     * @param scheduler the {@link FiberScheduler} to use when scheduling the fiber.
     * @param target    the operation
     * @return the operations return value
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static <V> V runInFiber(FiberScheduler scheduler, SuspendableCallable<V> target) throws ExecutionException, InterruptedException {
        return FiberUtil.runInFiber(scheduler, target);
    }

    /**
     * Runs an action in a new fiber and awaits the fiber's termination.
     * The new fiber is scheduled by the {@link DefaultFiberScheduler default scheduler}.
     * .
     *
     * @param target the operation
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void runInFiber(SuspendableRunnable target) throws ExecutionException, InterruptedException {
        FiberUtil.runInFiber(target);
    }

    /**
     * Runs an action in a new fiber and awaits the fiber's termination.
     *
     * @param scheduler the {@link FiberScheduler} to use when scheduling the fiber.
     * @param target    the operation
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void runInFiber(FiberScheduler scheduler, SuspendableRunnable target) throws ExecutionException, InterruptedException {
        FiberUtil.runInFiber(scheduler, target);
    }

    /**
     * Runs an action in a new fiber, awaits the fiber's termination, and returns its result.
     * Unlike {@link #runInFiber(SuspendableCallable) runInFiber} this method does not throw {@link ExecutionException}, but wraps
     * any checked exception thrown by the operation in a {@link RuntimeException}.
     * The new fiber is scheduled by the {@link DefaultFiberScheduler default scheduler}.
     *
     * @param <V>
     * @param target the operation
     * @return the operations return value
     * @throws InterruptedException
     */
    public static <V> V runInFiberRuntime(SuspendableCallable<V> target) throws InterruptedException {
        return FiberUtil.runInFiberRuntime(target);
    }

    /**
     * Runs an action in a new fiber, awaits the fiber's termination, and returns its result.
     * Unlike {@link #runInFiber(FiberScheduler, SuspendableCallable) runInFiber} this method does not throw {@link ExecutionException}, but wraps
     * any checked exception thrown by the operation in a {@link RuntimeException}.
     *
     * @param <V>
     * @param scheduler the {@link FiberScheduler} to use when scheduling the fiber.
     * @param target    the operation
     * @return the operations return value
     * @throws InterruptedException
     */
    public static <V> V runInFiberRuntime(FiberScheduler scheduler, SuspendableCallable<V> target) throws InterruptedException {
        return FiberUtil.runInFiberRuntime(scheduler, target);
    }

    /**
     * Runs an action in a new fiber and awaits the fiber's termination.
     * Unlike {@link #runInFiber(SuspendableRunnable)  runInFiber} this method does not throw {@link ExecutionException}, but wraps
     * any checked exception thrown by the operation in a {@link RuntimeException}.
     * The new fiber is scheduled by the {@link DefaultFiberScheduler default scheduler}.
     *
     * @param target the operation
     * @throws InterruptedException
     */
    public static void runInFiberRuntime(SuspendableRunnable target) throws InterruptedException {
        FiberUtil.runInFiberRuntime(target);
    }

    /**
     * Runs an action in a new fiber and awaits the fiber's termination.
     * Unlike {@link #runInFiber(FiberScheduler, SuspendableRunnable)   runInFiber} this method does not throw {@link ExecutionException}, but wraps
     * any checked exception thrown by the operation in a {@link RuntimeException}.
     *
     * @param scheduler the {@link FiberScheduler} to use when scheduling the fiber.
     * @param target    the operation
     * @throws InterruptedException
     */
    public static void runInFiberRuntime(FiberScheduler scheduler, SuspendableRunnable target) throws InterruptedException {
        FiberUtil.runInFiberRuntime(scheduler, target);
    }

    /**
     * Runs an action in a new fiber, awaits the fiber's termination, and returns its result.
     * Unlike {@link #runInFiber(SuspendableCallable) runInFiber} this method does not throw {@link ExecutionException}, but wraps
     * any checked exception thrown by the operation in a {@link RuntimeException}.
     * The new fiber is scheduled by the {@link DefaultFiberScheduler default scheduler}.
     *
     * @param <V>
     * @param target the operation
     * @return the operations return value
     * @throws InterruptedException
     */
    public static <V, X extends Exception> V runInFiberChecked(SuspendableCallable<V> target, Class<X> exceptionType) throws X, InterruptedException {
        return FiberUtil.runInFiberChecked(target, exceptionType);
    }

    /**
     * Runs an action in a new fiber, awaits the fiber's termination, and returns its result.
     * Unlike {@link #runInFiber(FiberScheduler, SuspendableCallable) runInFiber} this method does not throw {@link ExecutionException}, but wraps
     * any checked exception thrown by the operation in a {@link RuntimeException}, unless it is of the given {@code exception type}, in
     * which case the checked exception is thrown as-is.
     *
     * @param <V>
     * @param scheduler     the {@link FiberScheduler} to use when scheduling the fiber.
     * @param target        the operation
     * @param exceptionType a checked exception type that will not be wrapped if thrown by the operation, but thrown as-is.
     * @return the operations return value
     * @throws InterruptedException
     */
    public static <V, X extends Exception> V runInFiberChecked(FiberScheduler scheduler, SuspendableCallable<V> target, Class<X> exceptionType) throws X, InterruptedException {
        return FiberUtil.runInFiberChecked(scheduler, target, exceptionType);
    }

    /**
     * Runs an action in a new fiber and awaits the fiber's termination.
     * Unlike {@link #runInFiber(SuspendableRunnable)  runInFiber} this method does not throw {@link ExecutionException}, but wraps
     * any checked exception thrown by the operation in a {@link RuntimeException}, unless it is of the given {@code exception type}, in
     * which case the checked exception is thrown as-is.
     * The new fiber is scheduled by the {@link DefaultFiberScheduler default scheduler}.
     *
     * @param target        the operation
     * @param exceptionType a checked exception type that will not be wrapped if thrown by the operation, but thrown as-is.
     * @throws InterruptedException
     */
    public static <X extends Exception> void runInFiberChecked(SuspendableRunnable target, Class<X> exceptionType) throws X, InterruptedException {
        FiberUtil.runInFiberChecked(target, exceptionType);
    }

    /**
     * Runs an action in a new fiber and awaits the fiber's termination.
     * Unlike {@link #runInFiber(SuspendableRunnable)  runInFiber} this method does not throw {@link ExecutionException}, but wraps
     * any checked exception thrown by the operation in a {@link RuntimeException}, unless it is of the given {@code exception type}, in
     * which case the checked exception is thrown as-is.
     *
     * @param scheduler     the {@link FiberScheduler} to use when scheduling the fiber.
     * @param target        the operation
     * @param exceptionType a checked exception type that will not be wrapped if thrown by the operation, but thrown as-is.
     * @throws InterruptedException
     */
    public static <X extends Exception> void runInFiberChecked(FiberScheduler scheduler, SuspendableRunnable target, Class<X> exceptionType) throws X, InterruptedException {
        FiberUtil.runInFiberChecked(scheduler, target, exceptionType);
    }

    /**
     * Blocks on the input fibers and creates a new list from the results. The result list is the same order as the
     * input list.
     *
     * @param fibers to combine
     */
    public static <V> List<V> get(List<Fiber<V>> fibers) throws InterruptedException {
        return FiberUtil.get(fibers);
    }

    /**
     * Blocks on the input fibers and creates a new list from the results. The result list is the same order as the
     * input list.
     *
     * @param fibers to combine
     */
    public static <V> List<V> get(Fiber<V>... fibers) throws InterruptedException {
        return FiberUtil.get(fibers);
    }

    /**
     * Blocks on the input fibers and creates a new list from the results. The result list is the same order as the
     * input list.
     *
     * @param timeout to wait for all requests to complete
     * @param unit    the time is in
     * @param fibers  to combine
     */
    public static <V> List<V> get(long timeout, TimeUnit unit, List<Fiber<V>> fibers) throws InterruptedException, TimeoutException {
        return FiberUtil.get(timeout, unit, fibers);
    }

    /**
     * Blocks on the input fibers and creates a new list from the results. The result list is the same order as the
     * input list.
     *
     * @param time   to wait for all requests to complete
     * @param unit   the time is in
     * @param fibers to combine
     */
    public static <V> List<V> get(long time, TimeUnit unit, Fiber<V>... fibers) throws InterruptedException, TimeoutException {
        return FiberUtil.get(time, unit, fibers);
    }

    /**
     * Creates a new Fiber from the given {@link SuspendableCallable}.
     * The new fiber uses the default initial stack size.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param target    the SuspendableCallable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, FiberScheduler scheduler, SuspendableCallable<V> target) {
        return new Fiber<>(name, scheduler, target);
    }

    /**
     * Creates a new Fiber from the given {@link SuspendableCallable}.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(FiberScheduler scheduler, SuspendableCallable<V> target) {
        return new Fiber<>(scheduler, target);
    }

    /**
     * Creates a new Fiber from the given {@link SuspendableRunnable}.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param stackSize the initial size of the data stack.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, FiberScheduler scheduler, int stackSize, SuspendableRunnable target) {
        return new Fiber<>(name, scheduler, stackSize, target);
    }

    /**
     * Creates a new Fiber from the given {@link SuspendableRunnable}.
     * The new fiber uses the default initial stack size.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, FiberScheduler scheduler, SuspendableRunnable target) {
        return new Fiber<>(name, scheduler, target);
    }

    /**
     * Creates a new Fiber from the given SuspendableRunnable.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(FiberScheduler scheduler, SuspendableRunnable target) {
        return new Fiber<>(scheduler, target);
    }

    /**
     * Creates a new Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param stackSize the initial size of the data stack.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, FiberScheduler scheduler, int stackSize) {
        return new Fiber<>(name, scheduler, stackSize);
    }

    /**
     * Creates a new Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * The new fiber uses the default initial stack size.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, FiberScheduler scheduler) {
        return new Fiber<>(name, scheduler);
    }

    /**
     * Creates a new Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param scheduler The scheduler pool in which the fiber should run.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(FiberScheduler scheduler) {
        return new Fiber<>(scheduler);
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableCallable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber uses the default initial stack size.
     *
     * @param name   The name of the fiber (may be null)
     * @param target the SuspendableRunnable for the Fiber.
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, SuspendableCallable<V> target) {
        return new Fiber<>(name, target);
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableCallable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param target the SuspendableRunnable for the Fiber.
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(SuspendableCallable<V> target) {
        return new Fiber<>(target);
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableRunnable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     *
     * @param name      The name of the fiber (may be null)
     * @param stackSize the initial size of the data stack.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, int stackSize, SuspendableRunnable target) {
        return new Fiber<>(name, stackSize, target);
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableRunnable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber uses the default initial stack size.
     *
     * @param name   The name of the fiber (may be null)
     * @param target the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, SuspendableRunnable target) {
        return new Fiber<>(name, target);
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableRunnable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param target the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(SuspendableRunnable target) {
        return new Fiber<>(target);
    }

    /**
     * Creates a new child Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     *
     * @param name      The name of the fiber (may be null)
     * @param stackSize the initial size of the data stack.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name, int stackSize) {
        return new Fiber<>(name, stackSize);
    }

    /**
     * Creates a new child Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber uses the default initial stack size.
     *
     * @param name The name of the fiber (may be null)
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create(String name) {
        return new Fiber<>(name);
    }

    /**
     * Creates a new child Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> create() {
        return new Fiber<>();
    }

    public static <V> Fiber<V> create(Fiber fiber, SuspendableCallable<V> target) {
        return new Fiber<>(fiber, target);
    }

    public static <V> Fiber<V> create(Fiber fiber, SuspendableRunnable target) {
        return new Fiber<>(fiber, target);
    }

    public static <V> Fiber<V> create(Fiber fiber, FiberScheduler scheduler, SuspendableCallable<V> target) {
        return new Fiber<>(fiber, scheduler, target);
    }

    public static <V> Fiber<V> create(Fiber fiber, FiberScheduler scheduler, SuspendableRunnable target) {
        return new Fiber<>(fiber, scheduler, target);
    }

    /**
     * Creates a new Fiber from the given {@link SuspendableCallable}.
     * The new fiber uses the default initial stack size.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param target    the SuspendableCallable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, FiberScheduler scheduler, SuspendableCallable<V> target) {
        return create(name, scheduler, target).start();
    }

    /**
     * Creates a new Fiber from the given {@link SuspendableCallable}.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(FiberScheduler scheduler, SuspendableCallable<V> target) {
        return create(scheduler, target).start();
    }

    /**
     * Creates a new Fiber from the given {@link SuspendableRunnable}.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param stackSize the initial size of the data stack.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, FiberScheduler scheduler, int stackSize, SuspendableRunnable target) {
        return Fibers.<V>create(name, scheduler, stackSize, target).start();
    }

    /**
     * Creates a new Fiber from the given {@link SuspendableRunnable}.
     * The new fiber uses the default initial stack size.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, FiberScheduler scheduler, SuspendableRunnable target) {
        return Fibers.<V>create(name, scheduler, target).start();
    }

    /**
     * Creates a new Fiber from the given SuspendableRunnable.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(FiberScheduler scheduler, SuspendableRunnable target) {
        return Fibers.<V>create(scheduler, target).start();
    }

    /**
     * Creates a new Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @param stackSize the initial size of the data stack.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, FiberScheduler scheduler, int stackSize) {
        return Fibers.<V>create(name, scheduler, stackSize).start();
    }

    /**
     * Creates a new Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * The new fiber uses the default initial stack size.
     *
     * @param name      The name of the fiber (may be null)
     * @param scheduler The scheduler pool in which the fiber should run.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, FiberScheduler scheduler) {
        return Fibers.<V>create(name, scheduler).start();
    }

    /**
     * Creates a new Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param scheduler The scheduler pool in which the fiber should run.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(FiberScheduler scheduler) {
        return Fibers.<V>create(scheduler).start();
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableCallable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber uses the default initial stack size.
     *
     * @param name   The name of the fiber (may be null)
     * @param target the SuspendableRunnable for the Fiber.
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, SuspendableCallable<V> target) {
        return create(name, target).start();
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableCallable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param target the SuspendableRunnable for the Fiber.
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(SuspendableCallable<V> target) {
        return create(target).start();
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableRunnable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     *
     * @param name      The name of the fiber (may be null)
     * @param stackSize the initial size of the data stack.
     * @param target    the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, int stackSize, SuspendableRunnable target) {
        return Fibers.<V>create(name, stackSize, target).start();
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableRunnable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber uses the default initial stack size.
     *
     * @param name   The name of the fiber (may be null)
     * @param target the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, SuspendableRunnable target) {
        return Fibers.<V>create(name, target).start();
    }

    /**
     * Creates a new child Fiber from the given {@link SuspendableRunnable}.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @param target the SuspendableRunnable for the Fiber.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(SuspendableRunnable target) {
        return Fibers.<V>create(target).start();
    }

    /**
     * Creates a new child Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     *
     * @param name      The name of the fiber (may be null)
     * @param stackSize the initial size of the data stack.
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name, int stackSize) {
        return Fibers.<V>create(name, stackSize).start();
    }

    /**
     * Creates a new child Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber uses the default initial stack size.
     *
     * @param name The name of the fiber (may be null)
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start(String name) {
        return Fibers.<V>create(name).start();
    }

    /**
     * Creates a new child Fiber subclassing the Fiber class and overriding the {@link Fiber#run() run} method.
     * This constructor may only be called from within another fiber. This fiber will use the same fork/join pool as the creating fiber.
     * The new fiber has no name, and uses the default initial stack size.
     *
     * @throws NullPointerException     when proto is null
     * @throws IllegalArgumentException when stackSize is &lt;= 0
     */
    public static <V> Fiber<V> start() {
        return Fibers.<V>create().start();
    }

    public static <V> Fiber<V> start(Fiber fiber, SuspendableCallable<V> target) {
        return create(fiber, target).start();
    }

    public static <V> Fiber<V> start(Fiber fiber, SuspendableRunnable target) {
        return Fibers.<V>create(fiber, target).start();
    }

    public static <V> Fiber<V> start(Fiber fiber, FiberScheduler scheduler, SuspendableCallable<V> target) {
        return create(fiber, scheduler, target).start();
    }

    public static <V> Fiber<V> start(Fiber fiber, FiberScheduler scheduler, SuspendableRunnable target) {
        return Fibers.<V>create(fiber, scheduler, target).start();
    }
}
