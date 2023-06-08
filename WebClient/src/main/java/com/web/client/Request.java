package com.web.client;

import android.text.TextUtils;

import com.web.exceptions.RequestException;

import java.util.ArrayList;
import java.util.Arrays;

public class Request
{
    public final static String GET = "GET";
    public final static String POST = "POST";
    public final static String PUT = "PUT";
    public final static String DELETE = "DELETE";

    public final static String TYPE_JSON = "application/json";
    public final static String TYPE_XML = "application/xml";
    public final static String TYPE_URLENCODED = "application/x-www-form-urlencoded";
    public final static String TYPE_HTML = "text/html";
    public final static String TYPE_PLAIN = "text/plain";
    public final static String TYPE_WILDCARD = "*/*";

    String url = null;
    String method = null;
    ArrayList<Header> headers = new ArrayList<>();

    String contentType = null;
    String body = null;

    public Request(){}

    public static Request get(String url)
    {
        Request req = new Request();
        req.url = url;
        req.method = GET;
        return req;
    }

    public static Request post(String url)
    {
        Request req = new Request();
        req.url = url;
        req.method = POST;
        return req;
    }

    public Request url(String url)
    {
        this.url = url;
        return this;
    }

    public Request method(String method)
    {
        this.method = method;
        return this;
    }

    public Request header(String key, String value)
    {
        this.headers.add(new Header(key, value));
        return this;
    }

    public Request header(String... data)
    {
        int n = data.length;
        if(n < 2) throw new RequestException("Invalid header encountered");

        String key = data[0];

        String[] values = Arrays.copyOfRange(data,1, n);

        this.headers.add(new Header(key, TextUtils.join(",", values)));
        return this;
    }

    public Request body(String contentType, String body)
    {
        this.contentType = contentType;
        this.body = body;
        return this;
    }

    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public ArrayList<Header> getHeaders() { return headers; }
    public String getContentType() { return contentType; }
    public String getBody() { return body; }
}
