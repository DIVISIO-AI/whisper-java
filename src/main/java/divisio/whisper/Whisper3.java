package divisio.whisper;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.audio.processor.AudioProcessor;
import ai.djl.audio.processor.LogMelSpectrogram;
import ai.djl.audio.processor.PadOrTrim;
import ai.djl.engine.Engine;
import ai.djl.modality.audio.Audio;
import ai.djl.modality.nlp.DefaultVocabulary;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.ParameterStore;
import ai.djl.util.JsonUtils;
import com.google.gson.reflect.TypeToken;
import divisio.whisper.token.WhisperAnyToken;
import divisio.whisper.token.Whisper3Language;
import divisio.whisper.token.Whisper3SpecialToken;
import divisio.whisper.token.Whisper3Timestamp;
import divisio.whisper.token.WhisperToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The {@code Whisper} class is used to transcribe speech to text.
 *
 * <p>It is based on the huggingface implementation of Whisper.
 */
public class Whisper3 implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Whisper3.class);

    /**
     * Cache location for whisper files.
     */
    private static final Path WHISPER_CACHE = Path.of(System.getProperty("user.home"), ".whisper");

    /**
     * Base path of where to find all relevant model files.
     */
    private final Path basePath;

    /**
     * Device of this Whisper instance.
     */
    private final Device device;

    /**
     * NDManager of this Whisper instance.
     */
    private final NDManager whisperManager;

    private final List<AudioProcessor> preprocessors;
    private final DefaultVocabulary vocabulary;

    private final Model encoder;
    private final Model decoder;
    private final Model decoderCrossAttention;

    private final NDIndex suppressionMask;
    private final NDArray negativeInfinity;

    private static final String RESOURCE_MEL_FILTER = "whisper_v3_mel_filter.npz";
    private static final String RESOURCE_ADDED_TOKENS = "whisper_v3_added_tokens.json";
    private static final String RESOURCE_VOCAB = "whisper_v3_vocab.json";
    private static final String RESOURCE_DECODER = "whisper_v3_decoder.pt";
    private static final String RESOURCE_ENCODER = "whisper_v3_encoder.pt";
    private static final String RESOURCE_DECODER_CROSS_ATTENTION_INIT = "whisper_v3_decoder_cross_attention_initializer.pt";

    static {
        if (!WHISPER_CACHE.toFile().exists()) {
            WHISPER_CACHE.toFile().mkdir();
        }
    }
    /**
     * Special token pattern. Used to remove those tokens from Whisper transcriptions.
     */
    private final Pattern specialTokenPattern = Pattern.compile("<\\|[a-z0-9.]+\\|>");

    public static Whisper3 instance() {
        return instance(Engine.getInstance().defaultDevice());
    }

    public static Whisper3 instance(Device device) {
        extractResourceToCache(RESOURCE_MEL_FILTER);
        extractResourceToCache(RESOURCE_ADDED_TOKENS);
        extractResourceToCache(RESOURCE_VOCAB);
        extractResourceToCache(RESOURCE_DECODER);
        extractResourceToCache(RESOURCE_ENCODER);
        extractResourceToCache(RESOURCE_DECODER_CROSS_ATTENTION_INIT);
        return instance(WHISPER_CACHE, device);
    }

    public static Whisper3 instance(Path path) {
        return instance(path, Engine.getInstance().defaultDevice());
    }

    public static Whisper3 instance(Path path, Device device) {
        return new Whisper3(path, device);
    }

    /**
     * Whisper constructor with a base path and device to load the model onto.
     * @param path base path of the model files
     */
    private Whisper3(Path path, Device device) {
        // early out for non-GPU until we get a CPU version running
        if (!device.isGpu()) {
            throw new IllegalStateException("Currently, Whisper only works on GPU.");
        }

        this.basePath = path;
        this.device = device;
        this.whisperManager = NDManager.newBaseManager(this.device);

        whisperManager.setName("whisper");
        whisperManager.getParentManager().setName("SYSTEM");

        try {
            this.preprocessors = loadPreprocessors(this.basePath, this.whisperManager);
            this.vocabulary = loadVocabulary(this.basePath);

            this.encoder = loadModel(RESOURCE_ENCODER);
            this.decoder = loadModel(RESOURCE_DECODER);
            this.decoderCrossAttention = loadModel(RESOURCE_DECODER_CROSS_ATTENTION_INIT);

            this.suppressionMask = setupSuppressionMask(this.whisperManager, this.vocabulary.size());
            this.negativeInfinity = whisperManager.create(Float.NEGATIVE_INFINITY)
                    .toType(DataType.FLOAT16, false);

        } catch (IOException | MalformedModelException e) {
            throw new RuntimeException("Could not create whisper instance");
        }
    }

    /**
     * Create a {@link WhisperTask} using this {@code Whisper} instance.
     * <p>Just a convenience wrapper method for {@link WhisperTask#task(Whisper3)}.
     * @return a {@link WhisperTask}
     */
    public WhisperTask task() {
        return WhisperTask.task(this);
    }

    /**
     * Process the given {@link Audio} with a default set of start tokens.
     * Language of the input audio is detected by whisper, it is transcribed.
     *
     * <p>To facilitate configuration, it is recommended to use {@link WhisperTask} instead of
     * calling this method directly. Create a {@link WhisperTask} using {@link Whisper3#task()}.
     *
     * @param audio the input {@link Audio} to process.
     * @return a {@link WhisperResult} containing the transcribed text.
     */
    public WhisperResult process(final Audio audio) {
        WhisperToken[] initTokens = {
                Whisper3SpecialToken.START_OF_TRANSCRIPT,
                Whisper3Language.AUTO,
                Whisper3SpecialToken.TRANSCRIBE,
                Whisper3SpecialToken.NO_TIMESTAMPS
        };
        return process(audio, initTokens);
    }

    /**
     * Process the given {@link Audio} with the desired start tokens.
     *
     * <p>To facilitate configuration, it is recommended to use {@link WhisperTask} instead of
     * calling this method directly. Create a {@link WhisperTask} using {@link Whisper3#task()}.
     *
     * @param audio the input {@link Audio} to process.
     * @param startTokens list of start tokens to guide the Whisper model.
     * @return a {@link WhisperResult} containing the transcribed text.
     */
    public WhisperResult process(final Audio audio, final WhisperToken... startTokens) {
        return process(audio, List.of(startTokens));
    }

    /**
     * Process the given {@link Audio} with the desired start tokens.
     *
     * <p>To facilitate configuration, it is recommended to use {@link WhisperTask} instead of
     * calling this method directly. Create a {@link WhisperTask} using {@link Whisper3#task()}.
     *
     * @param audio the input {@link Audio} to process.
     * @param startTokens array of start tokens to guide the Whisper model.
     * @return a {@link WhisperResult} containing the transcribed text.
     */
    public WhisperResult process(final Audio audio, final List<WhisperToken> startTokens) {
        try (NDManager transcriptionManager = whisperManager.newSubManager()) {
            transcriptionManager.setName("transcription_manager");
            // audio -> mel spectrogram
            NDList processedInput = processInput(transcriptionManager, audio);

            // encoder pass
            NDArray encoderOutput = forward(encoder, processedInput).singletonOrThrow();

            // setup initial token
            long initToken;
            if (startTokens.size() == 0) {
                initToken = Whisper3SpecialToken.START_OF_TRANSCRIPT.getTokenId();
            } else {
                initToken = startTokens.get(0).getTokenId();
            }

            // initialize NDArray that will hold all tokens during the decoder process
            NDArray previousTokenIds = transcriptionManager
                    .create(new long[] { initToken })
                    .expandDims(0) // must be 2D
                    .toDevice(device, false);

            // init kv cache
            NDList keyValueCache = initKeyValueCache(transcriptionManager, encoderOutput);

            // special conditioning for timestamps; necessary to force-negate some logits later in the decoder
            boolean withTimestamps = true;
            if (startTokens.contains(Whisper3SpecialToken.NO_TIMESTAMPS)) {
                withTimestamps = false;
            }

            // simple max loop limit to avoid endless loops (if whisper fails to generate EOT token)
            int maxLoop = 100;
            int i = 0;

            while (true) {
                // early exit condition to avoid endless loops
                if (i >= maxLoop) {
                    break;
                }

                try (NDManager decoderPassManager = whisperManager.newSubManager()) {
                    decoderPassManager.setName("decoder_pass_manager");

                    NDArray lastToken = previousTokenIds.get(0, i).reshape(1, 1);

                    // input consists of a flat list containing the last token, the encoder output,
                    // and the entire key_value_cache flattened
                    NDList decoderInputs = new NDList(lastToken, encoderOutput).addAll(keyValueCache);
                    NDList output = forward(decoder, decoderInputs);

                    // first index contains the next predicted token
                    NDArray decoderOutput = output.get(0);
                    decoderOutput.attach(decoderPassManager);
                    // all other indices are the new key_value_cache
                    NDList pastKeyValueCache = output.subNDList(1);

                    NDArray logits = decoderOutput.get("0,-1:,:").duplicate();

                    // SuppressTokensLogitsProcessor
                    // suppresses a specific set of tokens, always, for some probably good reason
                    logits.set(suppressionMask, negativeInfinity);

                    // force the start tokens (adapted from ForceTokensLogitsProcessor)
                    if (i + 1 < startTokens.size()) {
                        WhisperToken startToken = startTokens.get(i + 1);
                        // null tokens and indices < 0 are skipped
                        if (startToken != null && startToken.getTokenId() >= 0) {
                            logits.set(new NDIndex(":,:"), negativeInfinity);
                            logits.set(new NDIndex(":,{}", startToken.getTokenId()), 0);
                        }
                    }

                    // suppress specific tokens that probably are commonly spit out by whisper immediately
                    // after starting and would break it somehow
                    // (adapted from: SuppressTokensAtBeginLogitsProcessor)
                    if (previousTokenIds.size(1) == 3) {
                        // 220 = some random token? taken from huggingface whisper code
                        logits.set(new NDIndex(":,220"), negativeInfinity);
                        logits.set(new NDIndex(":,{}", Whisper3SpecialToken.END_OF_TEXT.getTokenId()), negativeInfinity);

                        // if timestamps are desired, suppress these 2 tokens that prevent proper timestamp generation
                        if (withTimestamps) {
                            logits.set(new NDIndex(":,{}", Whisper3SpecialToken.NO_TIMESTAMPS.getTokenId()), negativeInfinity);
                            logits.set(new NDIndex(":,{}", Whisper3Timestamp.MIN_TIMESTAMP_TOKEN.getTokenId()), negativeInfinity);
                        }
                    }

                    // greedy decoding, attach to previous tokens
                    NDArray currentToken = logits.argMax();
                    previousTokenIds = previousTokenIds.concat(currentToken.reshape(1, 1), 1);
                    previousTokenIds.attach(transcriptionManager);

                    // close previous cache to store a new one
                    keyValueCache.close();
                    // TODO: in python, the self-attention-cache only grows to the size of 16,
                    //  while this cache can theoretically grow endlessly... check if it matters
                    keyValueCache = pastKeyValueCache;
                    keyValueCache.attach(transcriptionManager);
                } finally {
                    i++;
                }

                // if EOT, break out
                if (Whisper3SpecialToken.END_OF_TEXT.getTokenId() == previousTokenIds.get("0,-1").getLong()) {
                    break;
                }
            }

            // token ids -> raw tokens
            List<WhisperToken> parsedTokens = parseTokens(previousTokenIds);
            // transform tokens to UTF-8
            String rawResult = Whisper3TokenDecoder.rawTokensToText(
                    parsedTokens.stream().map(WhisperToken::getToken).toList()
            );
            // strip special tokens
            String cleanedResult = removeSpecialTokens(rawResult);

            return new WhisperResult(rawResult, cleanedResult, parsedTokens);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Model loadModel(String name) throws MalformedModelException, IOException {
        final Model model = Model.newInstance(name, device);
        model.load(this.basePath, null, Collections.singletonMap("mapLocation", "true"));
        this.whisperManager.attachInternal(name, model.getNDManager());
        return model;
    }

    private NDList forward(final Model model, final NDList inputs) {
        ParameterStore parameterStore = new ParameterStore(inputs.getManager(), false);
        NDList result = model.getBlock()
                .forward(parameterStore, inputs, false);
        result.attach(inputs.getManager());
        return result;
    }

    private NDList initKeyValueCache(NDManager manager, NDArray encoderOutput) {
        NDList kvCrossAttentions = forward(decoderCrossAttention, new NDList(encoderOutput));
        NDArray kvSelfAttention = manager.zeros(new Shape(1, 20, 0, 64), DataType.FLOAT16);

        NDList pastKeyValues = new NDList();
        for (int i = 0; i < 32; ++i) {
            pastKeyValues.add(kvSelfAttention);
            pastKeyValues.add(kvSelfAttention);
            pastKeyValues.add(kvCrossAttentions.get(i * 2));
            pastKeyValues.add(kvCrossAttentions.get(i * 2 + 1));
        }

        return pastKeyValues;
    }

    private static List<AudioProcessor> loadPreprocessors(final Path basePath, final NDManager manager) throws IOException {
        Path melFile = basePath.resolve(RESOURCE_MEL_FILTER);
        List<AudioProcessor> preprocessors = new ArrayList<>();
        preprocessors.add(new PadOrTrim(480000));
        preprocessors.add(LogMelSpectrogram.newInstance(melFile, 128, manager));
        return preprocessors;
    }

    private static DefaultVocabulary loadVocabulary(final Path basePath) {
        Map<String, Integer> vocab;
        Map<String, Integer> added;
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();

        Path vocabPath = basePath.resolve(RESOURCE_VOCAB);
        Path addedTokensPath = basePath.resolve(RESOURCE_ADDED_TOKENS);
        try (Reader reader = Files.newBufferedReader(vocabPath)) {
            vocab = JsonUtils.GSON.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException("Could not read vocabulary file");
        }
        try (Reader reader = Files.newBufferedReader(addedTokensPath)) {
            added = JsonUtils.GSON.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException("Could not read added_tokens file");
        }
        String[] result = new String[vocab.size() + added.size()];
        vocab.forEach((key, value) -> result[value] = key);
        added.forEach((key, value) -> result[value] = key);
        return new DefaultVocabulary(Arrays.asList(result));
    }

    private NDList processInput(NDManager manager, Audio input) {
        NDArray samples = manager.create(input.getData());
        for (AudioProcessor processor : preprocessors) {
            samples = processor.extractFeatures(samples.getManager(), samples);
        }
        samples = samples.expandDims(0).toType(DataType.FLOAT16, true);
        return new NDList(samples);
    }

    /**
     * Parse the tokens by replacing each token id in the result with the
     * corresponding token string representation in the vocabulary.
     * @param result the predicted token ids.
     * @return a list of strings representing the tokens.
     */
    private List<WhisperToken> parseTokens(NDArray result) {
        List<WhisperToken> sentence = new ArrayList<>();
        for (long tokenId : result.toLongArray()) {
            String token = vocabulary.getToken(tokenId);
            sentence.add(new WhisperAnyToken(tokenId, token));

            if (Whisper3SpecialToken.END_OF_TEXT.getToken().equals(token)) {
                break;
            }
        }

        return sentence;
    }

    private static NDIndex setupSuppressionMask(final NDManager manager, long vocabSize) {
        // from Whisper v3 config, therefore currently only works for Whisper v3
        // https://huggingface.co/openai/whisper-large-v3/blob/main/generation_config.json # suppress_tokens
        int[] indices = new int[]{
                1, 2, 7, 8, 9, 10, 14, 25, 26, 27, 28, 29, 31, 58, 59, 60, 61, 62, 63, 90, 91, 92, 93, 359, 503, 522, 542, 873, 893, 902, 918, 922, 931, 1350, 1853, 1982, 2460, 2627, 3246, 3253, 3268, 3536, 3846, 3961, 4183, 4667, 6585, 6647, 7273, 9061, 9383, 10428, 10929, 11938, 12033, 12331, 12562, 13793, 14157, 14635, 15265, 15618, 16553, 16604, 18362, 18956, 20075, 21675, 22520, 26130, 26161, 26435, 28279, 29464, 31650, 32302, 32470, 36865, 42863, 47425, 49870, 50254, 50258, 50359, 50360, 50361, 50362, 50363
        };

        NDArray mask = manager.zeros(new Shape(vocabSize), DataType.BOOLEAN);

        // set the mask to true at the specified indices
        for (int index : indices) {
            mask.set(new NDIndex(index), 1);
        }

        return new NDIndex().addBooleanIndex(mask.expandDims(0));
    }

    /**
     * Removes special tokens from the input string.
     * A token is defined as a sequence starting with "<|", ending with "|>",
     * and containing only lowercase letters in between.
     *
     * @param input The input string potentially containing tokens.
     * @return A string with all tokens removed.
     */
    private String removeSpecialTokens(final String input) {
        // Replace all occurrences of the pattern in the input string with an empty string
        return this.specialTokenPattern.matcher(input).replaceAll("").trim();
    }

    /**
     * Extract the given resource to the cache location.
     * @param resource the resource name.
     */
    private static void extractResourceToCache(String resource) {

        try (InputStream in = Whisper3.class.getResourceAsStream("/" + resource)) {
            if (in == null) {
                throw new RuntimeException("Could not find whisper resource. Are you sure you added the whisper dependency?");
            }

            Path target = WHISPER_CACHE.resolve(resource);

            // do not override if it already exists
            if (!Files.exists(target)) {
                FileOutputStream out = new FileOutputStream(target.toFile());
                in.transferTo(out);
                out.close();
                log.info("Whisper resource '{}' cached.", resource);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        this.whisperManager.close();
    }
}
