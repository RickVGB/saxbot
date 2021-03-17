package nl.saxion.discord.bot.commands;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.annotations.*;
import nl.saxion.discord.bot.internal.Command;

@Alias("delay")
@Cooldown(seconds=30)
public class Ping extends Command {

    public Ping() {
        super("ping");
    }
    // no support for simple types
    public void runRaw(Message message, String raw){
        long start = System.currentTimeMillis();
        message.getChannel().sendMessage("pong!").queue(ping -> {
            long took = System.currentTimeMillis() - start;
            ping.editMessage("pong! took "+took+" ms").queue();
        });
    }
}
