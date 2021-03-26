package nl.saxion.discord.bot.internal.smartinvoke.tokenizer;

public enum TokenizationFailureType {
    UNBALANCED_QUOTES("Unbalanced quotes"),

    ;
    private final String message;

    TokenizationFailureType(String message){
        this.message = message;
    }


    public String getMessage() {
        return message;
    }
}
