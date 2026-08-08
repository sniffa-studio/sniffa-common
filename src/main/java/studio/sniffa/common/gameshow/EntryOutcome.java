package studio.sniffa.common.gameshow;

public sealed interface EntryOutcome {
    record Created() implements EntryOutcome {
    }

    record AlreadyJoined() implements EntryOutcome {
    }

    /** The event's sign-up deadline has already passed. */
    record Closed() implements EntryOutcome {
    }

    record Error(String message) implements EntryOutcome {
    }
}
