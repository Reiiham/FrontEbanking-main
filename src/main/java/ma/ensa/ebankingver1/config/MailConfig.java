package ma.ensa.ebankingver1.config;

import io.mailtrap.client.*;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:mail.properties")
public class MailConfig {
    @Autowired
    private Environment env;

    @Bean
    public MailtrapClient mailtrapClient() {
        final MailtrapConfig config = new MailtrapConfig.Builder()
                .token(env.getProperty("mailtrap.token"))
                .build();
        return MailtrapClientFactory.createMailtrapClient(config);
    }
}
