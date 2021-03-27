package nl.saxion.discord.bot.internal.smartinvoke.invoke;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.annotations.smartinvoke.SmartInvoke;
import nl.saxion.discord.bot.annotations.smartinvoke.SmartInvokeCommand;
import nl.saxion.discord.bot.internal.Command;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.OnDemandTokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.TokenizationFailure;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.Tokenizer;

import java.lang.reflect.*;
import java.util.*;

/**
 * An invoker that can invoke various {@link SmartInvoke SmartInvoke} methods of a {@link SmartInvokeCommand SmartInvoke command}.
 */
public final class SmartInvoker {
    /**
     * The annotation used to mark this command. This may contain
     * The annotation used to mark this command. This may contain configuration of the invoker itself
     */
    private final SmartInvokeCommand invokeMeta;
    /**
     * The command the invoker is based on. This is used to call the methods
     */
    private final Command command;
    /**
     * A list of handlers where each handler is bound to a method annotated with the {@link SmartInvoke SmartInvoke} annotation.
     * These are used to invoke the methods themselves
     */
    private final List<MethodHandler> methods;

    /**
     * Creates a new SmartInvoker
     * @param command the command object to call the method on
     */
    public SmartInvoker(Command command){
        this.command = command;
        this.invokeMeta = command.getClass().getAnnotation(SmartInvokeCommand.class);
        if (invokeMeta == null){
            throw new IllegalArgumentException("Command is not annotated as a SmartInvoke command");
        }
        this.methods = new ArrayList<>();
        for (Method method : command.getClass().getMethods()){
            if (method.getAnnotation(SmartInvoke.class) != null){
                methods.add(new MethodHandler(command,method));
            }
        }
        if (methods.size() == 0){
            throw new IllegalArgumentException("SmartInvoke commands must have at least one SmartInvoke method");
        }
        this.methods.sort(Comparator.comparingInt(MethodHandler::getPriority));
    }

    /**
     * invokes the command method
     * @param message the method
     * @param rawArgs the raw arguments
     */
    public void invoke(Message message, String rawArgs) {
        // diagnostics
        double highestProbability = -1d;
        MethodHandler mostProbableHandler = null;

        for (MethodHandler handler : methods){
            try{
                Tokenizer tokenizer = new OnDemandTokenizer(rawArgs,handler.getTokenizerFlags());
                InvocationFailure failure = handler.invoke(command,message,tokenizer);
                if (failure == null){
                    // success!
                    return;
                }else {
                    // no other failure found
                    double probability = failure.getProbability();
                    if (highestProbability < probability){
                        mostProbableHandler = handler;
                        highestProbability = probability;
                    }
                }
            } catch (TokenizationFailure tokenizationFailure) {
                message.getChannel()
                        .sendMessage("Your message is not formatted correctly. the issue seems to be: "+
                                tokenizationFailure.getType()+" between characters "+tokenizationFailure.getRegionStart()+" and "+tokenizationFailure.getRegionEnd()).queue();
            }
        }
        if (mostProbableHandler != null){
            // todo diagnose issues here
        }
    }
}
