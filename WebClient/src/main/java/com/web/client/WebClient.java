package com.web.client;

import android.annotation.TargetApi;
import android.os.Handler;
import android.os.Looper;

import com.web.callback.Callback;
import com.web.callback.ErrorHandler;
import com.web.callback.ResponseHandler;
import com.web.exceptions.WebConnectionException;
import com.web.exceptions.WebRequestException;
import com.web.utils.DeferredResult;

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Class containing static methods for handling HTTP requests and responses on Android.
 * This client is able to send requests synchronously and asynchronously.
 * Sending requests synchronously on the main (UI) thread isn't recommended,
 * but do watchu want bestie
 * <br><br>
 * By default, results of all requests are posted to the main (UI) thread. If desired, this
 * behaviour can be changed by setting the static member <code>handler</code>.
 * <br>
 * The default connection and retrieval timeouts can be changed by setting the static members
 * <code>CONNECTION_TIMEOUT</code> and <code>RETRIEVAL_TIMEOUT</code> respectively.
 * <br><br>
 * In order to avoid the "Callback Hell" when sending multiple chained requests (meaning one depends
 * on the response of the other), you can either use {@link WebClient#sendSync} in conjunction
 * with {@link WebClient#execute}, or use the {@link DeferredResult} Response returned by
 * {@link WebClient#sendAsync(Request, int)}. Please note that {@link  DeferredResult} is only available
 * when targeting API 24 and up.
 *
 * @author Aleksei Karlovich <i>:)</i>
 */

public class WebClient
{
    public static ExecutorService executor = Executors.newSingleThreadExecutor();
    public static Handler handler = new Handler(Looper.getMainLooper());
    public static int CONNECTION_TIMEOUT = 3000;
    public static int RETRIEVAL_TIMEOUT = 3000;

    WebClient(){}

    /**
     * Sends the request synchronously with a given connection timeout and returns its response.
     *
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
     * Sends the request synchronously with default connection timeout and returns its response.
     *
     * @param req request to send
     * @return Response object containing network response
     * @throws WebConnectionException if and exception occurs while connecting to the given url
     * @throws WebRequestException if the request url is not valid
     */
    public static Response sendSync(Request req) throws WebConnectionException, WebRequestException
    {
        return sendSync(req, CONNECTION_TIMEOUT);
    }

    /**
     * Sends the request asynchronously with a given connection timeout and posts onSuccess and onFailure
     * callback actions to the handling thread. The callback may be null, then no
     * actions are posted.
     *
     * @param req request to send
     * @param callback desired onSuccess and onFailure actions after performing the request
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
     * Sends the request asynchronously with default connection timeout and posts onSuccess and onFailure
     * callback actions to the handling thread. The callback may be null, then no actions are posted.
     *
     * @param req request to send
     * @param callback desired onSuccess and onFailure actions after performing the request
     */
    public static void sendAsync(Request req, Callback callback)
    {
        sendAsync(req, CONNECTION_TIMEOUT, callback);
    }

    /**
     * Sends the request asynchronously with a given connection timeout and posts onSuccess and onFailure
     * callback actions to the handling thread. The actions can be expressed with lambdas and may be null.
     *
     * @param req request to be sent
     * @param timeout connection timeout
     * @param onSuccess called when response code is < 300
     * @param onFailure called when response code is >= 300
     * @param onException called when an exception occurs
     */
    public static void sendAsync(Request req, int timeout, ResponseHandler onSuccess, ResponseHandler onFailure, ErrorHandler onException)
    {
        executor.execute(() -> {
            try
            {
                Response res = sendSync(req, timeout);

                handler.post(() ->
                {
                    if(res.responseCode < 300 && onSuccess != null) onSuccess.handle(res);
                    else if(onFailure != null) onFailure.handle(res);
                });
            }
            catch (Exception ex)
            {
                if(onException != null)
                    handler.post(() -> onException.handle(ex));
            }
        });
    }

    /**
     * Sends the request asynchronously with default connection timeout and posts onSuccess and onFailure
     * callback actions to the handling thread. The actions can be expressed with lambdas and may be null.
     *
     * @param req request to be sent
     * @param onSuccess called when response code is < 300
     * @param onFailure called when response code is >= 300
     * @param onException called when an exception occurs
     */
    public static void sendAsync(Request req, ResponseHandler onSuccess, ResponseHandler onFailure, ErrorHandler onException)
    {
        sendAsync(req, CONNECTION_TIMEOUT, onSuccess, onFailure, onException);
    }

    /**
     * Sends the request asynchronously with a given connection timeout and posts onResponse callback
     * action to the handling thread. The action can be expression with lambda and may be null.
     *
     * @param req request to be send
     * @param timeout connection timeout
     * @param onResponse called when a response is received
     * @param onException called when an exception occurs
     */
    public static void sendAsync(Request req, int timeout, ResponseHandler onResponse, ErrorHandler onException)
    {
        executor.execute(() -> {
            try
            {
                Response res = sendSync(req, timeout);

                if (onResponse != null)
                    handler.post(() -> onResponse.handle(res));
            }
            catch (Exception ex)
            {
                if(onException != null)
                    handler.post(() -> onException.handle(ex));
            }
        });
    }

    /**
     * Sends the request asynchronously with default connection timeout and posts onResponse action
     * to the handling thread. The action can be expressed with lambda and may be null.
     *
     * @param req request to be sent
     * @param onResponse called when a response has been received
     * @param onException called when an exception occurs
     */
   public static void sendAsync(Request req, ResponseHandler onResponse, ErrorHandler onException)
   {
       sendAsync(req, CONNECTION_TIMEOUT, onResponse, onException);
   }

    /**
     * Sends the request asynchronously with a given connection timeout and returns a {@link DeferredResult}
     * object representing the future Response. Call available only when targeting API 24 and up.
     *
     * @param req request to be sent
     * @param timeout connection timeout
     *
     * @return a {@link CompletableFuture} Response object
     */
   @TargetApi(24)
   public static DeferredResult<Response> sendAsync(Request req, int timeout)
   {
       return DeferredResult.supplyAsync(() -> sendSync(req, timeout), executor);
   }

    /**
     * Sends the request asynchronously with default connection timeout and returns a {@link DeferredResult}
     * object representing the future Response. Call available only when targeting API 24 and up.
     *
     * @param req request to be sent
     * @return a {@link CompletableFuture} Response object
     */
    @TargetApi(24)
    public static DeferredResult<Response> sendAsync(Request req)
    {
        return sendAsync(req, CONNECTION_TIMEOUT);
    }

    /**
     * Sends the request asynchronously with a given connection timeout and blocks the calling thread
     * until the response is received with a given retrieval timeout.
     *
     * @param req request to be sent
     * @param conTimeout connection timeout in milliseconds
     * @param getTimeout result retrieval timeout in milliseconds
     *
     * @return Response object containing network response
     *
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     * @throws TimeoutException if the wait timed out
     */
    public static Response sendAsyncAndWait(Request req, int conTimeout, int getTimeout) throws InterruptedException, ExecutionException, TimeoutException
    {
        Future<Response> res = executor.submit(() -> sendSync(req, conTimeout));

        return res.get(getTimeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a request asynchronously with default connection timeout and blocks the calling thread
     * until the response is received with default retrieval timeout.
     *
     * @param req request to be sent
     *
     * @return Response object containing network response
     *
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     * @throws TimeoutException if the wait timed out
     */
    public static Response sendAsyncAndWait(Request req) throws ExecutionException, InterruptedException, TimeoutException
    {
        return sendAsyncAndWait(req, CONNECTION_TIMEOUT, RETRIEVAL_TIMEOUT);
    }

    /**
     * Shuts down the executor, killing the worker thread.
     */
    public static void shutdown()
    {
        executor.shutdown();
    }

    /**
     * Shuts down the previous executor and starts a new one.
     */
    public static void restart()
    {
        executor.shutdown();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Helper method to execute any command off the calling thread.
     *
     * @param command Runnable command to execute
     */
    public static void execute(Runnable command)
    {
        executor.execute(command);
    }

    /**
     * Helper method for sending request body in case of POST and PUT requests.
     *
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
     *
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
