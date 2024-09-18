package run.halo.imagestream.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ProvidedApiKeysLoader {
    static final String API_KEY_FILE = "apiKeys.properties";
    private final Properties properties;

    public ProvidedApiKeysLoader() {
        this.properties = load();
    }

    public Optional<String> getApiKey(String key) {
        var value = properties.getProperty(key);
        return StringUtils.isNotBlank(value) ? Optional.of(value) : Optional.empty();
    }

    private Properties load() {
        var properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(API_KEY_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            // ignore
        }
        return properties;
    }
}
