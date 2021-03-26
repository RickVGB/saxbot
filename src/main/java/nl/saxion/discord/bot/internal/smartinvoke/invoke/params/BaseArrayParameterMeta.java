package nl.saxion.discord.bot.internal.smartinvoke.invoke.params;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.internal.smartinvoke.InterpretationUtil;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TokenizationFailure;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.Tokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TypeInterpretationResult;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.tokens.Token;

import java.util.List;

/**
 * A base parameter meta for array parameters. this may consume multiple tokens to create one parameter
 */
public abstract class BaseArrayParameterMeta extends ParameterMeta {
    /**
     * The class of the parameter
     */
    private final Class<?> arrayClass;

    /**
     * Creates a base array parameter meta
     * @param name the name of the parameter
     * @param arrayClass the type of the parameter. This must be an array class
     */
    public BaseArrayParameterMeta(String name, Class<?> arrayClass, String suffix) {
        super(name, (InterpretationUtil.getTypeHint(ensureArray(arrayClass).getComponentType())+suffix).intern());
        this.arrayClass = arrayClass;
    }

    /**
     * ensures the given class is an array
     * @param arrayClass the array to check if it is an array
     * @return the arrayClass parameter
     */
    private static Class<?> ensureArray(Class<?> arrayClass){
        if (!arrayClass.isArray()){
            throw new IllegalArgumentException("arrayClass is not an array");
        }
        return arrayClass;
    }

    @Override
    public TypeInterpretationResult<?> transform(Message context, Tokenizer tokenizer) throws TokenizationFailure {
        return InterpretationUtil.interpret(context,arrayClass,getArrayTokens(tokenizer));
    }

    /**
     * should return the tokens used to iterpret the items in the array
     * @param tokenizer the tokenizer to use to obtain the array tokens
     * @return a list of tokens used to parse the array parameters
     * @throws TokenizationFailure If the tokenizer runs into an issue while tokenizing
     */
    protected abstract List<Token> getArrayTokens(Tokenizer tokenizer) throws TokenizationFailure;
}
