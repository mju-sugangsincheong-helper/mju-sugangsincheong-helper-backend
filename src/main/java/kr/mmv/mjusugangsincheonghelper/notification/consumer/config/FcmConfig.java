package kr.mmv.mjusugangsincheonghelper.notification.consumer.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "app.notification.enabled", havingValue = "true", matchIfMissing = true)
public class FcmConfig {

    @Value("${app.firebase.config-path:mju-sugangsincheong-helper-firebase-adminsdk.json}")
    private String configPath;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getServiceAccountStream();
                
                if (serviceAccount == null) {
                    log.warn("[FCM] Firebase config file not found at {}. FCM functionality might not work.", configPath);
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("[FCM] Firebase Application has been initialized successfully.");
            }
        } catch (IOException e) {
            log.error("[FCM] Error initializing Firebase Application", e);
        }
    }

    private InputStream getServiceAccountStream() throws IOException {
        ClassPathResource resource = new ClassPathResource(configPath);
        if (resource.exists()) {
            return resource.getInputStream();
        }
        return null;
    }
}
