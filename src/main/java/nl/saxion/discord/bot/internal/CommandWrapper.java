package nl.saxion.discord.bot.internal;

import net.dv8tion.jda.api.entities.Message;

import java.util.Objects;

/**
 * A thin wrapper that allows for commands to be called faster
 */
public class CommandWrapper {

    private Command command;

    public CommandWrapper(Command command){
        this.command = Objects.requireNonNull(command);
    }

    public Command getCommand() {
        return command;
    }

    void invoke(Message message, String rawArgs){
        command.runRaw(message, rawArgs);
    }
}
