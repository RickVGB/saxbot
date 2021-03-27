package nl.saxion.discord.bot.internal.smartinvoke.interpret;

import net.dv8tion.jda.api.entities.*;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.SnowflakeMentionToken;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens.Token;

import javax.annotation.RegEx;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static nl.saxion.discord.bot.internal.smartinvoke.interpret.TypeInterpretationResult.*;
/**
 * An utility class containing functions that help with converting types
 */
public class InterpretationUtil {
    private InterpretationUtil(){} // no instances

    /**
     * A map containing all known data about conversion from a {@link Token token} to a given type.
     */
    private static final Map<Class<?>,InterpretationBundle<?>> bundles = new HashMap<>();

    /**
     * Checks whether a given argument type is supported
     * @param cls the target type to check support for
     * @return {@code true} if tokens can be parsed as the given type
     */
    public static boolean supports(Class<?> cls){
        return bundles.containsKey(cls);
    }

    /**
     * Fetches the bundle of a given type
     * @param type the type to get the bundle of
     * @return the InterpretationBundle associated with the given type
     * @throws NoSuchElementException if no conversion to the given type can be done
     */
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

    /**
     * Adds an interpreter of a given type
     * @param cls the class that can be interpreted as
     * @param interpreter the interpreter to use for interpretation
     * @param typeHint the type hint associated with the given class
     * @param <T> the type that can be interpreted as
     */
    private static <T> void addInterpreter(Class<T> cls, TypeInterpreter<T> interpreter, String typeHint){
        bundles.put(cls, new InterpretationBundle<T>(typeHint,interpreter));
    }

    /**
     * Adds an interpreter for a primitive type
     * @param primitive the primitive class of the primitive type. e.g. {@code int}
     * @param wrapper the wrapper class of the primitive type. e.g. {@link Integer}
     * @param interpreter the interpreter to use for interpretation
     * @param typeHint the hint to associate with the given type
     * @param <T> the type of the primitive
     */
    private static <T> void putPrimitiveInterpreter(Class<T> primitive,Class<T> wrapper,TypeInterpreter<T> interpreter, String typeHint){
        InterpretationBundle<T> bundle = new InterpretationBundle<>(typeHint, interpreter);
        bundles.put(primitive,bundle);
        bundles.put(wrapper  ,bundle);
    }

    static{// add interpretation bindings
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
}
