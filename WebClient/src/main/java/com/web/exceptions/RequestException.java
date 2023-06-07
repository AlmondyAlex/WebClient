package com.web.exceptions;

/**
 * Exception class used for business logic
 *
 * @author Aleksei Karlovich
 */
public class RequestException extends RuntimeException
{
    public RequestException(String s)
    {
        super(s);
    }
    public RequestException(Throwable cause) { super(cause); }
}
