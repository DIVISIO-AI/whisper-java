package divisio.whisper.token;

import divisio.whisper.WhisperTask;

/**
 * Helper enum to hold some special Whisper v3 (huggingface) tokens.
 *
 * <p>Recommended to use in conjunction with {@link WhisperTask}.
 */
public enum Whisper3SpecialToken implements WhisperToken {
    START_OF_TRANSCRIPT(50258, "<|startoftranscript|>"),
    END_OF_TEXT(50257, "<|endoftext|>"),
    TRANSLATE(50359, "<|translate|>"),
    TRANSCRIBE(50360, "<|transcribe|>"),
    NO_TIMESTAMPS(50364, "<|notimestamps|>");

    private final long tokenId;
    private final String token;

    /**
     * Token consisting of its index and string representation.
     * @param tokenId The token index for Whisper v3
     * @param token The string representing the token
     */
    Whisper3SpecialToken(long tokenId, String token) {
        this.tokenId = tokenId;
        this.token = token;
    }

    /**
     * Get the token index of this token.
     * @return The token index.
     */
    @Override
    public long getTokenId() {
        return tokenId;
    }

    /**
     * Get the string representation of this token.
     * @return The string representation of this token.
     */
    @Override
    public String getToken() {
        return token;
    }
}
