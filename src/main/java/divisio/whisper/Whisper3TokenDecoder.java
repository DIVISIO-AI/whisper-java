package divisio.whisper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Token decoder to turn the predicted Whisper tokens back into text.
 * <p>
 * Inspired by the Whisper v3 huggingface decoder, reduced to its most crucial parts to make this work.
 */
public class Whisper3TokenDecoder {

    private static final int BYTE_SIZE = 256;

    private static final Map<String, Integer> CHAR_TO_UNICODE_MAP = Whisper3TokenDecoder.createCharToUnicodeMap();

    /**
     * Convert the "raw" predicted tokens from Whisper into UTF-8 text.
     * @param rawTokens the raw predicted tokens from Whisper.
     * @return the concatenated UTF-8 text.
     */
    public static String rawTokensToText(List<String> rawTokens) {
        String joined = String.join("", rawTokens);

        byte[] byteText = new byte[joined.length()];
        for (int i = 0; i < joined.length(); i++) {
            String c = String.valueOf(joined.charAt(i));
            if (CHAR_TO_UNICODE_MAP.containsKey(c)) {
                byteText[i] = CHAR_TO_UNICODE_MAP.get(c).byteValue();
            }
        }
        return new String(byteText, StandardCharsets.UTF_8);
    }

    /**
     * Create the char-to-unicode-map necessary to convert raw char tokens into UTF-8 text.
     * @return the char-to-unicode-map necessary to convert raw char tokens into UTF-8 text.
     */
    private static Map<String, Integer> createCharToUnicodeMap() {
        Map<String, Integer> charToUnicodeMap = new HashMap<>();
        List<Integer> bytes = createByteList();
        List<Integer> chars = new ArrayList<>(bytes);

        int n = 0;
        for (int b = 0; b < BYTE_SIZE; b++) {
            if (!bytes.contains(b)) {
                bytes.add(b);
                chars.add(BYTE_SIZE + n);
                n++;
            }
        }

        for (int i = 0; i < bytes.size(); i++) {
            charToUnicodeMap.put(Character.toString(chars.get(i)), bytes.get(i));
        }

        return charToUnicodeMap;
    }

    /**
     * Create a byte list consisting of printable chars in various ASCII ranges.
     * @return a list of bytes in integer form.
     */
    private static List<Integer> createByteList() {
        List<Integer> list = new ArrayList<>(BYTE_SIZE);
        // printable ASCII range
        for (int i = '!';
             i <= '~';
             i++) {
            list.add(i);
        }
        // extended ASCII range (¡ to ¬)
        for (int i = '¡';
             i <= '¬';
             i++) {
            list.add(i);
        }
        // extended ASCII range (® to ÿ)
        for (int i = '®';
             i <= 'ÿ';
             i++) {
            list.add(i);
        }
        return list;
    }
}
