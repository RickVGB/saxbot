package nl.saxion.discord.bot.internal.smartinvoke.interpret;

/**
 * A class representing the result of type interpretation
 * @param <Type> the type of the value that is being interpreted as
 */
public class TypeInterpretationResult<Type> {
    /**
     * The result. This only applies to success results and may hold any value, even null.
     */
    private final Type result;
    /**
     * Whether this result is a success.
     * If this is {@code true}, the interpretation was successful and the {@link #result} field is applicable.
     */
    private final boolean success;

    /**
     * @return the result of a successful interpretation.
     * @apiNote This will return {@code null} on failure or if the interpretation successfully completed yielding {@code null}. Therefor it is recommended to use {@link #isSuccess()} before calling this method if the {@link TypeInterpreter type interpreter} may return null
     */
    public Type getResult(){
        return result;
    }

    /**
     * @return {@code true} if the interpretation was successful, {@code false} otherwise
     */
    public boolean isSuccess(){
        return success;
    }

    /**
     * Creates a new TypeInterpretationResult
     * @param result the result to yield
     * @param success {@code true} if the interpretation was successful
     */
    private TypeInterpretationResult(Type result, boolean success){
        this.result = result;
        this.success = success;
    }
    /**
     * A constant for the failure result, as the content of the failure result is always the same
     */
    private static final TypeInterpretationResult<?> FAILURE = new TypeInterpretationResult<>(null,false);

    /**
     * Returns a success result with the given result value
     * @param result the result of the interpretation
     * @param <Type> the type of the result
     * @return the success result with the given value
     */
    public static <Type> TypeInterpretationResult<Type> success(Type result){
        return new TypeInterpretationResult<>(result,true);
    }

    /**
     * Returns a failure result, letting the called know that conversion has failed
     * @param <Type> the type of the interpretation result.
     * @return a failure result
     */
    @SuppressWarnings("unchecked") // null result can be seen as any type, so no issues here
    public static <Type> TypeInterpretationResult<Type> failure(){
        return (TypeInterpretationResult<Type>) FAILURE;
    }
}
