package nl.saxion.discord.bot.internal.smartinvoke.tokenize;

import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.MentionToken;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.SnowflakeMentionToken;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.Token;

import javax.annotation.Nonnull;

/**
 * An interface for tokenizers
 * This can be used to implement custom tokenizers
 */
public abstract class Tokenizer {
    /**
     * Returns the next token. after calling this, this token will not be returned anymore
     * @return the next token
     * @throws TokenizationFailure if the tokenizer cannot parse a given token
     */
    public abstract @Nonnull
    Token next() throws TokenizationFailure;

    /**
     * returns the rest of the input as one token
     * @return the token in question
     */
    public abstract @Nonnull
    Token remaining();

    /**
     * Marks the given area so it can be returned to later on. The meaning of the returned integer may vary between implementations.
     * Multiple marks may be set on one Tokenizer at a time.
     * Tokenizer implementations are allowed to discard marks if an earlier mark was restored.
     * @return the integer used to restore the current state of the Tokenizer using the {@link #restore(int) restore} method
     */
    public abstract int mark();

    /**
     * Restores the Tokenizer to the state it was on the moment {@link #mark() mark} was called.
     * @param mark the mark created by the {@link #mark() mark} method.
     *             This may destroy all other marks created after the restored mark.
     */
    public abstract void restore(int mark);

    /**
     * @return {@code true} if the tokenizer has more tokens
     */
    public abstract boolean hasNext();

    /**
     * The flags given to the parser. This sets the rules the parser will follow and can be used to turn on/off features.
     * All flags are given using constants with names starting with {@code FLAG_}. detail about every flag is written in their own javadoc
     */
    private final int flags;

    /**
     * Creates a new tokenizer
     * @param flags the flags of the tokenizer
     */
    public Tokenizer(int flags){
        this.flags = flags;
    }

    /**
     * checks if the given flag is present
     * @param flag the flag of the tokenizer
     * @return {@code true} if the flag set, {@code false} if not
     */
    protected final boolean hasFlag(int flag){
        return hasFlag(flags,flag);
    }

    /**
     * Checks if the given flag is present on the flags.
     * @param flags the flags to get the flag from
     * @param flag the flag to check
     * @return
     */
    public static boolean hasFlag(int flags, int flag){
        return (flags & flag) != 0;
    }

    /**
     * ensures the Tokenizer flags are set
     * @param flags the flags to set
     */
    public static void ensureValidFlags(int flags){
        // check if both FLAG_PURE_TEXT and FLAG_IGNORE_WHITESPACE are set, which is do not make sense together
        checkInvalidCombination(flags,FLAG_PURE_TEXT | FLAG_IGNORE_WHITESPACE,"Cannot ignore whitespace if pure text is on");
        checkInvalidCombination(flags,FLAG_PURE_TEXT | FLAG_USE_QUOTES,"Cannot use quotes if pure text is on");
        checkDependentFlags(flags,FLAG_USE_QUOTES,FLAG_STRICT_QUOTES,"Cannot use strict quotes if quotes are not used");
    }

    /**
     * Checks whether the given flag combination does not appear
     * @param flags the flags that are received
     * @param illegalCombination the illegal combination of flags
     * @param message the message to put in the {@link IllegalArgumentException} if thrown
     * @throws IllegalArgumentException if all flags on the illegal combination are set in the flags.
     */
    private static void checkInvalidCombination(int flags, int illegalCombination, String message){
        if (Integer.bitCount(flags & illegalCombination) == Integer.bitCount(illegalCombination)){
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks whether the flag the depender flag depends on is set
     * @param flags the flags to check
     * @param dependee the flag being depended on by the depender
     * @param depender the flag that depends on the dependee
     * @param message the message to put in the {@link IllegalArgumentException} if thrown
     * @throws IllegalArgumentException if the depender flag is set and the dependee flag is not set.
     */
    private static void checkDependentFlags(int flags, int dependee, int depender, String message){
        if ((flags & depender) != 0 // depender is set?
                && (flags & dependee) == 0){ // dependee is not set?
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * If this flag is on, the tokenizer will ignore spaces between quoted sections and until it finds a whitespace or EOF after a quote.
     * This will still accept improperly closed quotes, which can be disabled by using {@link #FLAG_STRICT_QUOTES}
     */
    public static final int FLAG_USE_QUOTES                = 1; // allow quotes
    /**
     * If this flag is on, any quoted sections that are parsed must be properly closed. This only takes effect if {@link #FLAG_USE_QUOTES} is set
     */
    public static final int FLAG_STRICT_QUOTES             = 1 << 1; // fail on unbalanced quotation marks
    /**
     * If this flag is on, the parser will parse plain {@link SnowflakeMentionToken snowflake mentions} as separate tokens
     */
    public static final int FLAG_SPLIT_MENTION_SNOWFLAKE   = 1 << 2; // mentions are regarded as their own tokens
    /**
     * If this flag is on, the parser will parse plain appearances of '@everyone' as {@link MentionToken#EVERYONE} {@link MentionToken MentionTokens}
     */
    public static final int FLAG_SPLIT_MENTION_EVERYONE    = 1 << 3; // treat @everyone as literal text
    /**
     * If this flag is on, the parser will parse plain appearances of '@here' as {@link MentionToken#HERE} {@link MentionToken MentionTokens}
     */
    public static final int FLAG_SPLIT_MENTION_HERE        = 1 << 4; // treat @here as literal text
    /**
     * If this flag is on, whitespace will be ignored when parsing
     */
    public static final int FLAG_IGNORE_WHITESPACE         = 1 << 5; // skip over the whitespace of other tokens

    /**
     * If this flag is set, all text will be parsed as pure, with only mention tokens between simple tokens.
     */
    public static final int FLAG_PURE_TEXT                 = 1 << 6; // only take out mentions if any
    /**
     * A configuration of all flags that add mention type parsing
     */
    public static final int FLAG_SPLIT_MENTIONS = FLAG_SPLIT_MENTION_SNOWFLAKE | FLAG_SPLIT_MENTION_EVERYONE | FLAG_SPLIT_MENTION_HERE;
    /**
     * The recommended flag set for the standard parsers
     */
    public static final int RECOMMENDED_FLAGS = FLAG_USE_QUOTES | FLAG_STRICT_QUOTES | FLAG_SPLIT_MENTIONS;
}
