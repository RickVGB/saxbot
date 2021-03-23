package nl.saxion.discord.bot.internal;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.saxion.discord.bot.Bot;
import nl.saxion.discord.bot.annotations.Alias;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseBot extends ListenerAdapter {
    private final Map<String, CommandWrapper> commandLookup = new HashMap<>();

    private void addCommand(Command command){
        // create a wrapper for the command
        CommandWrapper wrapper = new CommandWrapper(command);
        // register command
        registerCommand(command.getName(),wrapper);
        // check for alias
        Alias alias = command.getClass().getAnnotation(Alias.class);
        if (alias != null){
            // register all aliases
            for (String commandAlias : alias.value()){
                registerCommand(commandAlias,wrapper);
            }
        }
    }

    private void registerCommand(String name, CommandWrapper wrapper){
        name = name.toLowerCase(Locale.ROOT); //ensure name is lowercase
        CommandWrapper existing = commandLookup.get(name);
        if (existing != null){
            throw new IllegalStateException("Command name or alias shared between "+existing.getCommand().getClass()+" and "+wrapper.getCommand().getClass());
        }
        // add the command to the lookup
        commandLookup.put(name,wrapper);
    }

    private void collectCommands(){
        // find all commands in the commands package relative from the current bot class package
        new Reflections(getClass().getPackageName()+".commands")
                .getSubTypesOf(Command.class)
                .forEach( cls ->{
                    if (Modifier.isAbstract(cls.getModifiers())) {
                        return; // ignore abstract classes
                    }
                    try {
                        Constructor<? extends Command> constructor = cls.getConstructor();
                        Command command = constructor.newInstance();
                        addCommand(command);
                    }catch (NoSuchMethodException nsm){
                        throw new RuntimeException("missing zero-argument constructor on command "+cls.getName());
                    }catch (InvocationTargetException ite) {
                        throw new RuntimeException("Cannot instantiate Command " + cls.getName(), ite.getTargetException());
                    }catch (IllegalAccessException iae){
                        throw new RuntimeException("Cannot instanciate Command "+cls.getName()+": constructor is not public");
                    }catch (ReflectiveOperationException roe){
                        throw new RuntimeException("Cannot instantiate Command "+cls.getName(),roe);
                    }
                });
    }

    public BaseBot(){
        collectCommands();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        processCommands(event.getMessage());
    }

    public abstract String getPrefix(Guild guild);

    public void processCommands(Message message){
        if (message.getAuthor().isBot()){
            return;// ignore bots
        }
        String prefix = getPrefix(message.isFromGuild()?message.getGuild():null);
        String raw = message.getContentRaw();
        if (raw.startsWith(prefix)) {
            // Remove prefix from raw message
            int prefixLength = prefix.length();
            raw = raw.substring(prefixLength);

            if (raw.isBlank()) {
                // Is invalid command, ignore it
                return;
            }

            // Get parts from the raw message
            String[] messageParts = raw.split(" ", 2);

            // Get command name
            String commandName = messageParts[0];

            // get args
            String rawArgs = "";
            if (messageParts.length > 1) {
                rawArgs = messageParts[1];
            }

            // find command
            CommandWrapper command = commandLookup.get(commandName.toLowerCase());
            if (command != null) {
                // invoke command
                command.invoke(message, rawArgs);
            } else {
                // no such command
                onInvalidCommand(message, commandName);
            }
        }
    }

    public void onInvalidCommand(Message message, String commandName) {
        // TODO: Add error message?
    };
}
