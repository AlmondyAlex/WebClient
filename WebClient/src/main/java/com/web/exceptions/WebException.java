package com.web.exceptions;

/**
 * Exception class used for business logic
 *
 * @author Aleksei Karlovich
 */
public class WebException extends Exception
{
    public WebException(String s)
    {
        super(s);
    }
}
