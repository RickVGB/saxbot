package nl.saxion.discord.bot.internal.smartinvoke.interpret;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.Token;

/**
 * An interface whose instances can be used to parse a token as a given type
 * @param <Type> the parsed type of this interpreter
 */
public interface TypeInterpreter<Type> {
    /**
     * interprets the given token as if from the given type
     * @param context the context of the message which may be used as context to resolve data
     * @param token the token to interpret
     * @return the interpreted data
     */
    TypeInterpretationResult<Type> interpret(Message context, Token token);
}
