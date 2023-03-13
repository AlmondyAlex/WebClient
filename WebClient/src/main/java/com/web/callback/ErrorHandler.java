package com.web.callback;

/**
 * Functional interface mainly to support lambda expressions for handling exceptions.
 */
@FunctionalInterface
public interface ErrorHandler
{
    void handle(Exception exception);
}
