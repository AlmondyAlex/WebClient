# WebClient
Hello! This is a simple web client for handling HTTP requests on Android I wrote as an exercise in network connections on Android

This client is able to send requests both synchronously and asynchronously; for more details, please see the sections below.

Current version: 3.3.0; Min API: 21

# Request
To start off, you need to create a request. This can be done by calling static method `Request.newBuilder` that will create a new `RequestBuilder`,
upon which you can chain different methods specifying properties of the request. In the end, the `.build` method must be called.
Any request must have a `.method` and `.url` specified - if left out,
the `.build` method will throw an error.

For example, create a simple GET request to https://httpbin.org/ip:
```java
Request req = Request.newBuilder()
                     .url("https://httpbin.org/ip")
                     .method("GET")
                     .build();
```

Request headers and body can also be specified - for example, send a request to https://httpbin.org/post with "accept" header and a JSON body:
```java
Request req = Request.newBuilder()
                     .url("https://httpbin.org/post")
                     .method("POST")
                     .header("accept", "text/plain", "application/xml")
                     .body("application/json", json_body)
                     .build();
```
In this example, the multiple values for the "accept" header will be automatically joined, separated by a comma. When sending a body, its body type
must be specified.

# Response
The `Response` object is simple, it consists of the response code, response body + its type and response headers

# Sending requests
In order to send the request, static methods of `WebClient` class can be called. Let's go over some examples and use cases

_Please note that by default, the client uses the same executor for all off-thread work; and in case of asynchronous callbacks uses
the main (UI) thread handler to post results to. They are both public static members of the class, so they can be set to suit your configuration._

## Send request on the calling thread
This is the simplest scenario, when the request is sent on the calling thread, i.e. synchronously. In order to send the request, simply call
`WebClient.sendSync`, which takes the request and optional connection timeout (in millisecs) as parameters:

```java
Response res = WebClient.sendSync(req, 4000);
```

_The connection timeout parameter can be ommitted - instead, the default `CONNECTION_TIMEOUT` static member will be used. This member is public
and settable, so you can specify a different default timeout to use._


## Send a blocking request on the calling thread
By default, Android doesn't permit network connection on the main thread. However, sometimes there are situations when you need to send a request
on the main thread and wait for the Response - think of it like "async-await". In this case, the `WebClient.sendAsyncAndWait` method can be called,
which will send the request off the calling thread and will block it until the response is received:
```java
Response res = WebClient.sendAsyncAndWait(req);
```

## Send request off the calling thread
For non-blocking requests, you can call `WebClient.sendAsync` which needs a `Callback` object that consists of `OnSuccess`, `OnFailure` and `OnException` methods. 
`OnSuccess` is called when the response code is <300, `OnFailure` when it is >=300 and `OnException` is called whenever an exception occurs.
For example, send a request and print its response body to the `Log`:
```java
WebClient.sendAsync(req, new Callback() {
                @Override
                public void onSuccess(Response response) {
                    Log.i("Tag", response.getBody());
                }

                @Override
                public void onFailure(Response response) {
                    Log.i("Tag", response.getBody());
                }

                @Override
                public void onException(Exception ex) {
                    Log.e("Tag", ex.getMessage());
                }
          });
```
The `Callback` may also be `null` - in this case no actions will be performed.

Instead of the `Callback` object you can also use `ResponseHandler` and `ExceptionHandler` objects, primarily added to support lambda syntax:
```java
WebClient.sendAsync(req,
                responseSucc -> responseSucc.getBody(),
                responseFail -> responseFail.getResponseCode(),
                exception -> exception.getMessage()
          );
```

In case if both `OnSuccess` and `OnFailure` handlers are same, you can specify only one general response handler:
```java
WebClient.sendAsync(req,
                response -> response.getBody(),
                exception -> exception.getMessage()
          );
```

# Avoiding the "Callback Hell"
When using this library I've encountered a very common situation - whenever some requests that depend on the response of others need to be sent, 
it eventually comes down to a very nested callback, also called the "Callback Hell". In order to avoid it, here are two ways that can be used within
the client:

## Execute + sendSync
The simplest way is to call helper method `WebClient.execute` which will execute some code off the calling thread. For example, receive an
authentication token and send a request with it:
```java
WebClient.execute(() -> {
    Response res = WebClient.sendSync(req);
    String token = new JSONObject(res.getBody()).getString("token");

    Request authReq = new RequestWithToken(token) //new request containing the token
    Response authRes = WebClient.sendSync(authReq);

    //do stuff with the response
});
```

## DefferedResult chaining
The `DefferedResult` object is basically a wrapper around the `CompletableFuture` object with the ability to use the default `WebClient.executor`
for execution and `WebClient.handler` when posting results to the handling thread. In order to use it, call the `WebClient.sendAsync` method without
specifying any callbacks. The example above using `DefferedResult` will look like:

```java
WebClient.sendAsync(req)
         .thenApply(response -> new JSONObject(response.getBody).getString("token")) //get the token
         .thenApply(token -> requestWithToken(token)) //create a new request
         .thenApply(req -> WebClient.sendSync(req)) //send the request
         .thenPost(res -> doStuff(res)); //post to the handling thread
```

Please note that `DefferedResult` is available only when targeting API 24, since its underlying `CompletableFuture` also targets that API.
> I tried to make my own `CompletableFuture` to avoid API targeting, but honestly it was very frootless lol

# WebBulkClient
You can use this client to send arrays of requests following the same logic as in `WebClient`
To be honest, I made it primarily so that it's compatible with my app, but maybe it'll be useful for you as well

That's it, thanks for reading and/or using the library!
