package com.web.client;

/**
 * Functional interface mainly to support lambda expressions for handling responses.
 */
@FunctionalInterface
public interface ResponseHandler
{
    void handle(Response response);
}
