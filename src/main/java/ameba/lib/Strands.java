package ameba.lib;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.RuntimeSuspendExecution;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableCallable;
import co.paralleluniverse.strands.SuspendableRunnable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author icode
 */
public class Strands {
    /**
     * The minimum priority that a strand can have.
     */
    public final static int MIN_PRIORITY = 1;
    /**
     * The default priority that is assigned to a strand.
     */
    public final static int NORM_PRIORITY = 5;
    /**
     * The maximum priority that a strand can have.
     */
    public final static int MAX_PRIORITY = 10;

    private Strands() {
    }

    public static Strand of(Object owner) {
        return Strand.of(owner);
    }

    /**
     * Returns a strand representing the given thread.
     */
    public static Strand of(Thread thread) {
        return Strand.of(thread);
    }

    /**
     * Returns a strand representing the given fiber.
     * The current implementation simply returns the fiber itself as {@code Fiber} extends {@code Fiber}.
     */
    public static Strand of(Fiber fiber) {
        return Strand.of(fiber);
    }

    /**
     * Returns the current strand.
     * This method will return a strand representing the fiber calling this method, or the current thread if this method is not
     * called within a fiber.
     *
     * @return A strand representing the current fiber or thread
     */
    public static Strand currentStrand() {
        return Strand.currentStrand();
    }

    /**
     * Tests whether this function is called within a fiber. This method <i>might</i> be faster than {@code Fiber.currentFiber() != null}.
     *
     * @return {@code true} iff the code that called this method is executing in a fiber.
     */
    public static boolean isCurrentFiber() {
        return Strand.isCurrentFiber();
    }

    /**
     * Tests whether the current strand has been interrupted. The
     * <i>interrupted status</i> of the strand is cleared by this method. In
     * other words, if this method were to be called twice in succession, the
     * second call would return {@code false} (unless the current strand were
     * interrupted again, after the first call had cleared its interrupted
     * status and before the second call had examined it).
     *
     * @return {@code true} if the current thread has been interrupted; {@code false} otherwise.
     */
    public static boolean interrupted() {
        return Strand.interrupted();
    }

    /**
     * Awaits the termination of a given strand.
     * This method blocks until this strand terminates.
     *
     * @param strand the strand to join. May be an object of type {@code Strand}, {@code Fiber} or {@code Thread}.
     * @throws ExecutionException   if this strand has terminated as a result of an uncaught exception
     *                              (which will be the {@link Throwable#getCause() cause} of the thrown {@code ExecutionException}.
     * @throws InterruptedException
     */
    public static void join(Object strand) throws ExecutionException, InterruptedException {
        Strand.join(strand);
    }

    /**
     * Awaits the termination of a given strand, at most for the timeout duration specified.
     * This method blocks until this strand terminates or the timeout elapses.
     *
     * @param strand  the strand to join. May be an object of type {@code Strand}, {@code Fiber} or {@code Thread}.
     * @param timeout the maximum duration to wait for the strand to terminate in the time unit specified by {@code unit}.
     * @param unit    the time unit of {@code timeout}.
     * @throws TimeoutException     if this strand did not terminate by the time the timeout has elapsed.
     * @throws ExecutionException   if this strand has terminated as a result of an uncaught exception
     *                              (which will be the {@link Throwable#getCause() cause} of the thrown {@code ExecutionException}.
     * @throws InterruptedException
     */
    public static void join(Object strand, long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        Strand.join(strand, timeout, unit);
    }

