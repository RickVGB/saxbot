package nl.saxion.discord.bot.internal.smartinvoke.interpret;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.SnowflakeMentionToken;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.Token;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static nl.saxion.discord.bot.internal.smartinvoke.interpret.TypeInterpretationResult.failure;
import static nl.saxion.discord.bot.internal.smartinvoke.interpret.TypeInterpretationResult.success;

/**
 * A TypeInterpreter that helps with finding parsed mentions
 * @param <Mention> type type of mention to target
 */
class SnowflakeMentionParser<Mention extends IMentionable> implements TypeInterpreter<Mention> {
    /**
     * The interpretationType this mention parser is for
     */
    private final Class<Mention> targetType;
    /**
     * A function that is used to get a collection of possible mentions
     */
    private final Function<Message,? extends Collection<Mention>> mentionTypeGetter;

    /**
     * Creates a new mention parser
     * @param targetType the mention type to target
     * @param mentionTypeGetter a function that returns all the applicable mentions used to find the mention represented in target tokens
     */
    public SnowflakeMentionParser(Class<Mention> targetType, Function<Message, ? extends Collection<Mention>> mentionTypeGetter) {
        this.targetType = targetType;
        this.mentionTypeGetter = mentionTypeGetter;
    }

    @Override
    public TypeInterpretationResult<Mention> interpret(Message context, Token token) {
        if (!(token instanceof SnowflakeMentionToken)){
            return failure();
        }
        Collection<Mention> mentions = mentionTypeGetter.apply(context);
        for (Mention mention : mentions){
            if (mention.getIdLong() == ((SnowflakeMentionToken) token).getId()){
                return success(mention);
            }
        }
        throw new NoSuchElementException("No "+targetType.getName()+" found in the message");
    }
}
