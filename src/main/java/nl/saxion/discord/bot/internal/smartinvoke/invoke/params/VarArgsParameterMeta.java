package nl.saxion.discord.bot.internal.smartinvoke.invoke.params;

import nl.saxion.discord.bot.internal.smartinvoke.tokenize.TokenizationFailure;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.Tokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * A parameter meta that consumes the remaining tokens to form the varargs argument.
 * This will completely consume the remaining tokens and therefore cannot be followed by other parameter metas
 */
public class VarArgsParameterMeta extends BaseArrayParameterMeta {
    /**
     * Creates a new varargs parameter meta
     * @param name the name of the parameter
     * @param arrayClass the class of the parameter, this must be an array class
     */
    public VarArgsParameterMeta(String name, Class<?> arrayClass) {
        super(name, arrayClass,"...");
    }

    @Override
    protected List<Token> getArrayTokens(Tokenizer tokenizer) throws TokenizationFailure {
        List<Token> tokens = new ArrayList<>();
        while(tokenizer.hasNext()){
            Token token = tokenizer.next();
            if (!token.isBlank()){
                tokens.add(token);
            }
        }
        return tokens;
    }
}
