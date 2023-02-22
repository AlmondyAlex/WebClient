package com.example.webclient;

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
    int RESPONSE_CODE;
    ArrayList<Header> HEADERS;
    Body BODY;

    public Response(int response_code, Map<String, List<String>> headers, String body)
    {
        this.RESPONSE_CODE = response_code;

        HEADERS = new ArrayList<>();
        for(Map.Entry<String, List<String>> header : headers.entrySet())
        {
            HEADERS.add(new Header(
                    header.getKey(),
                    TextUtils.join(",", header.getValue())
            ));
        }

        String body_type = headers.get("Content-Type").get(0);
        if(body_type == null) body_type = "unknown";

        BODY = new Body(body_type, body);
    }

    public int response_code() {return RESPONSE_CODE;}
    public ArrayList<Header> headers() {return HEADERS;}
    public Body body() {return BODY;}
}
