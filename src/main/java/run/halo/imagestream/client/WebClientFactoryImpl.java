package run.halo.imagestream.client;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Secret;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.PluginConfigUpdatedEvent;
import run.halo.imagestream.model.BasicProp;
import run.halo.imagestream.model.PexelsProp;
import run.halo.imagestream.model.PixabayProp;
import run.halo.imagestream.model.UnsplashProp;

@Component
@RequiredArgsConstructor
public class WebClientFactoryImpl implements WebClientFactory, DisposableBean {
    private static final Map<String, WebClient> WEB_CLIENTS = new ConcurrentHashMap<>(WebClientType.values().length, 1);

    private final ProvidedApiKeysLoader providedApiKeysLoader;
    private final ExtensionClient client;

    static final String CONFIG_MAP_NAME = "image-stream-configmap";

    @PostConstruct
    void init() {
        var basicProp = client.fetch(ConfigMap.class, CONFIG_MAP_NAME)
            .map(ConfigMap::getData)
            .map(WebClientFactoryImpl::toJsonNodeMap)
            .map(BasicProp::from)
            .orElseGet(BasicProp::new);
        createClients(basicProp);
    }

    static Map<String, JsonNode> toJsonNodeMap(Map<String, String> data) {
        var result = new HashMap<String, JsonNode>(data.size());
        data.forEach((key, value) -> {
            try {
                result.put(key, JsonUtils.jsonToObject(value, JsonNode.class));
            } catch (Exception e) {
                // ignore invalid json
            }
        });
        return result;
    }

    @Override
    public WebClient getWebClient(WebClientType clientType) {
        return WEB_CLIENTS.get(clientType.name());
    }

    @Async
    @EventListener(PluginConfigUpdatedEvent.class)
    public void onPluginConfigUpdated(PluginConfigUpdatedEvent event) {
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
                    var pixabayClient = createClient("https://pixabay.com/api?key=" + accessKey, null);
                    WEB_CLIENTS.put(value.name(), pixabayClient);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported web client type: " + value);
            }
        }
    }

    private String getApiKeyOrProvided(WebClientType type, BasicProp basicProp) {
        Map<WebClientType, Supplier<String>> apiKeySuppliers = Map.of(
            WebClientType.UNSPLASH, () -> fetchApiKey(basicProp.getUnsplash().getApiKeySecretName(), UnsplashProp.SECRET_KEY),
            WebClientType.PEXELS, () -> fetchApiKey(basicProp.getPexels().getApiKeySecretName(), PexelsProp.SECRET_KEY),
            WebClientType.PIXABAY, () -> fetchApiKey(basicProp.getPixabay().getApiKeySecretName(), PixabayProp.SECRET_KEY)
        );
        return Optional.ofNullable(apiKeySuppliers.get(type))
            .map(Supplier::get)
            .filter(StringUtils::isNotBlank)
            .orElseGet(() -> fetchProvidedApiKey(type));
    }

    String fetchApiKey(String secretName, String key) {
        return client.fetch(Secret.class, secretName)
            .map(Secret::getStringData)
            .map(data -> data.get(key))
            .orElse(null);
    }

    private String fetchProvidedApiKey(WebClientType type) {
        return providedApiKeysLoader.getApiKey(type.name().toLowerCase()).orElse(null);
    }

    private WebClient createClient(String baseUrl, String authorizationValue) {
        var strategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();
        var builder = WebClient.builder().baseUrl(baseUrl)
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (StringUtils.isNotBlank(authorizationValue)) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, authorizationValue);
        }
        return builder.build();
    }

    @Override
    public void destroy() {
        WEB_CLIENTS.clear();
    }
}
