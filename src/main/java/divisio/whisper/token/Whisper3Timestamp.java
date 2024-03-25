package divisio.whisper.token;

/**
 * Helper class to derive the Whisper v3 timestamp tokens for ease-of-use.
 */
public class Whisper3Timestamp implements WhisperToken {

    /**
     * Whisper v3 token id for timestamp 0 ms.
     */
    private static final long FIRST_TIMESTAMP_TOKEN_ID = 50365;

    /**
     * Whisper v3 token id for timestamp 30_000 ms (30 seconds).
     */
    private static final long LAST_TIMESTAMP_TOKEN_ID = 51865;

    /**
     * Whisper v3 first timestamp token in milliseconds.
     */
    private static final long FIRST_TIMESTAMP_MS = 0;

    /**
     * Whisper v3 last timestamp token in milliseconds.
     */
    private static final long LAST_TIMESTAMP_MS = 30_000;

    /**
     * Step size in milliseconds between timestamp tokens.
     */
    private static final long MS_STEP = 20;

    /**
     * Static helper token representing the first timestamp token at 0.0 seconds.
     */
    public static final Whisper3Timestamp MIN_TIMESTAMP_TOKEN = Whisper3Timestamp.fromTokenId(FIRST_TIMESTAMP_TOKEN_ID);

    /**
     * Static helper token representing the last timestamp token at 30.0 seconds.
     */
    public static final Whisper3Timestamp MAX_TIMESTAMP_TOKEN = Whisper3Timestamp.fromTokenId(LAST_TIMESTAMP_TOKEN_ID);

    private final long tokenId;

    /**
     * Private constructor. Use {@link #fromTokenId(long)} or {@link #fromTimestampMs(long)} instead.
     * @param tokenId the token id of this timestamp token.
     */
    private Whisper3Timestamp(long tokenId) {
        this.tokenId = tokenId;
    }

    /**
     * Create a {@link Whisper3Timestamp} from the given token id.
     *
     * <p>Checks if the given token id is valid and throws an exception otherwise.
     *
     * @param tokenId token id of the timestamp token, must be a valid Whisper v3 timestamp token.
     * @return instance of a valid {@link Whisper3Timestamp}.
     */
    public static Whisper3Timestamp fromTokenId(final long tokenId) {
        if (tokenId < FIRST_TIMESTAMP_TOKEN_ID || tokenId > LAST_TIMESTAMP_TOKEN_ID) {
            throw new IllegalArgumentException(
                    String.format("Not a valid timestamp token id, it must be between %d (inclusive) and %d (inclusive).",
                            FIRST_TIMESTAMP_TOKEN_ID, LAST_TIMESTAMP_TOKEN_ID)
            );
        }

        return new Whisper3Timestamp(tokenId);
    }

    /**
     * Create a {@link Whisper3Timestamp} from the given millisecond timestamp.
     *
     * <p>Checks if the given millisecond timestamp is valid and throws an exception otherwise.
     *
     * @param ms millisecond value of the token, must be a valid Whisper v3 timestamp token.
     * @return instance of a valid {@link Whisper3Timestamp}.
     */
    public static Whisper3Timestamp fromTimestampMs(final long ms) {
        if (ms < FIRST_TIMESTAMP_MS || ms > LAST_TIMESTAMP_MS) {
            throw new IllegalArgumentException(
                    String.format("Not a valid timestamp value, milliseconds must be between %d (inclusive) and %d (inclusive).",
                            FIRST_TIMESTAMP_MS, LAST_TIMESTAMP_MS)
            );
        }

        if (ms % MS_STEP != 0) {
            throw new IllegalArgumentException(
                    String.format("Not a valid timestamp value, milliseconds must be multiples of %d.",
                            MS_STEP)
            );
        }

        return new Whisper3Timestamp(msToTokenId(ms));
    }

    /**
     * Convert from token id to milliseconds.
     * @param tokenId the token id.
     * @return the millisecond value this token represents.
     */
    private static long tokenIdToMs(long tokenId) {
        return (tokenId - FIRST_TIMESTAMP_TOKEN_ID) * MS_STEP;
    }

    /**
     * Convert from milliseconds to token id.
     * @param ms the millisecond value of the token.
     * @return the token id corresponding with this millisecond value.
     */
    private static long msToTokenId(long ms) {
        return (ms / MS_STEP) + FIRST_TIMESTAMP_TOKEN_ID;
    }

    /**
     * Get the millisecond value of this token.
     * @return millisecond value of this token.
     */
    public long getMs() {
        return tokenIdToMs(this.tokenId);
    }

    /**
     * Get the token id.
     * @return token id.
     */
    @Override
    public long getTokenId() {
        return tokenId;
    }

    /**
     * Get the string token representation of this token.
     * @return string token representation of this token.
     */
    @Override
    public String getToken() {
        // get ms part
        final long ms = getMs() % 1000;
        // get second part
        final long s = getMs() / 1000;
        // prepend zeros, remove trailing digit (it is always zero anyway)
        final String msPadded = String.format("%03d", ms).substring(0, 2);
        return "<|" + s + "." + msPadded + "|>";
    }
}
