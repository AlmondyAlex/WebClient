package com.web.client;

public interface Callbacks
{
    void onResponse(Response[] responses);
    void onException(Exception ex);
}
