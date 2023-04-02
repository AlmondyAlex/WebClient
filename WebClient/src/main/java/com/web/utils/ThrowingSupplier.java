package com.web.utils;

import android.annotation.TargetApi;

import java.util.function.Supplier;

/**
 * Same as {@link Supplier}, but rethrows any checked exceptions as {@link RuntimeException} exceptions.
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
            throw new RuntimeException(ex);
        }
    }

    T getExceptionally() throws Exception;
}
