package divisio.whisper.token;

/**
 * Base class to represent any Whisper 3 token.
 */
public class WhisperAnyToken implements WhisperToken {

    private final long tokenId;
    private final String token;

    /**
     * Constructor taking the token id and the corresponding token.
     * @param tokenId token id
     * @param token string representation of the token
     */
    public WhisperAnyToken(long tokenId, String token) {
        this.tokenId = tokenId;
        this.token = token;
    }

    /**
     * Get the token id.
     * @return token id.
     */
    @Override
    public long getTokenId() {
        return this.tokenId;
    }

    /**
     * Get the token.
     * @return token.
     */
    @Override
    public String getToken() {
        return this.token;
    }
}
