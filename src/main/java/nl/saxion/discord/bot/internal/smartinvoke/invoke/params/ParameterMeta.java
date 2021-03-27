package nl.saxion.discord.bot.internal.smartinvoke.invoke.params;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.TokenizationFailure;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.Tokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.interpret.TypeInterpretationResult;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.Token;

import javax.annotation.CheckReturnValue;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Information about a given parameter an how to handle it.
 * This is used for operations concerning individual SmartInvoke parameters
 */
public abstract class ParameterMeta {
    /**
     * The name of the parameter
     */
    private final String name;
    /**
     * A type hint about the parameter. this may be used for help commands
     */
    private final String typeHint;

    public ParameterMeta(String name, String typeHint) {
        this.name     = Objects.requireNonNull(name    );
        this.typeHint = Objects.requireNonNull(typeHint);
    }

    /**
     * @return the name of the parameter meta
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type hint of the parameter meta
     */
    public String getTypeHint() {
        return typeHint;
    }

    /**
     * returns the next non-whitespace token from the tokenizer
     * @param tokenizer the tokenizer to get tokens from
     * @return the next non-whitespace token
     * @throws TokenizationFailure if the tokenizer cannot parse the next token
     * @throws NoSuchElementException if the tokenizer runs out of tokens
     */
    protected static @CheckReturnValue
    Token getNextNonWhitespaceToken(Tokenizer tokenizer) throws TokenizationFailure {
        Token token;
        do{
            token = tokenizer.next();
        }while(token.isBlank());
        return token;
    }

    /**
     * Transforms a set of tokens from the tokenizer to the
     * @param context the message to use as context for transformations
     * @param tokenizer the tokenizer that provides the tokens
     * @return the object that was created
     * @throws TokenizationFailure if the token could not be parsed
     */
    public abstract TypeInterpretationResult<?> transform(Message context, Tokenizer tokenizer) throws TokenizationFailure;
}
