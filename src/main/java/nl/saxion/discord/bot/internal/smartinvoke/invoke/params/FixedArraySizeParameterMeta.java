package nl.saxion.discord.bot.internal.smartinvoke.invoke.params;

import nl.saxion.discord.bot.internal.smartinvoke.tokenize.TokenizationFailure;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.Tokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * A parameter meta for array parameters with a fixed size
 */
public class FixedArraySizeParameterMeta extends BaseArrayParameterMeta {
    /**
     * The fixed size of the array parameter
     */
    private final int size;

    /**
     * Creates a new fixed size array parameter meta
     * @param name the name of the parameter
     * @param arrayClass the class of the parameter. this must be an array class
     * @param size the size of the array
     */
    public FixedArraySizeParameterMeta(String name, Class<?> arrayClass, int size) {
        super(name, arrayClass,"["+size+"]");
        this.size = size;
    }

    @Override
    protected List<Token> getArrayTokens(Tokenizer tokenizer) throws TokenizationFailure {
        List<Token> tokens = new ArrayList<>();
        for (int i=0;i<size;++i){
            Token token;
            do{
                token = tokenizer.next();
            }while(token.isBlank());
            tokens.add(getNextNonWhitespaceToken(tokenizer));
        }
        return tokens;
    }
}
