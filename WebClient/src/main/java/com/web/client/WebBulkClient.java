package com.web.client;

/**
 * Static class for sending arrays of Requests (bulks) and handling their Responses.
 * Basically an extension of {@link WebClient}, all methods and properties are present
 *
 * @author Aleksei Karlovich
 */
public class WebBulkClient extends WebClient
{
    WebBulkClient(){ super(); }

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

    public static void sendAsync(Request[] reqs, int conTimeout, int wait, Callbacks callback)
    {
        executor.execute(() -> {
            try
            {
                int n = reqs.length;
                Response[] resps = new Response[n];

                for(int i = 0; i<n; i++)
                {
                    resps[i] = sendSync(reqs[i], conTimeout);
                    Thread.sleep(wait);
                }

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

    public static void sendAsync(Request[] reqs, Callbacks callback)
    {
        sendAsync(reqs, CONNECTION_TIMEOUT, callback);
    }
}
