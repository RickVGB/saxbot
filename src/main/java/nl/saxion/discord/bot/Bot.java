package nl.saxion.discord.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.saxion.discord.bot.internal.BaseBot;
import nl.saxion.discord.bot.internal.Command;
import org.reflections.Reflections;

import javax.security.auth.login.LoginException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class Bot extends BaseBot {

    @Override
    public String getPrefix(Guild guild) {
        return "$";
    }

    public static void main(String[] args) throws LoginException {
        // Get token from env variable
        String token = System.getenv("DISCORD_TOKEN");

        if (token.isBlank()) {
            throw new RuntimeException("Token not filled in");
        }

        JDA jda = JDABuilder.createDefault(token).build();
        jda.addEventListener(new Bot());
    }
}
