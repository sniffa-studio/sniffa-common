package studio.sniffa.common.gameshow;

/**
 * A draw entrant staff wants placed at a specific spot instead of randomly sampled - e.g. a
 * content creator. {@code position} is 1-based. See {@link GameshowClient#draw}.
 */
public record GuaranteedPick(long discordUserId, int position) {
}
