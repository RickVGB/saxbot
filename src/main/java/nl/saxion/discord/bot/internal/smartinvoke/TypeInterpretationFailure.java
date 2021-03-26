//package nl.saxion.discord.bot.internal.smartinvoke;
//
//import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.tokens.Token;
//
///**
// * An exception thrown by the {@link InterpretationUtil Type interpreter} if a type cannot be interpreted as the target type
// */
//public class TypeInterpretationFailure extends Exception{
//    public TypeInterpretationFailure(Token token, Class<?> targetType) {
//        super(makeMessage(token,targetType));
//    }
//    public TypeInterpretationFailure(Token token, Class<?> targetType, Throwable cause) {
//        super(makeMessage(token,targetType), cause);
//    }
//
//    private static String makeMessage(Token token, Class<?> targetType){
//        return "Token \""+token+"\" cannot be converted to "+targetType;
//    }
//}
