package com.example.webclient;

/**
 * Class containing content type and body of a request or a response.
 *
 * @author Aleksei Karlovich
 */
public class Body
{
    public String TYPE;
    public String BODY;

    public Body(String type, String body)
    {
        this.TYPE = type; this.BODY = body;
    }
}
