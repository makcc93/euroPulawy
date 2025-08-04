package pl.eurokawa.email;

import pl.eurokawa.user.User;

public interface EmailService {
    void sendSixNumbersCode(EmailType emailType, User user,String code);
    void sendEmailConfirmationLink(EmailType emailType, User user, String token);
    void sendEmailNotificationToAdmins (EmailType emailType, User user, String token);

}
