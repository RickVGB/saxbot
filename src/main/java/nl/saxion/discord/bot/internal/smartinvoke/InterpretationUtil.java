package nl.saxion.discord.bot.internal.smartinvoke;

import net.dv8tion.jda.api.entities.*;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TypeInterpretationResult;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TypeInterpreter;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.tokens.SnowflakeMentionToken;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.tokens.Token;

import javax.annotation.RegEx;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TypeInterpretationResult.*;
/**
 * An utility class containing functions that help with converting types
 */
public class InterpretationUtil {
    private InterpretationUtil(){} // no instances

    private static final Map<Class<?>,InterpretationBundle<?>> bundles = new HashMap<>();

    /**
     * Checks whether a given argument type is supported
     * @param cls the target type to check support for
     * @return {@code true} if tokens can be parsed as the given type
     */
    public static boolean supports(Class<?> cls){
        return bundles.containsKey(cls);
    }

    private static InterpretationBundle<?> getBundle(Class<?> type){
        InterpretationBundle<?> bundle = bundles.get(type);
        if (bundle == null){
            throw new NoSuchElementException("Cannot interpret as "+type);
        }else {
            return bundle;
        }
    }

    /**
     * returns the type hint of the target type. This bundle contains data about interpretation
     * @param targetType the type to target
     * @return the hint of the type to target
     */
    public static String getTypeHint(Class<?> targetType){
        return getBundle(targetType).typeHint;
    }

    /**
     * returns the interpreter associated with the given class
     * @param type the target type
     * @return the interpreter associated with the given class
     * @throws NoSuchElementException if there is no interpreter associated with the given class
     */
    public static TypeInterpreter<?> getInterpreter(Class<?> type){
        return getBundle(type).interpreter;
    }

    /**
     * interprets the given tokens as the given type
     * @param context the message to extract the context from
     * @param type the type of the argument to turn the tokens into
     * @param tokens the list of tokens that can be provided
     * @return the object
     */
    public static TypeInterpretationResult<?> interpret(Message context, Class<?> type, List<Token> tokens) {
        if (!type.isArray()){
            if (tokens.size() != 1){
                throw new IllegalArgumentException("Non-array types may only have a count of 1");
            }
            return getInterpreter(type).interpret(context, tokens.get(0));
        }else{
            // argument is array, get element type
            Class<?> component = type.getComponentType();
            // locate interpreter and create an array of the type
            TypeInterpreter<?> interpreter = getInterpreter(component);
            Object array = Array.newInstance(component, tokens.size());
            // fill array with values
            for (int i = 0; i < tokens.size(); ++i) {
                TypeInterpretationResult<?> value = interpreter.interpret(context, tokens.get(i));
                if (value.isSuccess()) {
                    Array.set(array, i, value.getResult());
                }else{
                    return failure();
                }
            }
            return success(array);
        }
    }

    /*
            INTERPRETERS BELOW
     */

    private static <T> void addInterpreter(Class<T> cls, TypeInterpreter<T> interpreter, String typeHint){
        bundles.put(cls, new InterpretationBundle<T>(typeHint,interpreter));
    }

    private static <T> void putPrimitiveInterpreter(Class<T> primitive,Class<T> wrapper,TypeInterpreter<T> interpreter, String typeHint){
        InterpretationBundle<T> bundle = new InterpretationBundle<>(typeHint, interpreter);
        bundles.put(primitive,bundle);
        bundles.put(wrapper  ,bundle);
    }

    static{
        // primitives
        putPrimitiveInterpreter(int   .class,Integer.class, new NumberTokenParser<>(Integer::parseInt   ,"-?\\d{1,10}"),"integer");
        putPrimitiveInterpreter(double.class,Double .class, new NumberTokenParser<>(Double ::parseDouble,"-?\\d+(\\.\\d+)?"),"number");
        addInterpreter(BigInteger.class, new NumberTokenParser<>(BigInteger::new,"-?\\d+"),"integer");
        // text
        addInterpreter(String.class, (ctx,token)-> success(token.getRaw()),"text");
        // mentions
        addInterpreter(IMentionable.class, new SnowflakeMentionParser<>(IMentionable.class,m -> m.getMentions(Message.MentionType.values())),"mention");
        // items in a bag are unique so it is likely faster in most cases
        addInterpreter(Role        .class, new SnowflakeMentionParser<>(Role        .class,Message::getMentionedRolesBag   ),"role");
        addInterpreter(User        .class, new SnowflakeMentionParser<>(User        .class,Message::getMentionedUsersBag   ),"user");
        addInterpreter(Member      .class, new SnowflakeMentionParser<>(Member      .class,Message::getMentionedMembers    ),"user");
        addInterpreter(TextChannel .class, new SnowflakeMentionParser<>(TextChannel .class,Message::getMentionedChannelsBag),"channel");
        addInterpreter(Emote       .class, new SnowflakeMentionParser<>(Emote       .class,Message::getEmotesBag           ),"emote");
    }

    /**
     * A TypeInterpreter that wraps parsing functions for numbers that are only expected to throw {@link NumberFormatException NumberFormatExceptions} when passed bad data
     * @param <Type> the type of the number the parser can parse
     */
    private static class NumberTokenParser<Type extends Number> implements TypeInterpreter<Type>{
        private final Function<String,Type> parser;
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

    /**
     * A TypeInterpreter that helps with finding parsed mentions
     * @param <Mention>
     */
    private static class SnowflakeMentionParser<Mention extends IMentionable> implements TypeInterpreter<Mention> {
        private final Class<Mention> targetType;
        private final Function<Message,? extends Collection<Mention>> mentionTypeGetter;

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

    /**
     * A bundle of data about supported types
     * @param <Type> the type of the data supported
     */
    private static final class InterpretationBundle<Type> {
        private final String typeHint;
        private final TypeInterpreter<Type> interpreter;

        public InterpretationBundle(String typeHint, TypeInterpreter<Type> interpreter) {
            this.typeHint = Objects.requireNonNull(typeHint);
            this.interpreter = Objects.requireNonNull(interpreter);
        }
    }
}
