package divisio.whisper;

import divisio.whisper.token.WhisperToken;

import java.util.List;

/**
 * Class to hold the whisper transcription results.
 * @param rawText Raw result text containing special tokens.
 * @param text    Result text of the transcription without the special tokens.
 * @param tokens  Unprocessed result tokens of the prediction.
 */
public record WhisperResult(String rawText, String text, List<WhisperToken> tokens) {}
