package nl.saxion.discord.bot.internal.smartinvoke;

import nl.saxion.discord.bot.internal.smartinvoke.tokenize.Tokenizer;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.TokenizationFailure;
import nl.saxion.discord.bot.internal.smartinvoke.tokenize.OnDemandTokenizer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;


public class TokenizerTest {
    @Test
    public void run() throws TokenizationFailure,ReflectiveOperationException {
        Tokenizer tokenizer = new OnDemandTokenizer("one \"two three", OnDemandTokenizer.RECOMMENDED_FLAGS &~ OnDemandTokenizer.FLAG_STRICT_QUOTES);
        for (int i=0; tokenizer.hasNext();i++){
            System.out.println(i+": "+tokenizer.next());
        }
        System.out.println("done");

        for (Method method : TokenizerTest.class.getMethods()){
            if (method.getName().equals("x")){
                System.out.println(Arrays.asList(method.getParameterTypes()));
                Object[] args = new Object[2];
                args[0] = "yet";
                args[1] = new String[]{"grimace"};
//                args[2] = "yos";
                method.invoke(null,args);
                System.out.println(method.getParameterTypes()[1]);
            }
        }


    }

    public static void x(String x, String...y){
        System.out.println("x = "+x);
        System.out.println("y = "+Arrays.asList(y));
    }
}
