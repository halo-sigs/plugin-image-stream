package run.halo.imagestream.client;

import org.springframework.web.reactive.function.client.WebClient;

public interface WebClientFactory {
    WebClient getWebClient(WebClientType clientType);
}
