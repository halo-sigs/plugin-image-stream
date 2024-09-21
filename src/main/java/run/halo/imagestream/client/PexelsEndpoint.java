package run.halo.imagestream.client;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

@Component
@RequiredArgsConstructor
public class PexelsEndpoint implements CustomEndpoint {
    private final WebClientFactory webClientFactory;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "PexelsV1alpha1";
        return SpringdocRouteBuilder.route()
            .GET("/photos/-/search", this::searchPhotos, builder -> {
                builder.operationId("SearchPexPhotos")
                    .description("Search photos for pexels")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(ObjectNode.class));
                buildSearchPhotoParam(builder);
            })
            .GET("/photos/-/curate", this::curatePhotos, builder -> {
                builder.operationId("CuratedPexPhotos")
                    .description("This endpoint enables you to receive real-time photos curated by the Pexels team.")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(ObjectNode.class));
                buildCuratedPhotoParam(builder);
            })
            .build();
    }

    private Mono<ServerResponse> curatePhotos(ServerRequest request) {
        var pexelsClient = getWebClient();
        return pexelsClient.get()
            .uri(uriBuilder -> uriBuilder.path("/curated")
                .queryParams(request.queryParams())
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .exchangeToMono(ClientUtils::responseExtractor);
    }

    private Mono<ServerResponse> searchPhotos(ServerRequest request) {
        var pexelsClient = getWebClient();
        return pexelsClient.get()
            .uri(uriBuilder -> uriBuilder.path("/search")
                .queryParams(request.queryParams())
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .exchangeToMono(ClientUtils::responseExtractor);
    }

    private WebClient getWebClient() {
        return webClientFactory.getWebClient(WebClientType.PEXELS);
    }

    static void buildCuratedPhotoParam(Builder builder) {
        builder.parameter(parameterBuilder()
                .name("page")
                .in(ParameterIn.QUERY)
                .description("The page number you are requesting. Default: 1")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("per_page")
                .in(ParameterIn.QUERY)
                .description("The number of results you are requesting per page."
                             + "Default: 15, Max: 80")
                .implementation(Integer.class)
                .required(false));
    }

    static void buildSearchPhotoParam(Builder builder) {
        builder.parameter(parameterBuilder()
                .name("page")
                .in(ParameterIn.QUERY)
                .description("The page number you are requesting. Default: 1")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("per_page")
                .in(ParameterIn.QUERY)
                .description("The number of results you are requesting per page."
                             + "Default: 15, Max: 80")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("query")
                .in(ParameterIn.QUERY)
                .description("The search query. Ocean, Tigers, Pears, etc.")
                .implementation(String.class)
                .required(false));
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("pexels.halo.run", "v1alpha1");
    }
}
