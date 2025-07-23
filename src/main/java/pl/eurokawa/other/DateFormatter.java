package pl.eurokawa.other;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class DateFormatter {

    public String formatToEuropeWarsaw(String date){
        LocalDateTime localDateTime = LocalDateTime.parse(date);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime warsawDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Warsaw"));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm", Locale.of("pl", "PL"));

        return warsawDateTime.format(dateTimeFormatter);
    }
}