    /**
     * A hint to the scheduler that the current strand is willing to yield
     * its current use of a processor. The scheduler is free to ignore this
     * hint.
     * <p>
     * <p>
     * Yield is a heuristic attempt to improve relative progression
     * between strands that would otherwise over-utilise a CPU. Its use
     * should be combined with detailed profiling and benchmarking to
     * ensure that it actually has the desired effect.
     */
    public static void yield() {
        try {
            Strand.yield();
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Causes the currently executing strand to sleep (temporarily cease
     * execution) for the specified number of milliseconds, subject to
     * the precision and accuracy of system timers and schedulers.
     *
     * @param millis the length of time to sleep in milliseconds
     * @throws IllegalArgumentException if the value of {@code millis} is negative
     * @throws InterruptedException     if any strand has interrupted the current strand. The
     *                                  <i>interrupted status</i> of the current strand is
     *                                  cleared when this exception is thrown.
     */
    public static void sleep(long millis) throws InterruptedException {
        try {
            Strand.sleep(millis);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Causes the currently executing strand to sleep (temporarily cease
     * execution) for the specified number of milliseconds plus the specified
     * number of nanoseconds, subject to the precision and accuracy of system
     * timers and schedulers.
     *
     * @param millis the length of time to sleep in milliseconds
     * @param nanos  {@code 0-999999} additional nanoseconds to sleep
     * @throws IllegalArgumentException if the value of {@code millis} is negative,
     *                                  or the value of {@code nanos} is not in the range {@code 0-999999}
     * @throws InterruptedException     if any strand has interrupted the current strand. The
     *                                  <i>interrupted status</i> of the current strand is
     *                                  cleared when this exception is thrown.
     */
    public static void sleep(long millis, int nanos) throws InterruptedException {
        try {
            Strand.sleep(millis, nanos);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Causes the currently executing strand to sleep (temporarily cease
     * execution) for the specified duration, subject to
     * the precision and accuracy of system timers and schedulers.
     *
     * @param duration the length of time to sleep in the time unit specified by {@code unit}.
     * @param unit     the time unit of {@code duration}.
     * @throws InterruptedException if any strand has interrupted the current strand. The
     *                              <i>interrupted status</i> of the current strand is
     *                              cleared when this exception is thrown.
     */
    public static void sleep(long duration, TimeUnit unit) throws InterruptedException {
        try {
            Strand.sleep(duration, unit);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Disables the current strand for scheduling purposes unless the
     * permit is available.
     * <p>
     * <p>
     * If the permit is available then it is consumed and the call returns
     * immediately; otherwise
     * the current strand becomes disabled for scheduling
     * purposes and lies dormant until one of three things happens:
     * <p>
     * <ul>
     * <li>Some other strand invokes {@link #unpark unpark} with the
     * current strand as the target; or
     * <p>
     * <li>Some other strand interrupts
     * the current strand; or
     * <p>
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     * <p>
     * <p>
     * This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the strand to park in the first place. Callers may also determine,
     * for example, the interrupt status of the strand upon return.
     */
    public static void park() {
        try {
            Strand.park();
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Disables the current strand for scheduling purposes unless the
     * permit is available.
     * <p>
     * <p>
     * If the permit is available then it is consumed and the call returns
     * immediately; otherwise
     * the current strand becomes disabled for scheduling
     * purposes and lies dormant until one of three things happens:
     * <p>
     * <ul>
     * <li>Some other strand invokes {@link #unpark unpark} with the
     * current strand as the target; or
     * <p>
     * <li>Some other strand interrupts
     * the current strand; or
     * <p>
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     * <p>
     * <p>
     * This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the strand to park in the first place. Callers may also determine,
     * for example, the interrupt status of the strand upon return.
     *
     * @param blocker the synchronization object responsible for this strand parking
     */
    public static void park(Object blocker) {
        try {
            Strand.park(blocker);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    public static void parkAndUnpark(Strand other, Object blocker) {
        try {
            Strand.parkAndUnpark(other, blocker);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    public static void parkAndUnpark(Strand other) {
        try {
            Strand.parkAndUnpark(other);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    public static void yieldAndUnpark(Strand other, Object blocker) {
        try {
            Strand.yieldAndUnpark(other, blocker);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    public static void yieldAndUnpark(Strand other) {
        try {
            Strand.yieldAndUnpark(other);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Disables the current strand for thread scheduling purposes, for up to
     * the specified waiting time, unless the permit is available.
     * <p>
     * <p>
     * If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current strand becomes disabled
     * for scheduling purposes and lies dormant until one of four
     * things happens:
     * <p>
     * <ul>
     * <li>Some other strand invokes {@link #unpark unpark} with the
     * current strand as the target; or
     * <p>
     * <li>Some other strand interrupts
     * the current strand; or
     * <p>
     * <li>The specified waiting time elapses; or
     * <p>
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     * <p>
     * <p>
     * This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the strand to park in the first place. Callers may also determine,
     * for example, the interrupt status of the strand, or the elapsed time
     * upon return.
     *
     * @param nanos the maximum number of nanoseconds to wait
     */
    public static void parkNanos(long nanos) {
        try {
            Strand.parkNanos(nanos);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Disables the current strand for thread scheduling purposes, for up to
     * the specified waiting time, unless the permit is available.
     * <p>
     * <p>
     * If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current strand becomes disabled
     * for scheduling purposes and lies dormant until one of four
     * things happens:
     * <p>
     * <ul>
     * <li>Some other strand invokes {@link #unpark unpark} with the
     * current strand as the target; or
     * <p>
     * <li>Some other strand interrupts
     * the current strand; or
     * <p>
     * <li>The specified waiting time elapses; or
     * <p>
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     * <p>
     * <p>
     * This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the strand to park in the first place. Callers may also determine,
     * for example, the interrupt status of the strand, or the elapsed time
     * upon return.
     *
     * @param blocker the synchronization object responsible for this strand parking
     * @param nanos   the maximum number of nanoseconds to wait
     */
    public static void parkNanos(Object blocker, long nanos) {
        try {
            Strand.parkNanos(blocker, nanos);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Disables the current strand for scheduling purposes, until
     * the specified deadline, unless the permit is available.
     * <p>
     * <p>
     * If the permit is available then it is consumed and the call
     * returns immediately; otherwise the current strand becomes disabled
     * for scheduling purposes and lies dormant until one of four
     * things happens:
     * <p>
     * <ul>
     * <li>Some other strand invokes {@link #unpark unpark} with the
     * current strand as the target; or
     * <p>
     * <li>Some other strand interrupts the
     * current strand; or
     * <p>
     * <li>The specified deadline passes; or
     * <p>
     * <li>The call spuriously (that is, for no reason) returns.
     * </ul>
     * <p>
     * <p>
     * This method does <em>not</em> report which of these caused the
     * method to return. Callers should re-check the conditions which caused
     * the strand to park in the first place. Callers may also determine,
     * for example, the interrupt status of the strand, or the current time
     * upon return.
     *
     * @param blocker  the synchronization object responsible for this strand parking
     * @param deadline the absolute time, in milliseconds from the Epoch, to wait until
     */
    public static void parkUntil(Object blocker, long deadline) {
        try {
            Strand.parkUntil(blocker, deadline);
        } catch (SuspendExecution e) {
            throw RuntimeSuspendExecution.of(e);
        }
    }

    /**
     * Makes available the permit for the given strand, if it
     * was not already available. If the strand was blocked on
     * {@code park} then it will unblock. Otherwise, its next call
     * to {@code park} is guaranteed not to block. This operation
     * is not guaranteed to have any effect at all if the given
     * strand has not been started.
     *
     * @param strand the strand to unpark, or {@code null}, in which case this operation has no effect
     */
    public static void unpark(Strand strand) {
        Strand.unpark(strand);
    }

    /**
     * Makes available the permit for the given strand, if it
     * was not already available. If the strand was blocked on
     * {@code park} then it will unblock. Otherwise, its next call
     * to {@code park} is guaranteed not to block. This operation
     * is not guaranteed to have any effect at all if the given
     * strand has not been started.
     *
     * @param strand    the strand to unpark, or {@code null}, in which case this operation has no effect
     * @param unblocker the synchronization object responsible for the strand unparking
     */
    public static void unpark(Strand strand, Object unblocker) {
        Strand.unpark(strand, unblocker);
    }

    /**
     * Makes available the permit for the given strand, if it
     * was not already available. If the strand was blocked on
     * {@code park} then it will unblock. Otherwise, its next call
     * to {@code park} is guaranteed not to block. This operation
     * is not guaranteed to have any effect at all if the given
     * strand has not been started.
     *
     * @param strand the strand to unpark, or {@code null}, in which case this operation has no effect
     */
    public static void unpark(Thread strand) {
        Strand.unpark(strand);
    }

    /**
     * Prints a stack trace of the current strand to the standard error stream.
     * This method is used only for debugging.
     */
    @SuppressWarnings({"CallToThreadDumpStack", "CallToPrintStackTrace"})
    public static void dumpStack() {
        Strand.dumpStack();
    }

    /**
     * Tests whether two strands represent the same fiber or thread.
     *
     * @param strand1 May be an object of type {@code Strand}, {@code Fiber} or {@code Thread}.
     * @param strand2 May be an object of type {@code Strand}, {@code Fiber} or {@code Thread}.
     * @return {@code true} if the two strands represent the same fiber or the same thread; {@code false} otherwise.
     */
    public static boolean equals(Object strand1, Object strand2) {
        return Strand.equals(strand1, strand2);
    }

    public static Strand clone(Strand strand, final SuspendableCallable<?> target) {
        return Strand.clone(strand, target);
    }

    public static Strand clone(Strand strand, final SuspendableRunnable target) {
        return Strand.clone(strand, target);
    }

    /**
     * A utility method that converts a {@link SuspendableRunnable} to a {@link Runnable} so that it could run
     * as the target of a thread.
     */
    public static Runnable toRunnable(final SuspendableRunnable runnable) {
        return Strand.toRunnable(runnable);
    }

    /**
     * A utility method that converts a {@link SuspendableCallable} to a {@link Runnable} so that it could run
     * as the target of a thread. The return value of the callable is ignored.
     */
    public static Runnable toRunnable(final SuspendableCallable<?> callable) {
        return Strand.toRunnable(callable);
    }

    /**
     * Returns the {@link SuspendableCallable} or {@link SuspendableRunnable}, wrapped by the given {@code Runnable}
     * by {@code toRunnable}.
     */
    public static Object unwrapSuspendable(Runnable r) {
        return Strand.unwrapSuspendable(r);
    }

    /**
     * This utility method turns a stack-trace into a human readable, multi-line string.
     *
     * @param trace a stack trace (such as returned from {@link Strand#getStackTrace()}.
     * @return a nice (multi-line) string representation of the stack trace.
     */
    public static String toString(StackTraceElement[] trace) {
        return Strand.toString(trace);
    }

    /**
     * This utility method prints a stack-trace into a {@link java.io.PrintStream}
     *
     * @param trace a stack trace (such as returned from {@link Strand#getStackTrace()}.
     * @param out   the {@link java.io.PrintStream} into which the stack trace will be printed.
     */
    public static void printStackTrace(StackTraceElement[] trace, java.io.PrintStream out) {
        Strand.printStackTrace(trace, out);
    }

    /**
     * This utility method prints a stack-trace into a {@link java.io.PrintWriter}
     *
     * @param trace a stack trace (such as returned from {@link Strand#getStackTrace()}.
     * @param out   the {@link java.io.PrintWriter} into which the stack trace will be printed.
     */
    public static void printStackTrace(StackTraceElement[] trace, java.io.PrintWriter out) {
        Strand.printStackTrace(trace, out);
    }
}
