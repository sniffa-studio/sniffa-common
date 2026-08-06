package studio.sniffa.common.trace;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import studio.sniffa.common.logging.CorrelationContext;

/**
 * Reads the correlation ID a {@link CorrelationClientInterceptor} attached and makes it available
 * via {@link CorrelationContext#current()} for the duration of the call, then clears it - so gRPC
 * service implementations see the same correlation ID that HTTP handlers already do.
 *
 * <p>Wraps {@code onHalfClose} rather than the whole {@code interceptCall} - for a unary call, the
 * generated stub invokes the actual service method from inside {@code onHalfClose}, so the
 * ThreadLocal set/clear has to be scoped there, not around {@code next.startCall(...)} itself
 * (which only sets up the listener and returns before the method body runs).
 */
public final class CorrelationServerInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> CORRELATION_ID =
            Metadata.Key.of(CorrelationContext.headerName(), Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String incoming = headers.get(CORRELATION_ID);
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onHalfClose() {
                if (incoming != null) {
                    CorrelationContext.set(incoming);
                }
                try {
                    super.onHalfClose();
                } finally {
                    CorrelationContext.clear();
                }
            }
        };
    }
}
