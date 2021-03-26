package nl.saxion.discord.bot.internal.smartinvoke.invoke.params;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.Tokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TypeInterpretationResult;

/**
 * A parameter meta for raw strings.
 * This will completely consume the remaining tokens and therefore cannot be followed by other parameter metas
 */
public class RawParameterMeta extends ParameterMeta{
    public RawParameterMeta(String name) {
        super(name, "raw text");
    }

    @Override
    public TypeInterpretationResult transform(Message context, Tokenizer tokenizer){
        return TypeInterpretationResult.success(tokenizer.remaining().getRaw().strip());
    }
}
