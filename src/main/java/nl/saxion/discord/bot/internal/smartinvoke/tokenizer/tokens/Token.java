package nl.saxion.discord.bot.internal.smartinvoke.tokenizer.tokens;

import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.Tokenizer;

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

    public boolean isBlank(){
        return raw.isBlank();
    }

    @Override
    public String toString() {
        return "Token:"+raw;
    }
}
