package pl.eurokawa.token.strategy;

import org.springframework.stereotype.Component;

import java.util.Random;
@Component
public class RandomCodeGenerator {

    private RandomCodeGenerator(){}

    public static String randomDigits(int length){
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 1; i <= length; i++){
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}
