package com.web.client;

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
    String url = null;
    String method = null;
    ArrayList<Header> headers = null;

    String contentType = null;
    String body = null;

    public Request(){}

    public static RequestBuilder newBuilder()
    {
        return new RequestBuilder();
    }

    @Override
    public Request clone()
    {
        Request request = new Request();
        request.url = this.url;
        request.method = this.method;

        if(this.headers != null)
        {
            int n = headers.size();
            request.headers = new ArrayList<>(n);
            request.headers.addAll(this.headers);
        }

        request.contentType = this.contentType;
        request.body = this.body;

        return request;
    }

    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public ArrayList<Header> getHeaders() { return headers; }
    public String getBody() { return body; }
}
