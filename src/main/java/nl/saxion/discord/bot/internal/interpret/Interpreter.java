package nl.saxion.discord.bot.internal.interpret;

public interface Interpreter<T> {
    T interpret(String raw);
}
