package nl.saxion.discord.bot.internal.smartinvoke.tokenizer;

/**
 * An exception thrown when the tokenizer cannot parse a message.
 */
public class TokenizationFailure extends Exception {
    /**
     * The type of the tokenization failure. This can be used to decide how to proceed with the exceptions thrown
     */
    private final TokenizationFailureType type;
    /**
     * the starting region of the bad token
     */
    private final int regionStart;
    /**
     * the end region of the bad token
     */
    private final int regionEnd;

    /**
     * Creates a new TokenizationFailure
     * @param type the type of the failure
     * @param regionStart the start of the failed token
     * @param regionEnd the end of the failed token
     */
    public TokenizationFailure(TokenizationFailureType type, int regionStart, int regionEnd) {
        super(type.getMessage());
        assert regionStart < regionEnd;
        this.type = type;
        this.regionStart = regionStart;
        this.regionEnd = regionEnd;
    }

    /**
     * @return the start index of the bad token
     */
    public int getRegionStart() {
        return regionStart;
    }

    /**
     * @return the stop index of the bad token
     */
    public int getRegionEnd() {
        return regionEnd;
    }

    /**
     * @return the type of failure when parsing a token
     */
    public TokenizationFailureType getType(){
        return type;
    }
}
