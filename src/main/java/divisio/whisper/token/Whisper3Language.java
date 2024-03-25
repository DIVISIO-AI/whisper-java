package divisio.whisper.token;

import divisio.whisper.WhisperTask;

/**
 * Helper enum to hold the Whisper v3 language tokens for ease-of-use.
 *
 * <p>Recommended to use in conjunction with {@link WhisperTask}.
 */
public enum Whisper3Language implements WhisperToken {
    // "auto" exists to allow Whisper to detect and determine the language itself (not actually a token of Whisper)
    AUTO(Long.MIN_VALUE, null, "auto", "automatic"),

    AFRIKAANS(50327, "<|af|>", "af", "afrikaans"),
    AMHARIC(50334, "<|am|>", "am", "amharic"),
    ARABIC(50272, "<|ar|>", "ar", "arabic"),
    ASSAMESE(50350, "<|as|>", "as", "assamese"),
    AZERBAIJANI(50304, "<|az|>", "az", "azerbaijani"),
    BASHKIR(50355, "<|ba|>", "ba", "bashkir"),
    BELARUSIAN(50330, "<|be|>", "be", "belarusian"),
    BULGARIAN(50292, "<|bg|>", "bg", "bulgarian"),
    BENGALI(50302, "<|bn|>", "bn", "bengali"),
    TIBETAN(50347, "<|bo|>", "bo", "tibetan"),
    BRETON(50309, "<|br|>", "br", "breton"),
    BOSNIAN(50315, "<|bs|>", "bs", "bosnian"),
    CATALAN(50270, "<|ca|>", "ca", "catalan"),
    CZECH(50283, "<|cs|>", "cs", "czech"),
    WELSH(50297, "<|cy|>", "cy", "welsh"),
    DANISH(50285, "<|da|>", "da", "danish"),
    GERMAN(50261, "<|de|>", "de", "german"),
    GREEK(50281, "<|el|>", "el", "greek"),
    ENGLISH(50259, "<|en|>", "en", "english"),
    SPANISH(50262, "<|es|>", "es", "spanish"),
    ESTONIAN(50307, "<|et|>", "et", "estonian"),
    BASQUE(50310, "<|eu|>", "eu", "basque"),
    PERSIAN(50300, "<|fa|>", "fa", "persian"),
    FINNISH(50277, "<|fi|>", "fi", "finnish"),
    FAROESE(50338, "<|fo|>", "fo", "faroese"),
    FRENCH(50265, "<|fr|>", "fr", "french"),
    GALICIAN(50319, "<|gl|>", "gl", "galician"),
    GUJARATI(50333, "<|gu|>", "gu", "gujarati"),
    HAWAIIAN(50352, "<|haw|>", "haw", "hawaiian"),
    HAUSA(50354, "<|ha|>", "ha", "hausa"),
    HEBREW(50279, "<|he|>", "he", "hebrew"),
    HINDI(50276, "<|hi|>", "hi", "hindi"),
    CROATIAN(50291, "<|hr|>", "hr", "croatian"),
    HAITIAN(50339, "<|ht|>", "ht", "haitian"),
    HUNGARIAN(50286, "<|hu|>", "hu", "hungarian"),
    ARMENIAN(50312, "<|hy|>", "hy", "armenian"),
    INDONESIAN(50275, "<|id|>", "id", "indonesian"),
    ICELANDIC(50311, "<|is|>", "is", "icelandic"),
    ITALIAN(50274, "<|it|>", "it", "italian"),
    JAPANESE(50266, "<|ja|>", "ja", "japanese"),
    JAVANESE(50356, "<|jw|>", "jw", "javanese"),
    GEORGIAN(50329, "<|ka|>", "ka", "georgian"),
    KAZAKH(50316, "<|kk|>", "kk", "kazakh"),
    KHMER(50323, "<|km|>", "km", "khmer"),
    KANNADA(50306, "<|kn|>", "kn", "kannada"),
    KOREAN(50264, "<|ko|>", "ko", "korean"),
    LATIN(50294, "<|la|>", "la", "latin"),
    LUXEMBOURGISH(50345, "<|lb|>", "lb", "luxembourgish"),
    LINGALA(50353, "<|ln|>", "ln", "lingala"),
    LAO(50336, "<|lo|>", "lo", "lao"),
    LITHUANIAN(50293, "<|lt|>", "lt", "lithuanian"),
    LATVIAN(50301, "<|lv|>", "lv", "latvian"),
    MALAGASY(50349, "<|mg|>", "mg", "malagasy"),
    MAORI(50295, "<|mi|>", "mi", "maori"),
    MACEDONIAN(50308, "<|mk|>", "mk", "macedonian"),
    MALAYALAM(50296, "<|ml|>", "ml", "malayalam"),
    MONGOLIAN(50314, "<|mn|>", "mn", "mongolian"),
    MARATHI(50320, "<|mr|>", "mr", "marathi"),
    MALAY(50282, "<|ms|>", "ms", "malay"),
    MALTESE(50343, "<|mt|>", "mt", "maltese"),
    MYANMAR(50346, "<|my|>", "my", "myanmar"),
    NEPALI(50313, "<|ne|>", "ne", "nepali"),
    DUTCH(50271, "<|nl|>", "nl", "dutch"),
    NYNORSK(50342, "<|nn|>", "nn", "nynorsk"),
    NORWEGIAN(50288, "<|no|>", "no", "norwegian"),
    OCCITAN(50328, "<|oc|>", "oc", "occitan"),
    PUNJABI(50321, "<|pa|>", "pa", "punjabi"),
    POLISH(50269, "<|pl|>", "pl", "polish"),
    PASHTO(50340, "<|ps|>", "ps", "pashto"),
    PORTUGUESE(50267, "<|pt|>", "pt", "portuguese"),
    ROMANIAN(50284, "<|ro|>", "ro", "romanian"),
    RUSSIAN(50263, "<|ru|>", "ru", "russian"),
    SANSKRIT(50344, "<|sa|>", "sa", "sanskrit"),
    SINDHI(50332, "<|sd|>", "sd", "sindhi"),
    SINHALA(50322, "<|si|>", "si", "sinhala"),
    SLOVAK(50298, "<|sk|>", "sk", "slovak"),
    SLOVENIAN(50305, "<|sl|>", "sl", "slovenian"),
    SHONA(50324, "<|sn|>", "sn", "shona"),
    SOMALI(50326, "<|so|>", "so", "somali"),
    ALBANIAN(50317, "<|sq|>", "sq", "albanian"),
    SERBIAN(50303, "<|sr|>", "sr", "serbian"),
    SUNDANESE(50357, "<|su|>", "su", "sundanese"),
    SWEDISH(50273, "<|sv|>", "sv", "swedish"),
    SWAHILI(50318, "<|sw|>", "sw", "swahili"),
    TAMIL(50287, "<|ta|>", "ta", "tamil"),
    TELUGU(50299, "<|te|>", "te", "telugu"),
    TAJIK(50331, "<|tg|>", "tg", "tajik"),
    THAI(50289, "<|th|>", "th", "thai"),
    TURKMEN(50341, "<|tk|>", "tk", "turkmen"),
    TAGALOG(50348, "<|tl|>", "tl", "tagalog"),
    TURKISH(50268, "<|tr|>", "tr", "turkish"),
    TATAR(50351, "<|tt|>", "tt", "tatar"),
    UKRAINIAN(50280, "<|uk|>", "uk", "ukrainian"),
    URDU(50290, "<|ur|>", "ur", "urdu"),
    UZBEK(50337, "<|uz|>", "uz", "uzbek"),
    VIETNAMESE(50278, "<|vi|>", "vi", "vietnamese"),
    YIDDISH(50335, "<|yi|>", "yi", "yiddish"),
    YORUBA(50325, "<|yo|>", "yo", "yoruba"),
    CANTONESE(50358, "<|yue|>", "yue", "cantonese"),
    CHINESE(50260, "<|zh|>", "zh", "chinese");

