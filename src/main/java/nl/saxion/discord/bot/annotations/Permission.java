package nl.saxion.discord.bot.annotations;

import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import nl.saxion.discord.bot.Bot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Permission {
    int value();

    int ADMINISTRATOR  = 0;
    int DEVELOPER      = 1;
}
