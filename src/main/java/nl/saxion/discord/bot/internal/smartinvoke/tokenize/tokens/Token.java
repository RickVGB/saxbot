package nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens;

import nl.saxion.discord.bot.internal.smartinvoke.tokenize.Tokenizer;

import javax.annotation.Nonnull;

/**
 * A token returned by a {@link Tokenizer Tokenizer}
 */
public class Token {
    /**
     * The raw value of the token
     */
    private final String raw;

    /**
     * Creates a new token
     * @param raw the raw value of the token
     */
    public Token(@Nonnull String raw) {
        assert !raw.isEmpty();
        this.raw = raw;
    }

    /**
     * @return the raw content of the token
     */
    public final String getRaw() {
        return raw;
    }

    /**
     * @return {@code true} if the token only contains whitespace. This is shorthand for {@code token.getRaw().isBlank()}.
     * @see String#isBlank()
     */
    public boolean isBlank(){
        return raw.isBlank();
    }

    @Override
    public String toString() {
        return "Token:"+raw;
    }
}
