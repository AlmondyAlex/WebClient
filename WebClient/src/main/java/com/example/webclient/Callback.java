package com.example.webclient;

/**
 * This interface is used to specify which actions to perform
 * in the worker thread given the network response when sending
 * request asynchronously.
 *
 * @author Aleksei Karlovich
 */
public interface Callback
{
    void onSuccess(Response response);
    void onFailure(Response response);
    void onException(Exception ex);
}
