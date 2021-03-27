package nl.saxion.discord.bot.internal.smartinvoke.tokenize;

/**
 * An enum containing the different issues that may occur while tokenizing.
 * This allows us to quickly diagnose why tokenization failed without having to check the string value.
 * for now, only one failure type is actually implemented.
 */
public enum TokenizationFailureType {
    /**
     * Used if a quoted token is not closed and the tokenizer has the {@link Tokenizer#FLAG_STRICT_QUOTES} flag set.
     */
    UNBALANCED_QUOTES("Unbalanced quotes"),

    ;
    /**
     * The message that may be used to send the caller diagnostics
     */
    private final String message;

    TokenizationFailureType(String message){
        this.message = message;
    }

    /**
     * @return The textual value of the tokenization failure. This may be used for human readable diagnostics
     */
    public String getMessage() {
        return message;
    }
}
