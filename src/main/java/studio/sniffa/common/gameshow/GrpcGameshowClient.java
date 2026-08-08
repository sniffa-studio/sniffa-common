package studio.sniffa.common.gameshow;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import studio.sniffa.common.gameshow.grpc.v1.CreateEventRequest;
import studio.sniffa.common.gameshow.grpc.v1.DrawRequest;
import studio.sniffa.common.gameshow.grpc.v1.DrawResponse;
import studio.sniffa.common.gameshow.grpc.v1.FindActiveEventRequest;
import studio.sniffa.common.gameshow.grpc.v1.GameshowServiceGrpc;
import studio.sniffa.common.gameshow.grpc.v1.ListEntriesRequest;
import studio.sniffa.common.gameshow.grpc.v1.ListEntriesResponse;
import studio.sniffa.common.gameshow.grpc.v1.ListPendingPanelEventsRequest;
import studio.sniffa.common.gameshow.grpc.v1.ListPendingPanelEventsResponse;
import studio.sniffa.common.gameshow.grpc.v1.MarkPanelPostedRequest;
import studio.sniffa.common.gameshow.grpc.v1.SubmitEntryRequest;
import studio.sniffa.common.gameshow.grpc.v1.SubmitEntryResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * gRPC implementation of {@link GameshowClient} — same operations as {@link GameshowApiClient}
 * (REST), different wire format. Selected via config (the bot's {@code GAMESHOW_TRANSPORT} flag);
 * translates gRPC's unchecked {@link StatusRuntimeException} into the checked {@link IOException}
 * the interface already uses, so callers don't need transport-specific catch blocks.
 *
 * <p>The generated proto message classes {@code EventInfo}/{@code Entry}/{@code Winner} share
 * simple names with this package's own domain records, so they're referenced fully-qualified
 * below instead of imported.
 */
public final class GrpcGameshowClient implements GameshowClient, AutoCloseable {

    private final ManagedChannel channel;
    private final GameshowServiceGrpc.GameshowServiceBlockingStub stub;

    public GrpcGameshowClient(String target) {
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = GameshowServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public EventInfo createEvent(String title, String description, long guildId, long channelId) throws IOException {
        CreateEventRequest request = CreateEventRequest.newBuilder()
                .setTitle(title)
                .setDescription(description)
                .setGuildId(guildId)
                .setChannelId(channelId)
                .build();
        return toDomain(call(() -> stub.createEvent(request)));
    }

    @Override
    public Optional<EventInfo> findActiveEvent(long channelId) throws IOException {
        FindActiveEventRequest request = FindActiveEventRequest.newBuilder()
                .setChannelId(channelId)
                .build();
        try {
            return Optional.of(toDomain(stub.findActiveEvent(request)));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return Optional.empty();
            }
            throw asIoException(e);
        }
    }

    @Override
    public EntryOutcome submitEntry(String eventId, long discordUserId, String discordTag,
                                     String ignUsername, String discordNameTyped) throws IOException {
        SubmitEntryRequest request = SubmitEntryRequest.newBuilder()
                .setEventId(eventId)
                .setDiscordUserId(discordUserId)
                .setDiscordTag(discordTag)
                .setIgnUsername(ignUsername)
                .setDiscordNameTyped(discordNameTyped)
                .build();
        SubmitEntryResponse response = call(() -> stub.submitEntry(request));
        return switch (response.getStatus()) {
            case ENTRY_OUTCOME_STATUS_CREATED -> new EntryOutcome.Created();
            case ENTRY_OUTCOME_STATUS_ALREADY_JOINED -> new EntryOutcome.AlreadyJoined();
            default -> new EntryOutcome.Error("unexpected status: " + response.getStatus());
        };
    }

    @Override
    public List<Winner> draw(String eventId, int count, boolean dryRun) throws IOException {
        DrawRequest request = DrawRequest.newBuilder()
                .setEventId(eventId)
                .setCount(count)
                .setDryRun(dryRun)
                .build();
        DrawResponse response = call(() -> stub.draw(request));
        return response.getWinnersList().stream()
                .map(w -> new Winner(w.getDiscordUserId(), w.getDiscordTag(), w.getIgnUsername()))
                .toList();
    }

    @Override
    public List<Entry> listEntries(String eventId) throws IOException {
        ListEntriesRequest request = ListEntriesRequest.newBuilder()
                .setEventId(eventId)
                .build();
        ListEntriesResponse response = call(() -> stub.listEntries(request));
        return response.getEntriesList().stream()
                .map(e -> new Entry(e.getId(), e.getDiscordUserId(), e.getDiscordTag(),
                        e.getIgnUsername(), e.getDiscordNameTyped(), e.getDrawn()))
                .toList();
    }

    @Override
    public List<PendingPanelEvent> listPendingPanelEvents() throws IOException {
        ListPendingPanelEventsResponse response = call(() ->
                stub.listPendingPanelEvents(ListPendingPanelEventsRequest.getDefaultInstance()));
        return response.getEventsList().stream()
                .map(e -> new PendingPanelEvent(e.getEventId(), e.getTitle(), e.getDescription(),
                        e.getGuildId(), e.getChannelId()))
                .toList();
    }

    @Override
    public void markPanelPosted(String eventId) throws IOException {
        MarkPanelPostedRequest request = MarkPanelPostedRequest.newBuilder().setEventId(eventId).build();
        call(() -> stub.markPanelPosted(request));
    }

    @Override
    public void close() {
        channel.shutdown();
    }

    private static EventInfo toDomain(studio.sniffa.common.gameshow.grpc.v1.EventInfo proto) {
        return new EventInfo(proto.getEventId(), proto.getTitle(), proto.getDescription());
    }

    private static <T> T call(Supplier<T> call) throws IOException {
        try {
            return call.get();
        } catch (StatusRuntimeException e) {
            throw asIoException(e);
        }
    }

    private static IOException asIoException(StatusRuntimeException e) {
        return new IOException("gRPC call failed: " + e.getStatus(), e);
    }
}
