package com.web.exceptions;

/**
 * Exception class used for business logic
 *
 * @author Aleksei Karlovich
 */
public class WebRequestException extends WebException
{
    public WebRequestException(String s)
    {
        super(s);
    }
}
