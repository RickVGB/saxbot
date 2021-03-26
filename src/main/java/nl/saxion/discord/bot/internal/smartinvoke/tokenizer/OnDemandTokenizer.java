package nl.saxion.discord.bot.internal.smartinvoke.tokenizer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.MiscUtil;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.tokens.MentionToken;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.tokens.SnowflakeMentionToken;
import nl.saxion.discord.bot.internal.smartinvoke.tokenizer.tokens.Token;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * A tokenizer implementation that functions as an iterable source of tokens useful for discord application.
 * This will only parse the next token when requested.
 */
public class OnDemandTokenizer extends Tokenizer {
    /**
     * The string the tokenizer is parsing
     */
    private final String source;
    /**
     * The caret, telling the parser at what character it left off
     */
    private int caret;

    /**
     * Creates a new Tokenizer
     * @param source the source string to tokenize
     * @param flags the flags of the tokenizer
     */
    public OnDemandTokenizer(String source, int flags){
        super(flags);
        this.source = source;
        ensureValidFlags(flags);
        moveCaretToNextToken();
    }

    /**
     * moves the caret to the next token, skipping whitespace if required
     */
    private void moveCaretToNextToken(){
        if (hasFlag(FLAG_IGNORE_WHITESPACE) && !hasFlag(FLAG_PURE_TEXT)) {
            // skip over any whitespace
            while(caret != source.length() && Character.isWhitespace(source.charAt(caret))){
                ++caret;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return caret != source.length();
    }

    @Override
    public @Nonnull Token next() throws TokenizationFailure {
        if (hasNext()){
            Token next = parseNext();
            moveCaretToNextToken();
            return next;
        }else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public int mark() {
        return caret;
    }

    @Override
    public void restore(int mark) {
        Objects.checkIndex(mark,source.length());
        caret = mark;
    }

    @Override
    public @Nonnull
    Token remaining() {
        String raw = source.substring(caret);
        caret = source.length();
        return new Token(raw);
    }

    /**
     * loads the next token into {@link #next}
     * @throws TokenizationFailure if a token cannot be parsed
     */
    private @Nonnull Token parseNext() throws TokenizationFailure {
        // skip to next token
        char current = source.charAt(caret);

//        System.out.println("src["+caret+"]='"+current+'\'');
        switch (current){
            case '<':
                // attempt to parse as mention
                Token snowflakeToken = parseSnowflakeMention();
                if (snowflakeToken != null){
                    return snowflakeToken;
                }
                // not a valid snowflake token
                break;
            case '"':
                // attempt to parse as quoted
                if (!hasFlag(FLAG_PURE_TEXT) && hasFlag(FLAG_USE_QUOTES)){
                    Token quoted = parseQuoted();
                    if (quoted != null){
                        return quoted;
                    }
                }
                break;
            case '@':
                // @everyone or @here?
                Token mentionToken = parseSpecialMention();
                if (mentionToken != null){
                    return mentionToken;
                }
                // not a valid special mention token
            default:
                // just another whitespace filling, continue
                if (Character.isWhitespace(current)){
                    assert !hasFlag(FLAG_IGNORE_WHITESPACE);
                    return parseWhitespace();
                }
        }
        // regular character, parse as regular token
        return parseSimple();
    }

    /**
     * sets the next token as the substring of the source from the caret to the end index (exclusive) and then moves the caret to this index
     * @param tokenEnd the end of the token
     */
    private @CheckReturnValue
    Token nextRaw(int tokenEnd){
        Token token = new Token(source.substring(caret,tokenEnd));
        caret = tokenEnd;
        return token;
    }

    /**
     * parses a whitespace token
     */
    private @CheckReturnValue
    Token parseWhitespace(){
        for (int tokenEnd=caret;tokenEnd<source.length();++tokenEnd){
            if (!Character.isWhitespace(source.charAt(tokenEnd))){
                return nextRaw(tokenEnd);
            }
        }
        // end of string reached
        return remaining();
    }

    /**
     * Checks whether a mention token can be parsed at the given index
     * @param index the index of the token to view
     * @return {@code true} if a mention token can be found at the given index
     */
    private boolean hasMentionTokenAt(int index){
        // save old caret position
        int previousPosition = caret;
        // set caret at index
        caret = index;
        // check if token found
        MentionToken token = null;
        switch (source.charAt(index)){
            case '<':
                token = parseSnowflakeMention();
                break;
            case '@':
                token = parseSpecialMention();
                break;
        }
        // return caret to original position
        caret = previousPosition;
        return token != null;
    }

    /**
     * cut at the first encountered space or special mention
     */
    private @CheckReturnValue
    Token parseSimple(){
        for (int tokenEnd=caret;tokenEnd<source.length();++tokenEnd){
            // end token on special mentions or whitespace
            if (hasMentionTokenAt(tokenEnd) ||
                    // do not check for whitespace tokens on pure text mode
                    (!hasFlag(FLAG_PURE_TEXT) && Character.isWhitespace(source.charAt(tokenEnd)))
            ){
                return nextRaw(tokenEnd);
            }
        }
        // end of string reached
        return remaining();
    }

    /**
     * Parses a quoted literal. This will remove the quotes from both sides and all double quotes ("") will be turned into single quotes (")
     * @return the quoted literal
     * @throws TokenizationFailure if the quoted text is formatted incorrectly. Currently, this only applies in strict mode
     */
    private @CheckReturnValue @Nullable
    Token parseQuoted() throws TokenizationFailure {
        int lastCut = caret+1;
        StringBuilder builder = new StringBuilder();
        boolean isQuoteEnd = false;
        for (int localCaret =caret+1;localCaret<source.length();++localCaret){
            // get current character
            char current = source.charAt(localCaret);
            if (current == '"'){
                if (isQuoteEnd){
                    // cut off to current part but include the caret
                    builder.append(source,lastCut,localCaret-1);
                    lastCut = localCaret;
                }else{
                    isQuoteEnd = true;
                    continue;
                }
            }else if (Character.isWhitespace(current)){// literal: {" } encountered
                if (isQuoteEnd){
                    caret = localCaret - 1;
                    builder.append(source,lastCut,caret);
                    return new Token(builder.toString());
                }
            }
            isQuoteEnd = false;
        }
        // end of source
        if (isQuoteEnd){
            Token out = new Token(builder.append(source,lastCut,source.length()-1).toString());
            caret = source.length();
            return out;
        }
        // quoted area not properly closed
        if (hasFlag(FLAG_STRICT_QUOTES)){
            // unbalanced quotes
            throw new TokenizationFailure(TokenizationFailureType.UNBALANCED_QUOTES,caret,source.length());
        }
        // not strict, parse as normal token(s)
        return null;
    }

    /**
     * Parses a special mention type.
     * Currently, this only applies to @everyone and @here
     * @return the matched token if any
     */
    private MentionToken parseSpecialMention(){
        assert source.charAt(caret) == '@';
        if (hasFlag(FLAG_SPLIT_MENTION_EVERYONE) && lookaheadConsume("@everyone")){
            return MentionToken.EVERYONE;
        }
        if (hasFlag(FLAG_SPLIT_MENTION_HERE) && lookaheadConsume("@here")){
            return MentionToken.HERE;
        }
        return null;
    }

    /**
     * Looks ahead to see if the given sequence is present in the source string at the caret.
     * The caret is moved to the character after the match if found.
     * @param seq the character sequence to check
     * @return {@code true} if the sequence appears at the source starting from the caret
     */
    private boolean lookaheadConsume(CharSequence seq){
        boolean matches = subMatch(source,caret,seq);
        if (matches){
            // consume the token
            caret += seq.length();
        }
        return matches;
    }

    /**
     * looks ahead to see if the given sequence is present in the source string at the caret.
     * Nothing is done to the caret
     * @param seq the character sequence to check
     * @return {@code true} if the sequence appears at the source starting from the caret
     */
    private static boolean subMatch(CharSequence source,int index,CharSequence seq){
        int left = source.length() - index;
        if (left < seq.length()){
            return false; // sequence cannot appear as the source ends too soon
        }
        for (int i=0;i<seq.length();++i){
            if (seq.charAt(i) != source.charAt(index+i)){
                return false; // mismatching character
            }
        }
        // full match
        return true;
    }

    /**
     * parses a snowflake mention.
     * The caret will be moved if and only if a valid mention token is found
     * @return the mention found, null if there is no snowflake mention at the caret
     */
    private SnowflakeMentionToken parseSnowflakeMention(){
        assert source.charAt(caret) == '<';
        if (hasFlag(FLAG_SPLIT_MENTION_SNOWFLAKE)) {
            // allowed to parse snowflake mentions
            for (int tokenEnd = caret; tokenEnd < source.length(); ++tokenEnd) {
                // find the end
                if (source.charAt(tokenEnd) == '>') {
                    // cut the mention and test every type
                    String mention = source.substring(caret, tokenEnd + 1);
                    for (Message.MentionType type : Message.MentionType.values()) {
                        Matcher matcher = type.getPattern().matcher(mention);
                        if (matcher.matches()) {
                            long snowflake = MiscUtil.parseSnowflake(matcher.group(1));
                            // move caret to end of token
                            caret = tokenEnd + 1;
                            return new SnowflakeMentionToken(mention, type, snowflake);
                        }
                    }
                    // not a valid mention
                }
            }
        }
        return null;
    }
}
