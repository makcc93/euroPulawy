package pl.eurokawa.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import pl.eurokawa.user.UserRepository;

import javax.sql.DataSource;

@Component
@DependsOn("entityManagerFactory")
public class DatabaseInitializer implements ApplicationRunner {

    private final DataSource dataSource;
    private final SqlInitializationProperties properties;
    private final UserRepository userRepository;

    public DatabaseInitializer(DataSource dataSource, SqlInitializationProperties properties, UserRepository userRepository) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.count() == 0L){
            new SqlDataSourceScriptDatabaseInitializer(dataSource,properties).initializeDatabase();
        }}
}
