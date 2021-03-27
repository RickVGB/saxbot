package nl.saxion.discord.bot.commands;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.annotations.smartinvoke.ParamName;
import nl.saxion.discord.bot.annotations.smartinvoke.SmartInvoke;
import nl.saxion.discord.bot.annotations.smartinvoke.SmartInvokeCommand;
import nl.saxion.discord.bot.internal.Command;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple command that shows the power of smart commands
 */
@SmartInvokeCommand(doc="rolls a die one time")
public class RollDie extends Command {
    public RollDie() {
        super("roll");
    }

    private static final int MAX_DICE  = 100;
    private static final int MAX_SIDES = 1000;

    private static final Random RNG = new Random();

    @SmartInvoke(invokeDoc = "rolls a die")
    public void roll(Message message, @ParamName("dice") int dice, @ParamName("sides") int sides){
        if (sides < 0 || dice < 0){
            message.getChannel().sendMessage("The dice collapse in on themselves\nTotal: ???").queue();
            return;
        }
        if (sides == 0){
            message.getChannel().sendMessage("the dice fall through the floor.\nTotal: no").queue();
            return;
        }
        if (dice > MAX_DICE){
            message.getChannel().sendMessage("You cannot throw that many dice").queue();
            return;
        }
        if (sides > MAX_SIDES){
            message.getChannel().sendMessage("The dice have become too round to roll").queue();
            return;
        }
        int total = 0;
        int[] results = new int[dice];
        for (int i=0;i<dice;++i){
            int roll = RNG.nextInt(sides)+1;
            results[i] = roll;
            total += roll;
        }
        StringBuilder builder = new StringBuilder("rolled: ");
        if (dice != 0){
            builder.append(results[0]);
            for (int i=1;i<dice;++i){
                builder.append(", ").append(results[i]);
            }
        }
        builder.append("\nTotal: ").append(total);
        message.getChannel().sendMessage(builder).queue();
    }

    @SmartInvoke(invokeDoc = "rolls a number of dice")
    public void roll(Message message, @ParamName("dice") int dice){
        roll(message, dice,6);
    }

    private static final Pattern DNDPattern = Pattern.compile("(\\d+)d(\\d+)");

    @SmartInvoke(invokeDoc = "roll dice DND style (like 1d20)")
    public void roll(Message message, @ParamName("roll") String dnd){
        Matcher matcher = DNDPattern.matcher(dnd);
        if (!matcher.matches()){
            message.getChannel().sendMessage("Invalid roll. try something like 2d6 to roll 2 6 sided die").queue();
        }
        int dice = Integer.parseInt(matcher.group(1));
        int sides = Integer.parseInt(matcher.group(2));
        roll(message,dice,sides);
    }

    @SmartInvoke(invokeDoc = "rolls one die")
    public void roll(Message message){
        roll(message,1);
    }
}
