package com.web.callback;

import com.web.client.Response;

/**
 * Functional interface mainly to support lambda expressions for handling responses.
 */
@FunctionalInterface
public interface ResponseHandler
{
    void handle(Response response);
}
