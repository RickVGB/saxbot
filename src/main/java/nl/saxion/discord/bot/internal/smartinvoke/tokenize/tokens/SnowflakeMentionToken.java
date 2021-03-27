package nl.saxion.discord.bot.internal.smartinvoke.tokenize.tokens;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message.MentionType;

/**
 * A special token for when a {@link IMentionable mention} is encountered in the source text
 */
public class SnowflakeMentionToken extends MentionToken{
    /**
     * The id embedded in the mention
     */
    private final long id;

    /**
     * Creates a new SnowflakeMentionToken
     * @param raw the raw token
     * @param type the type of the token
     * @param id the snowflake ID itself
     */
    public SnowflakeMentionToken(String raw, MentionType type, long id){
        super(raw,type);
        this.id = id;
    }

    /**
     * @return the snowflake id
     */
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "SnowflakeMentionToken:"+getRaw()+"("+getMentionType()+"->"+getId()+")";
    }
}
