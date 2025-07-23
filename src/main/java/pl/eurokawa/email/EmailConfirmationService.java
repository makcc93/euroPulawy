package pl.eurokawa.email;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailConfirmationService {

    public String generateEmailConfirmationLink(Integer id){
        StringBuilder sb = new StringBuilder();
        String web = "https://europulawy.pl/users/" + id + "/emailConfirmation/";
        String randomChars = UUID.randomUUID().toString();

        sb.append(web);
        sb.append(randomChars);

        return sb.toString();
    }
}
