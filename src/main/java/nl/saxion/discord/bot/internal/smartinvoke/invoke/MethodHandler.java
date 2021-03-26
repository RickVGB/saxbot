package nl.saxion.discord.bot.internal.smartinvoke.invoke;

import net.dv8tion.jda.api.entities.Message;
import nl.saxion.discord.bot.annotations.smartinvoke.FixedSize;
import nl.saxion.discord.bot.annotations.smartinvoke.ParamName;
import nl.saxion.discord.bot.annotations.smartinvoke.SmartInvoke;
import nl.saxion.discord.bot.internal.Command;
import nl.saxion.discord.bot.internal.smartinvoke.InterpretationUtil;
import nl.saxion.discord.bot.internal.smartinvoke.invoke.params.*;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TokenizationFailure;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.Tokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.TypeInterpretationResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A class that wraps around a {@link SmartInvoke command}
 */
final class MethodHandler {
    private final SmartInvoke methodMeta;
    private final Method method;
    private final ParameterMeta[] paramMetas;

    MethodHandler(Command command, Method method) {
        if (method.getReturnType() != void.class){
            throw new IllegalArgumentException("method "+method+" does not return void, which is required for smartCommands");
        }
        this.paramMetas = new ParameterMeta[method.getParameterCount()-1];
        this.method = method;
        this.methodMeta = Objects.requireNonNull(method.getAnnotation(SmartInvoke.class));
        prepareInvocationMeta(command);
        if (!Modifier.isPublic(method.getModifiers())){
            throw new IllegalStateException("Smart Invocation is can only be applied to public methods");
        }
        if (Modifier.isStatic(method.getModifiers())){
            throw new IllegalStateException("Smart Invocation cannot be applied to static methods");
        }
        // ensure most exception handling is done in command
        for (Class<?> throwable : method.getExceptionTypes()){
            if (!RuntimeException.class.isAssignableFrom(throwable)){
                throw new IllegalStateException("Smart Invocation methods may not throw non-runtime exceptions");
            }
        }
    }

    /**
     * @return the priority of the commands
     */
    int getPriority(){
        return methodMeta.priority();
    }

    int getTokenizerFlags(){
        return this.methodMeta.tokenizerFlags();
    }

    /**
     * returns the name of the given parameter as given by the {@link ParamName} annotation.
     * @param param the parameter to check
     * @return the name of the parameter
     */
    private static String getParameterName(Parameter param){
        ParamName paramName = param.getAnnotation(ParamName.class);
        if (paramName != null){
            return paramName.value();
        }else{
            // todo: maybe a warning or exception here? since the parameter names may be removed by the compiler
            return param.getName();
        }
    }

    /**
     * invokes the method this handler handles
     * @param command the command to invoke
     * @param message the message that invoked the command
     * @param tokenizer the tokenizer to use to parse the arguments
     * @return the failure that happened if any. If the command was successfully invoked, {@code null} is returned.
     * @throws TokenizationFailure if the tokenizer runs into an issue while creating tokens
     */
    public InvocationFailure invoke(Command command, Message message, Tokenizer tokenizer) throws TokenizationFailure {
        // assert that the method is from the command class
        assert command.getClass().isAssignableFrom(method.getDeclaringClass());
        // create arguments
        Object[] params = new Object[paramMetas.length+1];
        // set message as first argument
        params[0] = message;
        // prepare custom parameters
        for(int i = 0; i< paramMetas.length; ++i){
            TypeInterpretationResult<?> result;
            try {
                 result = paramMetas[i].transform(message, tokenizer);
            }catch (NoSuchElementException nse){
                return new InvocationFailure(i/paramMetas.length);
            }
            if (result.isSuccess()) {
                params[i + 1] = result.getResult();
            }else{
                return new InvocationFailure(i/paramMetas.length);
            }
        }
        // call the method
        try {
            method.invoke(command, params);
            // no failure
            return null;
        }catch (IllegalAccessException iae){
            throw new IllegalStateException("Smart Invoker cannot access method "+method);
        }catch (InvocationTargetException ite){
            // method threw an exception
            Throwable target = ite.getTargetException();
            if (target instanceof RuntimeException){
                // let target exception pass through the stack
                throw (RuntimeException) target;
            }else{
                // no declarations of this are allowed, so this should never happen.
                throw new RuntimeException("SmartInvoke command threw non-runtime exception",ite);
            }
        }
    }

    /**
     * Prepares the invocation meta of the SmartInvoker.
     * This
     */
    private void prepareInvocationMeta(Command command){
        Parameter[] params = method.getParameters();
        if (params.length < 1 || params[0].getType() != Message.class){
            throw new IllegalArgumentException("First parameter of a SmartInvoke invocation must be of class "+Message.class);
        }
        // first argument has no parameter meta
        assert paramMetas.length == params.length-1;
        // all normal parameters are the same
        for (int i=0;i<params.length-1;++i){
            Parameter param = params[i+1];
            Class<?> paramType = param.getType();
            if (!InterpretationUtil.supports(paramType)){
                throw new IllegalArgumentException("Parameter "+param.getName()+" of command "+command.getClass().getName()+" has an unsupported type ("+ paramType);
            }
            // create meta
            paramMetas[i] = createAnywhereParameterMeta(param);
        }
        // create last meta, which may be varargs or raw
        if (paramMetas.length > 0){
            Parameter param = params[params.length-1];
            String name = getParameterName(param);
            ParameterMeta lastMeta;
            if (methodMeta.lastArgRaw()){
                Class<?> rawType = param.getType();
                if (rawType != String.class){
                    throw new IllegalArgumentException("When the last argument is to be raw, it may only be of type "+String.class+" (encountered "+rawType+")");
                }
                lastMeta = new RawParameterMeta(name);
            }else if (method.isVarArgs()){
                Class<?> rawType = param.getType();
                lastMeta = new VarArgsParameterMeta(name,rawType);
            }else{
                // normal type
                lastMeta = createAnywhereParameterMeta(param);
            }
            paramMetas[paramMetas.length-1] = lastMeta;
        }
    }

    /**
     * Creates a fitting parameter meta that may be used at any parameter location
     * @param param the parameter to create a meta for
     * @return the parameter meta most fitting for this parameter
     */
    private static ParameterMeta createAnywhereParameterMeta(Parameter param){
        return param.getType().isArray()
                ? createArrayParameterMeta(param)
                : createDirectParameterMeta(param);
    }

    /**
     * Creates a parameter meta for an array parameter
     * @param param the parameter to create a meta for
     * @return the parameter meta
     */
    private static ParameterMeta createArrayParameterMeta(Parameter param){
        assert param.getType().isArray();
        FixedSize fixedSize = param.getAnnotation(FixedSize.class);
        if (fixedSize == null){
            throw new IllegalArgumentException("Array parameters must have a Fixed size. See the "+FixedSize.class+" annotation");
        }
        if (fixedSize.value() < 2){
            throw new IllegalArgumentException("Fixed size array parameters must have a size of at least 2");
        }
        String name = getParameterName(param);
        return new FixedArraySizeParameterMeta(name,param.getType(),fixedSize.value());
    }

    /**
     * Creates a parameter meta for a direct parameter
     * @param param the parameter to create a meta for
     * @return the parameter meta for the given argument
     */
    private static ParameterMeta createDirectParameterMeta(Parameter param){
        // ensure type is not an array
        assert !param.getType().isArray();
        String name = getParameterName(param);
        // create the meta
        return new SimpleParameterMeta(name,param.getType());
    }
}
