package com.example.webclient;

import java.util.ArrayList;

/**
 * Class containing information necessary for performing a request.
 * To create a new request, call {@link Request#newBuilder()} for the request builder.
 * A request must contain URL to which this request will be sent and a sending method.
 * Request body and headers may be null.
 *
 * @author Aleksei Karlovich
 */
public class Request
{
    String URL = null;
    String METHOD = null;
    ArrayList<Header> HEADERS = null;
    Body BODY = null;

    private Request(){}

    Request(RequestBuilder builder)
    {
        this.URL = builder.URL;
        this.METHOD = builder.METHOD;
        this.HEADERS = builder.HEADERS;
        this.BODY = builder.BODY;
    }

    public static RequestBuilder newBuilder()
    {
        return new RequestBuilder();
    }

    public String url() { return URL; }
    public String method() { return METHOD; }
    public ArrayList<Header> headers() { return HEADERS; }
    public Body body() { return BODY; }
}
