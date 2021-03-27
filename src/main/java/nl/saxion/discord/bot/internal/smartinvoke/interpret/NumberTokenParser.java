package nl.saxion.discord.bot.internal.smartinvoke.interpret;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.Token;

import javax.annotation.RegEx;
import java.util.function.Function;
import java.util.regex.Pattern;

import static nl.saxion.discord.bot.internal.smartinvoke.interpret.TypeInterpretationResult.failure;
import static nl.saxion.discord.bot.internal.smartinvoke.interpret.TypeInterpretationResult.success;

/**
 * A TypeInterpreter that wraps parsing functions for numbers that are only expected to throw {@link NumberFormatException NumberFormatExceptions} when passed bad data
 * @param <Type> the type of the number the parser can parse
 */
class NumberTokenParser<Type extends Number> implements TypeInterpreter<Type>{
    /**
     * The parser to use to parse the number.
     * This may throw a {@link NumberFormatException} on conversion
     */
    private final Function<String,Type> parser;
    /**
     * A pattern to match to test the number without creating the number at the risk of a {@link NumberFormatException}
     */
    private final Pattern pattern;
    /**
     * Creates a NumberTokenParser
     * @param parser the parser that can parse the given types
     */
    public NumberTokenParser(Function<String, Type> parser, @RegEx String regex) {
        this.parser = parser;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public TypeInterpretationResult<Type> interpret(Message context, Token token){
        String raw = token.getRaw();
        if (!pattern.matcher(raw).matches()){
            return failure();
        }
        try{
            return success(parser.apply(token.getRaw()));
        }catch (NumberFormatException nfe){
            return failure();
        }
    }
}
