package com.web.utils;

import android.annotation.TargetApi;

import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

/**
 * Same as {@link Supplier}, but rethrows any checked exceptions as {@link CompletionException} exceptions.
 */
@FunctionalInterface
@TargetApi(24)
public interface ThrowingSupplier<T> extends Supplier<T>
{
    @Override
    default T get()
    {
        try
        {
            return getExceptionally();
        }
        catch (Exception ex)
        {
            throw new CompletionException(ex);
        }
    }

    T getExceptionally() throws Exception;
}
