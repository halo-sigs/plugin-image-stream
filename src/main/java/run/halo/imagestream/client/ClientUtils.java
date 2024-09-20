package run.halo.imagestream.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@UtilityClass
public class ClientUtils {

    public static Mono<ServerResponse> responseExtractor(ClientResponse clientResponse) {
        var serverResponseBuilder = ServerResponse.status(clientResponse.statusCode())
            .headers(headers -> headers.addAll(clientResponse.headers().asHttpHeaders()));
        return clientResponse.bodyToMono(JsonNode.class)
            .flatMap(serverResponseBuilder::bodyValue)
            .switchIfEmpty(serverResponseBuilder.build());
    }
}
