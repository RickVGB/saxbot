package nl.saxion.discord.bot.annotations.smartinvoke;

import nl.saxion.discord.bot.internal.smartinvoke.invoke.SmartInvoker;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.Tokenizer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SmartInvoke {
    /**
     * Tells SmartInvoke to regard the last parameter as a raw {@link String} type.
     * With this, the parameter will contain the text after the other parameters without further parsing.
     * @return {@code true} if the last parameter is to be filled using raw text.
     */
    boolean lastArgRaw() default false;

    /**
     * The priority of the interface.
     * This can be used to tell the {@link SmartInvoker SmartInvoker} to process the given method first.
     * The default priority is 0, but it is recommended to set the priorities of methods that resemble eachother
     * This does not affect the error message if applicable. a higher priority means it will be run only once
     * @return the priority of the method
     */
    int priority() default 0;

    /**
     * shows the invoke documentation of the command. This is used to display help information.
     * @return the invokeDoc of the command
     */
    String invokeDoc();
    /**
     * The flags to be used by the Tokenizer.
     * The default is most useful for most commands, but there may be a need
     * to change these settings on custom commands.
     * @return the flags for the {@link Tokenizer}
     */
    int tokenizerFlags() default Tokenizer.RECOMMENDED_FLAGS;
}
