package studio.sniffa.common.security;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * Attaches a bearer token to every outgoing gRPC call's {@code authorization} header - the
 * client-side counterpart to whatever server interceptor checks it (e.g. sniffa-backend's
 * {@code BearerAuthInterceptor}). sniffa-node-agent and sniffa-proxy both authenticate against
 * sniffa-backend's gRPC services this exact way - previously duplicated once in Java, once in
 * Kotlin, byte-for-byte the same logic either way.
 */
public final class BearerAuthClientInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final String token;

    public BearerAuthClientInterceptor(String token) {
        this.token = token;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(AUTHORIZATION, "Bearer " + token);
                super.start(responseListener, headers);
            }
        };
    }
}
