package nl.saxion.discord.bot.internal.smartinvoke.invoke.params;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.internal.smartinvoke.interpret.InterpretationUtil;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.TokenizationFailure;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.Tokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.interpret.TypeInterpretationResult;
import nl.saxion.discord.bot.internal.smartinvoke.interpret.TypeInterpreter;

/**
 * A parameter meta for simple parameters. This is the most common parameter meta
 */
public class SimpleParameterMeta extends ParameterMeta{
    /**
     * The interpreter to use to transform the data
     */
    private final TypeInterpreter<?> interpreter;

    /**
     * creates the simple parameter meta
     * @param name the name of the parameter
     * @param targetClass the type of the parameter
     */
    public SimpleParameterMeta(String name, Class<?> targetClass) {
        super(name, InterpretationUtil.getTypeHint(targetClass));
        this.interpreter = InterpretationUtil.getInterpreter(targetClass);
    }

    @Override
    public TypeInterpretationResult<?> transform(Message message, Tokenizer tokenizer) throws TokenizationFailure {
        return interpreter.interpret(message,getNextNonWhitespaceToken(tokenizer));
    }
}
