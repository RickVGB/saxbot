package nl.saxion.discord.bot.internal.smartinvoke.interpret;

import java.util.Objects;

/**
 * A bundle of data about supported types
 * @param <Type> the type of the data supported
 */
final class InterpretationBundle<Type> {
    /**
     * The hint that may be used to explain a type to a user
     */
    final String typeHint;
    /**
     * The interpreter to use to interpret tokens as the type this bundle is for
     */
    final TypeInterpreter<Type> interpreter;

    /**
     * Creates a new interpretationBundle
     * @param typeHint the hint to give about the given type
     * @param interpreter the interpreter used to interpret as the given type
     */
    public InterpretationBundle(String typeHint, TypeInterpreter<Type> interpreter) {
        this.typeHint = Objects.requireNonNull(typeHint);
        this.interpreter = Objects.requireNonNull(interpreter);
    }
}
