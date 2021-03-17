package nl.saxion.discord.bot.internal.interpret;

public class InterpretationFailure extends RuntimeException {
    public InterpretationFailure(String message) {
        super(message);
    }

    public InterpretationFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
