package nl.saxion.discord.bot.internal;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Command {
    public Command(@NotNull String name){
        this.name = Objects.requireNonNull(name);
    }

    private final String name;

    public String getName(){
        return name;
    }

    public void runRaw(Message message, String rawArgs) {}

    public void run(Message message, String[] args) {}
}
