package com.web.client;

/**
 * Class containing key and value for a request or response header.
 *
 * @author Aleksei Karlovich
 */
public class Header
{
    public String key;
    public String value;

    public Header(String key, String value)
    {
        this.key = key;
        this.value = value;
    }
}
