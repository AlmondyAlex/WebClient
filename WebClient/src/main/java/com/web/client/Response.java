package com.web.client;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class containing network response information such as response code, headers and response body.
 *
 * @author Karlovich Aleksei
 */
public class Response
{
    int responseCode;
    ArrayList<Header> headers;
    String contentType;
    String body;

    public Response(int response_code, Map<String, List<String>> headers, String body)
    {
        this.responseCode = response_code;

        this.headers = new ArrayList<>(headers.size());

        for(Map.Entry<String, List<String>> header : headers.entrySet())
        {
            this.headers.add(new Header(
                    header.getKey(),
                    TextUtils.join(",", header.getValue())
            ));
        }

        List<String> contentHeader = headers.get("Content-Type");

        contentType = null;
        if(contentHeader != null)
            contentType = contentHeader.get(0);

        this.body = body;
    }

    public int getResponseCode() { return responseCode; }
    public ArrayList<Header> getHeaders() { return headers; }
    public String getContentType() { return contentType; }
    public String getBody() { return body; }
}
