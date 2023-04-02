package com.web.utils;

import android.annotation.TargetApi;

import java.util.function.Consumer;

/**
 * Same as {@link Consumer}, but rethrows any checked exceptions as {@link RuntimeException} exceptions.
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
            throw new RuntimeException(ex);
        }
    }

    void acceptExceptionally(T t) throws Exception;
}
