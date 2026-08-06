package studio.sniffa.common.trace;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import studio.sniffa.common.logging.CorrelationContext;

/**
 * Attaches the current {@link CorrelationContext} ID to every outgoing gRPC call - the gRPC
 * equivalent of what {@code SniffaHttpClient} already does for HTTP via the same
 * {@code X-Correlation-Id} header name. Closes the gap where a correlation ID currently reaches
 * the gRPC boundary (Backend&lt;-&gt;Agent, Backend&lt;-&gt;Proxy) and disappears.
 */
public final class CorrelationClientInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> CORRELATION_ID =
            Metadata.Key.of(CorrelationContext.headerName(), Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(CORRELATION_ID, CorrelationContext.current());
                super.start(responseListener, headers);
            }
        };
    }
}
