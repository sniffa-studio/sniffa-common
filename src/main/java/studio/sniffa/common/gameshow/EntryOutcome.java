package studio.sniffa.common.gameshow;

public sealed interface EntryOutcome {
    record Created() implements EntryOutcome {
    }

    record AlreadyJoined() implements EntryOutcome {
    }

    record Error(String message) implements EntryOutcome {
    }
}
