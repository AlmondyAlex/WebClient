package com.example.webclient;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

/**
 * Web client for handling https requests and their responses on Android.
 * This client is able to send REST requests synchronously and asynchronously.
 * Using synchronous requests isn't recommended as they block the main (UI) thread.
 *
 * @author Karlovich Aleksei <i>:)</i>
 */

public class WebClient
{
    public static ExecutorService executor = Executors.newSingleThreadExecutor();
    public static Handler handler = new Handler(Looper.getMainLooper());
    public static int TIMEOUT = 3000;

    private WebClient(){}
    /**
     * Helper method for sending request body in case of POST and PUT requests.
     * @param con url connection object
     * @param body request body
     * @throws IOException if the request body cannot be sent
     */
    private static void _sendBody(HttpsURLConnection con, Body body) throws IOException
    {
        con.setDoOutput(true);

        if(body != null)
        {
            con.setRequestProperty("content-type", body.TYPE);

            try(OutputStream os = con.getOutputStream())
            {
                byte[] out = body.BODY.getBytes(StandardCharsets.UTF_8);
                os.write(out, 0, out.length);
            }
        }
    }

    /**
     * Helper method for receiving the server response to the given request.
     * @param con url connection object
     * @return {@link Response} object containing server's response
     * @throws IOException if an I/O exception occurs while creating the input stream
     */
    private static Response _getResponse(HttpsURLConnection con) throws IOException
    {
        int responseCode = con.getResponseCode();
        BufferedReader br;

        if(responseCode < 300)
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        else
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));

        StringBuilder sb = new StringBuilder(); String line;

        while((line = br.readLine()) != null)
            sb.append(line);

        br.close();

        return new Response(responseCode, con.getHeaderFields(), sb.toString());
    }

    /**
     * Sends a request synchronously with a timeout and receives a response.
     * @param req request to send.
     * @return network response.
     * @throws WebConnectionException if and exception occurs while connecting to the given url.
     * @throws WebRequestException if the request url is not valid.
     */
    public static Response sendSync(Request req, int timeout) throws WebConnectionException, WebRequestException
    {
        try
        {
            URL url = new URL(req.URL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            con.setConnectTimeout(timeout);

            String method = req.METHOD;
            con.setRequestMethod(method);

            ArrayList<Header> headers = req.HEADERS;

            if(headers != null)
                for(Header header : headers)
                    con.setRequestProperty(header.key, header.value);

            if(method.equals("POST") || method.equals("PUT"))
            {
                Body body = req.BODY;
                _sendBody(con, body);
            }

            return _getResponse(con);
        }
        catch(MalformedURLException ex)
        {
            throw new WebRequestException("Invalid URL address");
        }
        catch (SocketTimeoutException ex)
        {
            throw new WebConnectionException("Connection timed out");
        }
        catch(IOException ex)
        {
            throw new WebConnectionException("Couldn't connect to the given URL");
        }
    }

    public static Response sendSync(Request req) throws WebConnectionException, WebRequestException
    {
        return sendSync(req, TIMEOUT);
    }

    /**
     * Sends a request asynchronously with a timeout and performs on success and on failure actions
     * according to the given callback.
     * @param req request to send.
     * @param callback desired onSuccess and onFailure actions after performing the request. Can be null, then
     *                 no actions are performed.
     */
    public static void sendAsync(Request req, int timeout, Callback callback)
    {
        executor.execute(() -> {
            try
            {
                Response res = sendSync(req);

                if(callback != null)
                    handler.post(() -> {
                        if(res.RESPONSE_CODE < 300) callback.onSuccess(res);
                        else callback.onFailure(res);
                    });

            }
            catch (Exception ex)
            {
                if(callback != null)
                    handler.post(() -> callback.onException(ex));
            }
        });
    }

    public static void sendAsync(Request req, Callback callback)
    {
        sendAsync(req, TIMEOUT, callback);
    }

    public static void shutdown()
    {
        executor.shutdown();
    }

    public static void restart()
    {
        if(executor.isShutdown())
            executor = Executors.newSingleThreadExecutor();
    }

}
