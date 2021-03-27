package nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens;

import net.dv8tion.jda.api.entities.Message.MentionType;

import java.util.Objects;

/**
 * An interface for tokens that represent a {@link MentionType MentionType}
 */
public class MentionToken extends Token {
    /**
     * The type of mention this token is of
     */
    private final MentionType type;

    /**
     * Creates a new MentionToken
     * @param raw the raw string representation of the token
     * @param type the type of the token
     */
    public MentionToken(String raw, MentionType type) {
        super(raw);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    /**
     * returns the type of the token
     * @return the type of the underlying token
     */
    public final MentionType getMentionType(){
        return type;
    }

    /**
     * A special token for the @everyone mention.
     * Since this token is always the same, this constant can be used to save memory
     */
    public static final MentionToken EVERYONE = new MentionToken("@everyone", MentionType.EVERYONE);
    /**
     * A special token for the @here mention.
     * Since this token is always the same, this constant can be used to save memory
     */
    public static final MentionToken HERE     = new MentionToken("@here"    , MentionType.HERE    );

    @Override
    public String toString() {
        return "MentionToken:"+getRaw();
    }
}
