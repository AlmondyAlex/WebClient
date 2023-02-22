package com.example.webclient;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for building requests. While creating a new request,
 * specify request attributes chaining corresponding methods (i.e. {@link RequestBuilder#url(String)}
 * and calling  {@link RequestBuilder#build()} in the end.
 *
 * @author Aleksei Karlovich
 */
public class RequestBuilder
{
    String URL = null;
    String METHOD = null;
    ArrayList<Header> HEADERS = null;
    Body BODY = null;

    public RequestBuilder()
    {
        HEADERS = new ArrayList<>();
    }

    /**
     * Adds a URL to the request.
     * @param URL url to which the request will be sent
     * @return builder object
     */
    public RequestBuilder url(String URL)
    {
        this.URL = URL;
        return this;
    }

    /**
     * Adds a method to the request.
     * @param method request method
     * @return builder object
     */
    public RequestBuilder method(String method)
    {
        this.METHOD = method;
        return this;
    }

    /**
     * Adds a header as a key-value pair to the request.
     * @param key key of the header
     * @param value value of the header
     * @return builder object
     */
    public RequestBuilder header(String key, String value)
    {
        this.HEADERS.add(new Header(key, value));
        return this;
    }

    /**
     * Adds a header with a key and multiple values to the request.
     * Values are joined with a comma separating them.
     * @param data array containing the key and values
     * @return builder object
     * @throws WebRequestException if the array contains at most only the key
     */
    public RequestBuilder header(String... data) throws WebRequestException
    {
        int n = data.length;
        if(n < 2) throw new WebRequestException("Invalid header encountered");

        String key = data[0];

        String[] values = Arrays.copyOfRange(data,1, n);

        this.HEADERS.add(new Header(key, TextUtils.join(",",values)));
        return this;
    }

    /**
     * Adds a body to the request given the content type and payload to be sent.
     * @param type content type of the payload
     * @param payload payload to be sent
     * @return builder object
     */
    public RequestBuilder body(String type, String payload)
    {
        this.BODY = new Body(type, payload);
        return this;
    }

    /**
     * Builds the request with specified properties and checks for validity.
     * @return request object
     * @throws WebRequestException if the url or method is null
     */
    public Request build() throws WebRequestException
    {
        Request req = new Request(this);
        validate();
        return req;
    }

    /**
     * Validates the URL and method of the request
     * @throws WebRequestException if the url or method is null
     */
    private void validate() throws WebRequestException
    {
        if(URL == null) throw new WebRequestException("Request URL must not be null");
        if(METHOD == null) throw new WebRequestException("Request method must not be null");
    }
}
