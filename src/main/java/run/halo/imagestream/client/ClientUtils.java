package run.halo.imagestream.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.infra.utils.JsonUtils;

@UtilityClass
public class ClientUtils {

    public static Mono<ServerResponse> responseExtractor(ClientResponse clientResponse) {
        var serverResponseBuilder = ServerResponse.status(clientResponse.statusCode())
            .headers(headers -> headers.addAll(clientResponse.headers().asHttpHeaders()));
        return clientResponse.bodyToMono(String.class)
            .flatMap(ClientUtils::parseJsonNode)
            .flatMap(serverResponseBuilder::bodyValue)
            .switchIfEmpty(serverResponseBuilder.build());
    }

    private static Mono<JsonNode> parseJsonNode(String body) {
        return Mono.fromCallable(() -> {
                try {
                    return JsonUtils.jsonToObject(body, JsonNode.class);
                } catch (Exception e) {
                    throw new ServerWebInputException(body);
                }
            })
            .subscribeOn(Schedulers.boundedElastic());
    }
}
