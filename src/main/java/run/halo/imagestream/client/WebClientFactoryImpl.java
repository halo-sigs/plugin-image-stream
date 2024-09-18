package run.halo.imagestream.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import run.halo.app.plugin.PluginConfigUpdatedEvent;
import run.halo.imagestream.model.BasicProp;

@Component
@RequiredArgsConstructor
public class WebClientFactoryImpl implements WebClientFactory {
    private static final Map<String, WebClient> WEB_CLIENTS = new HashMap<>(WebClientType.values().length, 1);

    private final ProvidedApiKeysLoader providedApiKeysLoader;

    @Override
    public WebClient getWebClient(WebClientType clientType) {
        return WEB_CLIENTS.get(clientType.name());
    }

    @EventListener(PluginConfigUpdatedEvent.class)
    public void onPluginConfigUpdated(PluginConfigUpdatedEvent event) {
        WEB_CLIENTS.clear();
        var newConfig = event.getNewConfig();
        createClients(BasicProp.from(newConfig));
    }

    void createClients(BasicProp basicProp) {
        for (WebClientType value : WebClientType.values()) {
            var accessKey = getApiKeyOrProvided(value, basicProp);
            switch (value) {
                case UNSPLASH:
                    var authorizationValue = StringUtils.isBlank(accessKey) ? null : "Client-ID " + accessKey;
                    var unsplashClient = createClient("https://api.unsplash.com", authorizationValue);
                    WEB_CLIENTS.put(value.name(), unsplashClient);
                    break;
                case PEXELS:
                    var pexelsClient = createClient("https://api.pexels.com/v1", accessKey);
                    WEB_CLIENTS.put(value.name(), pexelsClient);
                    break;
                case PIXABAY:
                    var pixabayClient = createClient("https://pixabay.com/api", accessKey);
                    WEB_CLIENTS.put(value.name(), pixabayClient);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported web client type: " + value);
            }
        }
    }

    private String getApiKeyOrProvided(WebClientType type, BasicProp basicProp) {
        Map<WebClientType, Supplier<String>> apiKeySuppliers = Map.of(
            WebClientType.UNSPLASH, () -> basicProp.getUnsplash().getAccessKey(),
            WebClientType.PEXELS, () -> basicProp.getPexels().getApiKey(),
            WebClientType.PIXABAY, () -> basicProp.getPixabay().getApiKey()
        );
        return Optional.ofNullable(apiKeySuppliers.get(type))
            .map(Supplier::get)
            .filter(StringUtils::isNotBlank)
            .orElseGet(() -> fetchProvidedApiKey(type));
    }

    private String fetchProvidedApiKey(WebClientType type) {
        return providedApiKeysLoader.getApiKey(type.name().toLowerCase()).orElse(null);
    }

    private WebClient createClient(String baseUrl, String authorizationValue) {
        var builder = WebClient.builder().baseUrl(baseUrl);
        if (StringUtils.isNotBlank(authorizationValue)) {
            builder.defaultHeader("Authorization", authorizationValue);
        }
        return builder.build();
    }
}
