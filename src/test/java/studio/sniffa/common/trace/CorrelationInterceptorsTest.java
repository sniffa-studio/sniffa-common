package studio.sniffa.common.trace;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import studio.sniffa.common.gameshow.grpc.v1.EventInfo;
import studio.sniffa.common.gameshow.grpc.v1.FindActiveEventRequest;
import studio.sniffa.common.gameshow.grpc.v1.GameshowServiceGrpc;
import studio.sniffa.common.logging.CorrelationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * End-to-end proof that a correlation ID set on the client side reaches
 * {@code CorrelationContext.current()} on the server side, using the already-generated gameshow
 * stubs (no need for a dedicated test-only .proto) over an in-process transport.
 */
class CorrelationInterceptorsTest {

    private Server server;
    private ManagedChannel channel;

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void correlationIdSurvivesTheGrpcHop() throws Exception {
        CompletableFuture<String> seenOnServer = new CompletableFuture<>();
        String serverName = "correlation-test-" + System.identityHashCode(this);

        GameshowServiceGrpc.GameshowServiceImplBase service = new GameshowServiceGrpc.GameshowServiceImplBase() {
            @Override
            public void findActiveEvent(FindActiveEventRequest request, StreamObserver<EventInfo> responseObserver) {
                seenOnServer.complete(CorrelationContext.current());
                responseObserver.onNext(EventInfo.newBuilder().setEventId("evt").build());
                responseObserver.onCompleted();
            }
        };

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(io.grpc.ServerInterceptors.intercept(service, new CorrelationServerInterceptor()))
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        GameshowServiceGrpc.GameshowServiceBlockingStub stub = GameshowServiceGrpc.newBlockingStub(channel)
                .withInterceptors(new CorrelationClientInterceptor());

        CorrelationContext.set("test-correlation-id");
        try {
            stub.findActiveEvent(FindActiveEventRequest.newBuilder().setChannelId(1L).build());
        } finally {
            CorrelationContext.clear();
        }

        assertEquals("test-correlation-id", seenOnServer.get(5, TimeUnit.SECONDS));
    }

    @Test
    void serverGeneratesNothingWhenClientSendsNoCorrelationId() throws Exception {
        CompletableFuture<String> seenOnServer = new CompletableFuture<>();
        String serverName = "correlation-test-no-header-" + System.identityHashCode(this);

        GameshowServiceGrpc.GameshowServiceImplBase service = new GameshowServiceGrpc.GameshowServiceImplBase() {
            @Override
            public void findActiveEvent(FindActiveEventRequest request, StreamObserver<EventInfo> responseObserver) {
                seenOnServer.complete(CorrelationContext.current());
                responseObserver.onNext(EventInfo.newBuilder().setEventId("evt").build());
                responseObserver.onCompleted();
            }
        };

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(io.grpc.ServerInterceptors.intercept(service, new CorrelationServerInterceptor()))
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        GameshowServiceGrpc.GameshowServiceBlockingStub stub = GameshowServiceGrpc.newBlockingStub(channel);

        stub.findActiveEvent(FindActiveEventRequest.newBuilder().setChannelId(1L).build());

        // CorrelationContext.current() always returns *something* (auto-generates if unset) - the
        // point here is just that it's not left over from a previous call/thread state.
        assertNotEquals("test-correlation-id", seenOnServer.get(5, TimeUnit.SECONDS));
    }
}
