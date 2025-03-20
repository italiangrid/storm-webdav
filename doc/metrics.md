<!-- SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0 -->

# Metrics

The title of each subsequent section is the metrics name to be instantiated in this URL `/.storm-webdav/actuator/metrics/{metricName}` in order to get the metric measurements from StoRM WebDAV.
For example to get the number of completed HTTP server requests use `/.storm-webdav/actuator/metrics/http.server.requests`.

To see the complete list of all the possible metrics (here only a portion of all metrics are documented) perform a get request to `/.storm-webdav/actuator/metrics`.

You can also add any number of `tag=KEY:VALUE` query parameters to the end of the URL to dimensionally drill down on a meter.
For example to get the number of completed HTTP server requests with the method GET use `/.storm-webdav/actuator/metrics/http.server.requests?tag=method:GET` or to get the number of completed HTTP server requests with the method GET and 2XX as a response status code use `/.storm-webdav/actuator/metrics/http.server.requests?tag=method:GET&tag=outcome:SUCCESS`.
See `availableTags` section in the JSON returned by `/.storm-webdav/actuator/metrics/{metricName}` to see the complete list of all possibile tags for `{metricName}`.

## httpcomponents.httpclient.request

- **COUNT**:
  The number of completed client request.
  A client request is a request sent by StoRM WebDAV as a client.
  A client request is completed when the StoRM WebDAV sent the request, the server responded, the connection was closed and the response handler is terminated.
  More precisely a client request starts at the statement `observation.start()` in method `public void handle(GetTransferRequest request, TransferStatusCallback cb)` or `public void handle(PutTransferRequest request, TransferStatusCallback cb)` of the class HttpTransferClient.java and becomes complete at the statement `observation.close()` in method `public void handle(GetTransferRequest request, TransferStatusCallback cb)` or `public void handle(PutTransferRequest request, TransferStatusCallback cb)` of the class HttpTransferClient.java.
  During the time between `observation.start()` and `observation.close()` the request is not completed.

- **TOTAL_TIME**:
  The sum of the times in seconds of the completed client requests.

- **MAX**:
  The maximum of the times in seconds of the completed client requests.

## httpcomponents.httpclient.request.active

- **ACTIVE_TASKS**:
  The number of not completed client requests.
  Before sending a request this counter is incremented and after the server responded, the connection was closed and the client `responseHandler` is terminated, this counter is decremented.

- **DURATION**
  The sum of the times in seconds of the not completed client requests.
  If there are three not completed client requests started at time t1, t2, and t3 respectively, and you are now at time t, the value of duration is (t - t1) + (t - t2) + (t - t3).

## http.server.requests

- **COUNT**:
  The number of completed server requests.
  A server request is a request received by StoRM WebDAV as a server.
  A server request is completed when the `filterChain.doFilter` method invocation in the class [ServerHttpObservationFilter][ServerHttpObservationFilter] returns.
  During the time between the `doFilter` call and the `doFilter` termination of the class ServerHttpObservationFilter the server request is not completed.

- **TOTAL_TIME**:
  The sum of the times in seconds of the completed server requests.

- **MAX**:
  The maximum of the times in seconds of the completed server requests.

## http.server.requests.active

- **ACTIVE_TASKS**
  The number of not completed server requests.

- **DURATION**
  The sum of the times in seconds of the not completed server request.
  If there are three not completed server requests started at time t1, t2, and t3 respectively, and you are now at time t, the value of duration is (t - t1) + (t - t2) + (t - t3).

## jetty.connections.request

Jetty client or server requests.
The server requests are the requests received from StoRM WebDAV as a server, the client request are the requests send from StoRM WebDAV as a client.
Since StoRM WebDAV doesn't use Jetty HTTP client library, this metrics is only about Jetty server requets.

- **COUNT**:
  The number of already open and close connections.
  `http.server.requests` will count every HTTP request made to the server, including those made over reused connections, while `Jetty.connections.request` may not count those subsequent requests if they are made over an already established connection.
  This distinction is particularly relevant in scenarios involving connection reuse or keep-alive settings.

- **TOTAL_TIME**:
  The sum of the times in second of the open and closed connections.

- MAX
  The maximum of the times in seconds of the open and closed connections.

## jetty.connections.bytes.in

