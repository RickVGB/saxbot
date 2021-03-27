package nl.saxion.discord.bot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the aliases that can be used to also invoke the command.
 * For example, the `play` command may have an alias of `p` or `start`
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Alias {
    /**
     * @return An array of all alternative names of this command
     */
    String[] value();
}
