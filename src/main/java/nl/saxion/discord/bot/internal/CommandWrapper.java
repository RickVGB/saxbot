package nl.saxion.discord.bot.internal;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.annotations.smartinvoke.SmartInvoke;
import nl.saxion.discord.bot.annotations.smartinvoke.SmartInvokeCommand;
import nl.saxion.discord.bot.internal.smartinvoke.invoke.SmartInvoker;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TokenizationFailure;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A thin wrapper that allows for commands to be called faster
 */
public class CommandWrapper {

    private final Command command;
    private final SmartInvoker smartInvoker;

    public CommandWrapper(Command command){
        this.command = Objects.requireNonNull(command);
        Method[] methods = command.getClass().getMethods();
        // add SmartInvoker if command is annotated with SmartInvokeCommand
        if (command.getClass().getAnnotation(SmartInvokeCommand.class) != null) {
            smartInvoker = new SmartInvoker(command);
        }else{
            smartInvoker = null;
        }
    }

    public Command getCommand() {
        return command;
    }

    public void invoke(Message message, String rawArgs){
        command.runRaw(message, rawArgs);

        String[] args = extractArgs(rawArgs);
        command.run(message, args);
        if (smartInvoker != null) {
            smartInvoker.invoke(message, rawArgs);
        }
    }

    private String[] extractArgs(String rawArgs) {
        List<String> args = new ArrayList<>();

        String[] parts = rawArgs.split(" ");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if (part.startsWith("\"")) {
                part = part.substring(1);

                List<String> subParts = new ArrayList<>();

                while (true) {
                    if (part.endsWith("\"")) {
                        subParts.add(part.substring(0, part.length() - 1));
                        break;
                    }
                    else {
                        subParts.add(part);

                        if (i + 1 > parts.length) {
                            break;
                        }
                        else {
                            i++;
                            part = parts[i];
                        }
                    }
                }

                args.add(String.join(" ", subParts));
            }
            else {
                args.add(part);
            }
        }

        return args.toArray(new String[0]);
    }
}
