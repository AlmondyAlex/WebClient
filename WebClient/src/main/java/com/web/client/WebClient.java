package com.web.client;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.web.exceptions.WebConnectionException;
import com.web.exceptions.WebRequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Static class for handling HTTP requests and responses on Android.
 * This client is able to send REST requests synchronously and asynchronously.
 * Sending requests synchronously on the main (UI) thread isn't recommended,
 * but do watchu want bestie
 *
 * @author Aleksei Karlovich <i>:)</i>
 */

public class WebClient
{
    public static ExecutorService executor = Executors.newSingleThreadExecutor();
    public static ExecutorCompletionService<Response> compExecutor = new ExecutorCompletionService<>(executor);
    public static Handler handler = new Handler(Looper.getMainLooper());
    public static int TIMEOUT = 3000;

    private WebClient(){}

    /**
     * Sends a request synchronously with a timeout and receives a response.
     * @param req request to send
     * @return Response object containing network response
     * @throws WebConnectionException if an exception occurs while connecting to the given url
     * @throws WebRequestException if the request url is not valid
     */
    public static Response sendSync(Request req, int timeout) throws WebConnectionException, WebRequestException
    {
        try
        {
            URL url = new URL(req.url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setConnectTimeout(timeout);

            String method = req.method;
            con.setRequestMethod(method);

            ArrayList<Header> headers = req.headers;

            if(headers != null)
                for(Header header : headers)
                    con.setRequestProperty(header.key, header.value);

            if(method.equals("POST") || method.equals("PUT"))
            {
                _sendBody(con, req.contentType, req.body);
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

    /**
     * Sends a request synchronously with a default timeout and receives a response.
     * @param req request to send
     * @return Response object containing network response
     * @throws WebConnectionException if and exception occurs while connecting to the given url
     * @throws WebRequestException if the request url is not valid
     */
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
                Response res = sendSync(req, timeout);

                if(callback != null)
                    handler.post(() ->
                    {
                        if(res.responseCode < 300) callback.onSuccess(res);
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

    /**
     * Sends a request asynchronously with a default timeout and performs on success and on failure actions
     * according to the given callback.
     * @param req request to send.
     * @param callback desired onSuccess and onFailure actions after performing the request. Can be null, then
     *                 no actions are performed.
     */
    public static void sendAsync(Request req, Callback callback)
    {
        sendAsync(req, TIMEOUT, callback);
    }

    /**
     * Send multiple requests asynchronously with their corresponding timeout and performs
     * actions based on the given callback. Requests are sent in order they appear in the array.
     * @param reqs requests to send
     * @param timeout timeout for each of the requests
     * @param callback actions to perform upon retrieval of responses or an exception
     */
    public static void sendAsync(Request[] reqs, int[] timeout, Callbacks callback)
    {
        executor.execute(() -> {
            try
            {
                int n = reqs.length;
                Response[] resps = new Response[n];

                for(int i = 0; i<n; i++)
                    resps[i] = sendSync(reqs[i], timeout[i]);

                if (callback != null)
                    handler.post(() -> callback.onResponse(resps));
            }
            catch (Exception ex)
            {
                if (callback != null)
                    handler.post(() -> callback.onException(ex));
            }
        });
    }

    /**
     * Sends multiple requests asynchronously with same timeout for each request and performs
     * actions based on the given callback. Requests are send in order they appear in the array.
     * @param reqs requests to send
     * @param timeout timeout applied for each request
     * @param callback actions to perform upon retrieval of responses or an exception
     */
    public static void sendAsync(Request[] reqs, int timeout, Callbacks callback)
    {
        executor.execute(() -> {
            try
            {
                int n = reqs.length;
                Response[] resps = new Response[n];

                for(int i = 0; i<n; i++)
                    resps[i] = sendSync(reqs[i], timeout);

                if (callback != null)
                    handler.post(() -> callback.onResponse(resps));
            }
            catch (Exception ex)
            {
                if (callback != null)
                    handler.post(() -> callback.onException(ex));
            }
        });
    }

    /**
     * Sends a request asynchronously with the given timeout and makes the calling thread wait until the Response is received
     * with the given timeout.
     * @param req request to be sent
     * @param conTimeout connection timeout in milliseconds
     * @param getTimeout result retrieval timeout in milliseconds
     * @return network response
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static Response sendAsyncAndWait(Request req, int conTimeout, int getTimeout) throws InterruptedException, ExecutionException, TimeoutException
    {
        compExecutor.submit(() -> sendSync(req, conTimeout));

        Future<Response> res = compExecutor.take();

        return res.get(getTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a request asynchronously with the default timeout and makes the calling thread wait until the Response is received
     * with the default timeout.
     * @param req request to be sent
     * @return network response
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public static Response sendAsyncAndWait(Request req) throws ExecutionException, InterruptedException, TimeoutException
    {
        return sendAsyncAndWait(req, TIMEOUT, TIMEOUT);
    }

    /**
     * Shuts down the executor, killing the worker thread.
     */
    public static void shutdown()
    {
        executor.shutdown();
    }

    /**
     * Creates a new executor if the current is shutdown. Otherwise does nothing.
     */
    public static void restart()
    {
        if(executor.isShutdown())
        {
            executor = Executors.newSingleThreadExecutor();
            compExecutor = new ExecutorCompletionService<>(executor);
        }
    }

    /**
     * Helper method to execute any command off the calling thread.
     * @param command Runnable command to execute
     */
    public static void execute(Runnable command)
    {
        executor.execute(command);
    }

    /**
     * Helper method for sending request body in case of POST and PUT requests.
     * @param con url connection object
     * @param body request body
     * @throws IOException if the request body cannot be sent
     */
    private static void _sendBody(HttpURLConnection con, String contentType, String body) throws IOException
    {
        if(contentType != null && body != null)
        {
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", contentType);

            try(OutputStream os = con.getOutputStream())
            {
                byte[] out = body.getBytes(StandardCharsets.UTF_8);
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
    private static Response _getResponse(HttpURLConnection con) throws IOException
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

        Map<String, List<String>> headers = con.getHeaderFields();
        con.disconnect();

        return new Response(responseCode, headers, sb.toString());
    }
}
