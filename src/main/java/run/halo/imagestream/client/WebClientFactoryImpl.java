package run.halo.imagestream.client;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import run.halo.app.plugin.PluginConfigUpdatedEvent;
import run.halo.imagestream.model.BasicProp;

@Component
public class WebClientFactoryImpl implements WebClientFactory {
    private static final Map<String, WebClient> WEB_CLIENTS = new HashMap<>(WebClientType.values().length, 1);

    @Override
    public WebClient getWebClient(WebClientType clientType) {
        return WEB_CLIENTS.get(clientType.name());
    }

    @EventListener(PluginConfigUpdatedEvent.class)
    public void onPluginConfigUpdated(PluginConfigUpdatedEvent event) {
        WEB_CLIENTS.clear();
        var newConfig = event.getNewConfig();
        if (newConfig == null) {
            return;
        }
        createClients(BasicProp.from(newConfig));
    }

    void createClients(BasicProp basicProp) {
        for (WebClientType value : WebClientType.values()) {
            switch (value) {
                case UNSPLASH:
                    var prop = basicProp.getUnsplash();
                    var accessKey = prop.getAccessKey();
                    var authorizationValue = StringUtils.isBlank(accessKey) ? null : "Client-ID " + accessKey;
                    var unsplashClient = createClient("https://api.unsplash.com", authorizationValue);
                    WEB_CLIENTS.put(value.name(), unsplashClient);
                    break;
                case PEXELS:
                    var pexelsProp = basicProp.getPexels();
                    var pexelsClient = createClient("https://api.pexels.com/v1", pexelsProp.getApiKey());
                    WEB_CLIENTS.put(value.name(), pexelsClient);
                    break;
                case PIXABAY:
                    var pixabayProp = basicProp.getPixabay();
                    var pixabayClient = createClient("https://pixabay.com/api", pixabayProp.getApiKey());
                    WEB_CLIENTS.put(value.name(), pixabayClient);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported web client type: " + value);
            }
        }
    }

    private WebClient createClient(String baseUrl, String authorizationValue) {
        var builder = WebClient.builder().baseUrl(baseUrl);
        if (StringUtils.isNotBlank(authorizationValue)) {
            builder.defaultHeader("Authorization", authorizationValue);
        }
        return builder.build();
    }
}
