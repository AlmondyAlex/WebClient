package com.web.utils;

import android.annotation.TargetApi;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Basically a wrapper around {@link CompletableFuture} with added functionality for
 * using default executors and handlers. Use the {@link DeferredResult#supplyAsync}
 * to get a new {@link DeferredResult} object or a no-arg constructor. All of the work is
 * done off calling thread using either specified {@link Executor} or with an executor used in
 * {@link CompletableFuture#supplyAsync} method.
 * <br>
 * Any occurring checked exceptions are converted to {@link RuntimeException} exceptions.
 * @param <T> The result type returned by completing the DeferredResult
 */
@TargetApi(24)
public class DeferredResult<T>
{
    CompletableFuture<T> future;
    Executor executor;

    public DeferredResult()
    {
        this.future = new CompletableFuture<>();
    }

    DeferredResult(CompletableFuture<T> future, Executor executor)
    {
        this.future = future;
        this.executor = executor;
    }

    public static <U> DeferredResult<U> supplyAsync(ThrowingSupplier<U> supplier, Executor executor)
    {
        return new DeferredResult<>(CompletableFuture.supplyAsync(supplier, executor), executor);
    }

    public static <U> DeferredResult<U> supplyAsync(ThrowingSupplier<U> supplier)
    {
        return new DeferredResult<>(CompletableFuture.supplyAsync(supplier), ForkJoinPool.commonPool());
    }

    public <U> DeferredResult<U> thenApply(ThrowingFunction<? super T, ? extends U> func, Executor executor)
    {
        return new DeferredResult<>(future.thenApplyAsync(func, executor), executor);
    }

    /**
     * For more details see {@link CompletableFuture#thenApplyAsync(Function)}
     */
    public <U> DeferredResult<U> thenApply(ThrowingFunction<? super T, ? extends U> func)
    {
        return thenApply(func, executor);
    }

    /**
     * For more details see {@link CompletableFuture#thenAcceptAsync(Consumer, Executor)}
     */
    public DeferredResult<Void> thenAccept(ThrowingConsumer<? super T> consumer, Executor executor)
    {
        return new DeferredResult<>(future.thenAcceptAsync(consumer, executor), executor);
    }

    /**
     * For more details see {@link CompletableFuture#thenAcceptAsync(Consumer)}
     */
    public DeferredResult<Void> thenAccept(ThrowingConsumer<? super T> consumer)
    {
        return thenAccept(consumer, executor);
    }

    /**
     * Posts the consumer to the handling thread. For more details see
     * {@link CompletableFuture#thenAcceptAsync(Consumer, Executor)}
     */
    public DeferredResult<Void> thenPost(ThrowingConsumer<? super T> consumer, Handler handler, Executor executor)
    {
        return new DeferredResult<>(
                future.thenAcceptAsync(message -> handler.post(() -> consumer.accept(message)), executor),
                executor
        );
    }

    public DeferredResult<Void> thenPost(ThrowingConsumer<? super T> consumer, Handler handler)
    {
        return thenPost(consumer, handler, executor);
    }

    public DeferredResult<Void> thenPostToMain(ThrowingConsumer<? super T> consumer, Executor executor)
    {
        return thenPost(consumer, new Handler(Looper.getMainLooper()), executor);
    }

    public DeferredResult<Void> thenPostToMain(ThrowingConsumer<? super T> consumer)
    {
        return thenPost(consumer, new Handler(Looper.getMainLooper()), executor);
    }

    /**
     * For more details see {@link CompletableFuture#complete(Object)}
     */
    public boolean complete(T value)
    {
        return future.complete(value);
    }

    /**
     * For more details see {@link CompletableFuture#get()}
     */
    public T get() throws ExecutionException, InterruptedException
    {
        return future.get();
    }

    /**
     * For more details see {@link CompletableFuture#get(long, TimeUnit)}
     */
    public T get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException
    {
        return future.get(timeout, unit);
    }

    /**
     * For more details see {@link CompletableFuture#handle(BiFunction)}
     */
    public <U> DeferredResult<U> handle(BiFunction<? super T, Throwable, ? extends U> func)
    {
        return new DeferredResult<>(future.handleAsync(func, executor), executor);
    }

    /**
     * For more details see {@link CompletableFuture#cancel(boolean)}
     */
    public boolean cancel()
    {
        return future.cancel(true);
    }

    /**
     * For more details see {@link CompletableFuture#completeExceptionally(Throwable)}
     */
    public boolean completeExceptionally(Throwable ex)
    {
        return future.completeExceptionally(ex);
    }

    /**
     * For more details see {@link CompletableFuture#exceptionally(Function)}
     */
    public DeferredResult<T> exceptionally(Function<Throwable, ? extends T> func)
    {
        return new DeferredResult<>(future.exceptionally(func), executor);
    }

    public DeferredResult<T> exceptionally(Function<Throwable, ? extends T> func, Executor executor)
    {
        return new DeferredResult<>(future.exceptionally(func), executor);
    }

    /**
     * For more details see {@link CompletableFuture#isCancelled()}
     */
    public boolean isCancelled()
    {
        return future.isCancelled();
    }

    /**
     * For more details see {@link CompletableFuture#isDone()}
     */
    public boolean isDone()
    {
        return future.isDone();
    }

    /**
     * For more details see {@link CompletableFuture#isCompletedExceptionally()}
     */
    public boolean isCompletedExceptionally()
    {
        return future.isCompletedExceptionally();
    }

    /**
     * Returns the core {@link CompletableFuture} object
     */
    public CompletableFuture<T> getFuture()
    {
        return future;
    }
}
