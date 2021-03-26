package nl.saxion.discord.bot.internal.smartinvoke.tokenizer;

public class TypeInterpretationResult<Type> {
    private Type result;
    private boolean success;

    public Type getResult(){
        return result;
    }

    public boolean isSuccess(){
        return success;
    }

    private TypeInterpretationResult(Type result, boolean success){
        this.result = result;
        this.success = success;
    }

    private static final TypeInterpretationResult<?> FAILURE = new TypeInterpretationResult<>(null,false);

    public static <Type> TypeInterpretationResult<Type> success(Type result){
        return new TypeInterpretationResult<>(result,true);
    }

    @SuppressWarnings("unchecked") // null result can be seen as any type, so no issues here
    public static <Type> TypeInterpretationResult<Type> failure(){
        return (TypeInterpretationResult<Type>) FAILURE;
    }
}
