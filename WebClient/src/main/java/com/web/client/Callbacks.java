package com.web.client;

/**
 * Interface declaring actions to perform when an array of responses has been received
 * or an exceptions occurs.
 */
public interface Callbacks
{
    void onResponse(Response[] responses);
    void onException(Exception ex);
}
