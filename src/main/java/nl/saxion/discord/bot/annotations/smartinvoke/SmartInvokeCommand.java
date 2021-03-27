package nl.saxion.discord.bot.annotations.smartinvoke;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SmartInvokeCommand {
    /**
     * supposed to tell the user what the command does.
     * @return the documentation of the command
     */
    String doc();
}
