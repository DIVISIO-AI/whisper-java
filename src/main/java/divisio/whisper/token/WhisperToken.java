package divisio.whisper.token;

/**
 * Interface for various token classes to facilitate handling of tokens in java.
 */
public interface WhisperToken {
    /**
     * Numerical ID of the token in the dictionary
     */
    long getTokenId();

    /**
     * The actual String value of the token
     */
    String getToken();
}
