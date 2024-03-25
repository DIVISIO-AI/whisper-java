package divisio.whisper;

import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.AudioFactory;
import divisio.whisper.token.Whisper3Language;
import divisio.whisper.token.Whisper3SpecialToken;
import divisio.whisper.token.WhisperToken;
import org.bytedeco.ffmpeg.global.avutil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to easily configure a task and execute it using a {@link Whisper3} instance.
 *
 * <p>Most notably, it makes it easier to instruct {@link Whisper3} to use
 * certain start tokens that guide its transcription capabilities.
 */
public class WhisperTask {

    /**
     * The {@link Whisper3} instance to execute this task with.
     */
    private final Whisper3 whisper;

    /**
     * The input audio for {@link Whisper3} to transcribe / translate.
     */
    private Audio audio;

    /**
     * The language for this task. Default is {@link Whisper3Language#AUTO}, which lets Whisper detect the language.
     */
    private Whisper3Language lang = Whisper3Language.AUTO;

    /**
     * Whether to enable the "no-timestamps" token for Whisper. True by default.
     */
    private boolean noTimestamps = true;

    /**
     * The task token. Should be either {@link Whisper3SpecialToken#TRANSCRIBE}
     * or {@link Whisper3SpecialToken#TRANSLATE}.
     */
    private Whisper3SpecialToken task = Whisper3SpecialToken.TRANSCRIBE;

    /**
     * Private constructor. Use {@link #task(Whisper3)} or {@link Whisper3#task()}to create a task instance.
     */
    private WhisperTask(final Whisper3 whisper) {
        this.whisper = whisper;
    }

    /**
     * Create a {@code WhisperTask}.
     * @param whisper the {@link Whisper3} instance to configure this task for.
     * @return a {@code WhisperTask} to configure.
     */
    public static WhisperTask task(Whisper3 whisper) {
        return new WhisperTask(whisper);
    }

    /**
     * Set the audio input for this task.
     * @param audio the audio input.
     * @return this task.
     */
    private WhisperTask setAudio(Audio audio) {
        this.audio = audio;
        return this;
    }

    /**
     * Set the audio input language of this task.
     * @param lang the language of the audio input; might be {@code null}
     *             or {@link Whisper3Language#AUTO} to let Whisper detect the language.
     * @return this task.
     */
    public WhisperTask language(Whisper3Language lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Enable the Whisper "no-timestamps" token.
     * @return this task.
     */
    public WhisperTask noTimestamps() {
        this.noTimestamps = true;
        return this;
    }

    /**
     * Disable the Whisper "no-timestamps" token.
     * @return this task.
     */
    public WhisperTask withTimestamps() {
        this.noTimestamps = false;
        return this;
    }

    /**
     * Set the task to transcription for the audio file on the given path.
     * @param path the string path to the audio file.
     * @return this task.
     */
    public WhisperTask transcribe(String path) {
        this.task = Whisper3SpecialToken.TRANSCRIBE;
        return this.setAudio(loadAudio(path));
    }

    /**
     * Set the task to transcription for the audio file on the given path.
     * @param path the path to the audio file.
     * @return this task.
     */
    public WhisperTask transcribe(Path path) {
        this.task = Whisper3SpecialToken.TRANSCRIBE;
        return this.setAudio(loadAudio(path));
    }

    /**
     * Set the task to transcription for the given {@link Audio} instance.
     * @param audio the audio input.
     * @return this task.
     */
    public WhisperTask transcribe(Audio audio) {
        this.task = Whisper3SpecialToken.TRANSCRIBE;
        return setAudio(audio);
    }

    /**
     * Set the task to translation for the audio file on the given path.
     * @param path the string path to the audio file.
     * @return this task.
     */
    public WhisperTask translate(String path) {
        this.task = Whisper3SpecialToken.TRANSLATE;
        return this.setAudio(loadAudio(path));
    }

    /**
     * Set the task to translation for the audio file on the given path.
     * @param path the path to the audio file.
     * @return this task.
     */
    public WhisperTask translate(Path path) {
        this.task = Whisper3SpecialToken.TRANSLATE;
        return this.setAudio(loadAudio(path));
    }

    /**
     * Set the task to translation for the given {@link Audio} instance.
     * @param audio the audio input.
     * @return this task.
     */
    public WhisperTask translate(Audio audio) {
        this.task = Whisper3SpecialToken.TRANSLATE;
        return setAudio(audio);
    }

    /**
     * Loads the file on the given path into an {@link Audio} instance.
     * @param path the string path to the audio file.
     * @return instance of an {@link Audio}.
     */
    private static Audio loadAudio(String path) {
        return loadAudio(Path.of(path));
    }

    /**
     * Loads the file on the given path into an {@link Audio} instance.
     * @param path the path to the audio file.
     * @return instance of an {@link Audio}.
     */
    private static Audio loadAudio(Path path) {
        try {
            Audio audio = AudioFactory.newInstance()
                    .setChannels(1) // fixed for Whisper3
                    .setSampleRate(16000) // fixed for Whisper3
                    .setSampleFormat(avutil.AV_SAMPLE_FMT_S16P)
                    .fromFile(path);
            return audio;
        } catch (IOException e) {
            throw new RuntimeException("Could not load audio for whisper from path");
        }
    }

    /**
     * Execute this configured task.
     *
     * <p>Constructs the start tokens necessary to guide the Whisper model
     * and forwards them to the model.
     * @return a {@link WhisperResult} containing the transcribed text.
     */
    public WhisperResult execute() {
        if (this.whisper == null) {
            throw new IllegalStateException("Cannot execute WhisperTask without an instance of Whisper.");
        }

        if (this.audio == null) {
            throw new IllegalStateException("Cannot execute WhisperTask without having an input to transcribe or translate.");
        }

        List<WhisperToken> startTokens = new ArrayList<>(List.of(
                Whisper3SpecialToken.START_OF_TRANSCRIPT,
                this.lang,
                this.task
        ));

        if (this.noTimestamps) {
            startTokens.add(Whisper3SpecialToken.NO_TIMESTAMPS);
        }

        return whisper.process(this.audio, startTokens);
    }
}
