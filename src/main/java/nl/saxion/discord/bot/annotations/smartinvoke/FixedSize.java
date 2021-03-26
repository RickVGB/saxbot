package nl.saxion.discord.bot.annotations.smartinvoke;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that allows SmartInvoke parameters to use arrays as arguments, by fixing the size of the array to parse.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface FixedSize {
    int value();
}
