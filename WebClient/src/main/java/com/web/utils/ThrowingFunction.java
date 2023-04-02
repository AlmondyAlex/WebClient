package com.web.utils;

import android.annotation.TargetApi;

import java.util.function.Function;

/**
 * Same as {@link Function}, but rethrows any checked exceptions as {@link RuntimeException} exceptions.
 */
@FunctionalInterface
@TargetApi(24)
public interface ThrowingFunction<T, R> extends Function<T, R>
{
    @Override
    default R apply(T t)
    {
        try
        {
            return applyExceptionally(t);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    R applyExceptionally(T t) throws Exception;
}
