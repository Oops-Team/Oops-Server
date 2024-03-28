package com.oops.server.initializer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@PropertySource("classpath:application-secrete.yml")
public class FcmInitializer {

    @Value("${firebase.key-path}")
    private String fcmKeyPath;

    @PostConstruct
    public void setting() throws IOException {
        InputStream input = new ClassPathResource(fcmKeyPath).getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                                                 .setCredentials(
                                                         GoogleCredentials.fromStream(input))
                                                 .build();

        FirebaseApp.initializeApp(options);

        log.info("FCM 세팅 완료");
    }
}
