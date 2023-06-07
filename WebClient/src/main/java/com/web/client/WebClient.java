package com.web.client;

import android.annotation.TargetApi;
import android.os.Handler;

import com.web.callback.ErrorHandler;
import com.web.callback.ResponseHandler;
import com.web.exceptions.ConnectionException;
import com.web.exceptions.RequestException;
import com.web.utils.DeferredResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebClient extends HandlerExecutor
{
    public int CONNECTION_TIMEOUT = 3000;
    public int RETRIEVAL_TIMEOUT = 3000;

    public WebClient()
    {
        super();
    }

    public WebClient(ExecutorService executor, Handler handler)
    {
        super(executor, handler);
    }

    public Response sendSync(Request request, int timeout)
    {
        try
        {
            validateRequest(request);

            URL url = new URL(request.url);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setConnectTimeout(timeout);

            String method = request.method;
            con.setRequestMethod(method);


            if(request.headers.size() != 0)
                for(Header header : request.headers)
                    con.setRequestProperty(header.key, header.value);

            if(method.equals(Request.POST) || method.equals(Request.PUT))
            {
                sendBody(con, request.contentType, request.body);
            }

            return getResponse(con);
        }
        catch (IOException ex)
        {
            throw new ConnectionException(ex);
        }
    }

    public Response sendSync(Request request)
    {
        return sendSync(request, CONNECTION_TIMEOUT);
    }

    public void sendAsync(Request req, int timeout, ResponseHandler onResponse, ErrorHandler onException)
    {
        executor.execute(() ->
        {
            try
            {
                Response res = sendSync(req, timeout);

                if (onResponse != null) handler.post(() -> onResponse.handle(res));
            }
            catch (Exception ex)
            {
                if(onException != null) handler.post(() -> onException.handle(ex));
            }
        });
    }

    public void sendAsync(Request req, ResponseHandler onResponse, ErrorHandler onException)
    {
        sendAsync(req, CONNECTION_TIMEOUT, onResponse, onException);
    }

    @TargetApi(24)
    public DeferredResult<Response> sendAsync(Request req, int timeout)
    {
        return DeferredResult.supplyAsync(() -> sendSync(req, timeout), executor);
    }

    @TargetApi(24)
    public DeferredResult<Response> sendAsync(Request req)
    {
        return sendAsync(req, CONNECTION_TIMEOUT);
    }

    public Response sendAsyncAndWait(Request req, int conTimeout, int getTimeout)
    {
        try
        {
            Future<Response> res = executor.submit(() -> sendSync(req, conTimeout));

            return res.get(getTimeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException ex)
        {
            throw new ConnectionException(ex);
        }
    }

    public Response sendAsyncAndWait(Request req)
    {
        return sendAsyncAndWait(req, CONNECTION_TIMEOUT, RETRIEVAL_TIMEOUT);
    }

    /* HELPER METHODS */
    private void validateRequest(Request request)
    {
        if(request.url == null) throw new RequestException("Request must contain a url");
        if(request.method == null) throw new RequestException("Request must contain a method");
    }

    private static void sendBody(HttpURLConnection con, String contentType, String body) throws IOException
    {
        if(contentType == null || body == null) return;

        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", contentType);

        try(OutputStream os = con.getOutputStream())
        {
            byte[] out = body.getBytes(StandardCharsets.UTF_8);
            os.write(out, 0, out.length);
        }
    }

    private static Response getResponse(HttpURLConnection con) throws IOException
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