    private final long tokenId;
    private final String token;
    private final String isoCode;
    private final String isoLanguageName;

    /**
     * Token consisting of its index and string representation,
     * with ISO 639 language codes and names to easily retrieve them
     * via {@link #fromIsoCode} or {@link #fromLanguageName(String)}.
     *
     * @param tokenId           The token index for Whisper v3
     * @param token           The string representing the token
     * @param isoCode         The shortform language code
     * @param isoLanguageName The full language name
     */
    Whisper3Language(long tokenId, String token, String isoCode, String isoLanguageName) {
        this.tokenId = tokenId;
        this.token = token;
        this.isoCode = isoCode;
        this.isoLanguageName = isoLanguageName;
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

    /**
     * Get the shortform language code.
     * @return The shortform language code.
     */
    public String getIsoCode() {
        return isoCode;
    }

    /**
     * Get the full language name.
     * @return The full language name.
     */
    public String getIsoLanguageName() {
        return isoLanguageName;
    }

    /**
     * Retrieves a WhisperLang enum value matching the given ISO 639 two-letter language code.
     *
     * @param twoLetterCode A string representing the ISO 639 two-letter language code, e.g. "de".
     * @return The matching WhisperLang enum value, or null if no match is found.
     */
    public static Whisper3Language fromIsoCode(String twoLetterCode) {
        for (Whisper3Language lang : Whisper3Language.values()) {
            if (lang.getIsoCode().equalsIgnoreCase(twoLetterCode)) {
                return lang;
            }
        }
        return null;
    }

    /**
     * Retrieves a WhisperLang enum value matching the given ISO 639 language name.
     *
     * @param isoLanguageName A string representing the ISO 639 language name, e.g. "german".
     * @return The matching WhisperLang enum value, or null if no match is found.
     */
    public static Whisper3Language fromLanguageName(String isoLanguageName) {
        for (Whisper3Language lang : Whisper3Language.values()) {
            if (lang.getIsoLanguageName().equalsIgnoreCase(isoLanguageName)) {
                return lang;
            }
        }
        return null;
    }
}
