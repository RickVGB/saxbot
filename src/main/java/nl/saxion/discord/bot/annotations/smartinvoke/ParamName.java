package nl.saxion.discord.bot.annotations.smartinvoke;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells {@link SmartInvoke} the name of the parameter, which is used for the help command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParamName {
    /**
     * @return The name of the parameter
     */
    String value();
}