- **COUNT**:
  The number of times the `void record(double amount)` method of the Micrometer class [DistributionSummary][DistributionSummary] has been called when the server receives bytes sent from a remote client.
  The metrics of Jetty connections are implemented in the class [JettyConnectionMetrics][JettyConnectionMetrics] that implements the Jetty [NetworkTrafficListener][NetworkTrafficListener] interface.
  A `NetworkTrafficListener` defines some callbacks to network traffic events.
  When the server receives bytes sent from a remote client the `Incoming bytes` events are launched and the `incoming` callback is invoked.
  The `incoming` callback of the class `JettyConnectionMetrics` call the method `record` of the `DistributionSummary` object used for the bytes.in metrics.
  It is not clear when the events `Incoming bytes` events are launched, hence it is not clear what the value of COUNT really means.

- **TOTAL**:
  The total amount of received bytes sent by tracked connection.
  A connection is tracked if a `NetworkTrafficListener` has been added to the `ServerConnector`.
  Using `curl -w \\n%{size_request}\\n localhost:8085/actuator/metrics/jetty.connections.bytes.in` you can see that the value of TOTAL is correctly incremented.

- **MAX**:
  The maximum value among all values passed to the `void record(double amount)` method of Micrometer class [DistributionSummary][DistributionSummary] when the server receives bytes sent from a remote client. 
  For the same reasons in the description of COUNT it is not clear what the value of MAX really means.

## jetty.connections.bytes.out

- **COUNT**:
  The number of times the `void record(double amount)` method of the Micrometer class [DistributionSummary][DistributionSummary] has been called when the server sends bytes to a remote client.
  The metrics of Jetty connections are implemented in the class [JettyConnectionMetrics][JettyConnectionMetrics] that implements the Jetty [NetworkTrafficListener][NetworkTrafficListener] interface.
  A `NetworkTrafficListener` defines some callbacks to network traffic events. When the server sends bytes to a remote client the `Outgoing bytes` events are launched and the `outgoing` callback is invoked.
  The `outgoing` callback of the class `JettyConnectionMetrics` call the method `record` of the `DistributionSummary` object used for the bytes.out metrics.
  It is not clear when the events `Outgoing bytes` events are launched, hence it is not clear what the value of COUNT really means.

- **TOTAL**:
  The total amount of sent bytes by tracked connection.
  A connection is tracked if a `NetworkTrafficListener` has been added to the `ServerConnector`.

- **MAX**:
  The maximum value among all values passed to the `void record(double amount)` method of Micrometer class [DistributionSummary][DistributionSummary] when the server sends bytes to a remote client.
  For the same reasons in the description of COUNT it is not clear what the value of MAX really means.

## jetty.threads.busy

- **VALUE**:
  The number of busy threads in the pool.
  A thread is busy if it is executing a Jetty internal job or if it is executing a transient job in order to handle an incoming client request.

## jetty.threads.idle

- **VALUE**:
  The number of idle threads in the pool: `idleThreads = readyThreads - availableReservedThreads`.
  A thread is ready if it is ready to execute a transient job in order to handle an incoming client request.
  An available reserved thread is a thread that is not currently executing anything but has been reserved by another thread for use.
  For this reason `jetty.threads.busy + jetty.threads.idle` is not always equal to `jetty.threads.current`, because `jetty.threads.busy + jetty.threads.idle + availableReservedThreads = jetty.threads.current`.

## jetty.threads.current

- **VALUE**:
  The total number of threads in the pool: `jetty.threads.config.min <= jetty.threads.current <= jetty.threads.config.max`.

## jetty.threads.jobs

- **VALUE**: Number of jobs queued waiting for a thread.

## jvm.memory.used

- **VALUE**: The amount of used memory in bytes.

## process.files.open

- **VALUE**: The open file descriptor count.

[ServerHttpObservationFilter]:
https://github.com/spring-projects/spring-framework/blob/b660d8c553fe216337db0dac92b801fc82d8046a/spring-web/src/main/java/org/springframework/web/filter/ServerHttpObservationFilter.java#L153

[DistributionSummary]:
https://github.com/micrometer-metrics/micrometer/blob/main/micrometer-core/src/main/java/io/micrometer/core/instrument/DistributionSummary.java

[JettyConnectionMetrics]:
https://github.com/micrometer-metrics/micrometer/blob/f6ada8437c7c45a4c502af6413f0bb7eab509247/micrometer-core/src/main/java/io/micrometer/core/instrument/binder/jetty/JettyConnectionMetrics.java#L103

[NetworkTrafficListener]:
https://github.com/jetty/jetty.project/blob/426d39dbefe89eae1a696209ae7fcbfac9f76034/jetty-core/jetty-io/src/main/java/org/eclipse/jetty/io/NetworkTrafficListener.java#L34
