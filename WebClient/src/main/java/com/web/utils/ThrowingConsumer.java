package com.web.utils;

import android.annotation.TargetApi;

import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

/**
 * Same as {@link Consumer}, but rethrows any checked exceptions as {@link CompletionException} exceptions.
 */
@FunctionalInterface
@TargetApi(24)
public interface ThrowingConsumer<T> extends Consumer<T>
{
    @Override
    default void accept(T t)
    {
        try
        {
            acceptExceptionally(t);
        }
        catch (Exception ex)
        {
            throw new CompletionException(ex);
        }
    }

    void acceptExceptionally(T t) throws Exception;
}
