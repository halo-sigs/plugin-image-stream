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
public class UnsplashEndpoint implements CustomEndpoint {

    private final WebClientFactory webClientFactory;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "UnsplashV1alpha1";
        return SpringdocRouteBuilder.route()
            .GET("/photos/-/search", this::searchPhotos, builder -> {
                builder.operationId("SearchUnsplashPhotos")
                    .description("Search photos")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(ObjectNode.class));
                buildSearchPhotosParam(builder);
            })
            .GET("/topics", this::listTopics, builder -> {
                builder.operationId("ListUnsplashTopics")
                    .description("Get a single page from the list of all topics.")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(ObjectNode.class));
                buildListTopicsParam(builder);
            })
            .GET("/topics/{idOrSlug}/photos", this::getTopicPhotos, builder -> {
                builder.operationId("GetUnsplashTopicPhotos")
                    .description("Retrieve a topic’s photos.")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementationArray(ObjectNode.class));
                buildGetTopicPhotosParam(builder);
            })
            .GET("/photos/{id}/download", this::trackPhotoDownload, builder ->
                builder.operationId("TrackUnsplashPhotoDownload")
                    .description("Track a photo download.")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(ObjectNode.class))
            )
            .build();
    }

    private Mono<ServerResponse> trackPhotoDownload(ServerRequest request) {
        var unsplashClient = getUnsplashWebClient();
        return unsplashClient.get()
            .uri(uriBuilder -> uriBuilder.path("/photos/{id}/download")
                .build(request.pathVariable("id"))
            )
            .exchangeToMono(ClientUtils::responseExtractor);
    }

    private Mono<ServerResponse> listTopics(ServerRequest request) {
        var unsplashClient = getUnsplashWebClient();
        return unsplashClient.get()
            .uri(uriBuilder -> uriBuilder.path("/topics")
                .queryParams(request.queryParams()).build())
            .exchangeToMono(ClientUtils::responseExtractor);
    }

    private Mono<ServerResponse> getTopicPhotos(ServerRequest request) {
        var unsplashClient = getUnsplashWebClient();
        return unsplashClient.get()
            .uri(uriBuilder -> uriBuilder.path("/topics/{idOrSlug}/photos")
                .queryParams(request.queryParams())
                .build(request.pathVariable("idOrSlug")))
            .exchangeToMono(ClientUtils::responseExtractor);
    }

    private WebClient getUnsplashWebClient() {
        return webClientFactory.getWebClient(WebClientType.UNSPLASH);
    }

    private Mono<ServerResponse> searchPhotos(ServerRequest request) {
        var unsplashClient = getUnsplashWebClient();
        return unsplashClient.get()
            .uri(uriBuilder -> uriBuilder.path("/search/photos")
                .queryParams(request.queryParams())
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .exchangeToMono(ClientUtils::responseExtractor);
    }

    public static void buildListTopicsParam(Builder builder) {
        builder.parameter(parameterBuilder()
                .name("page")
                .in(ParameterIn.QUERY)
                .description("Page number to retrieve. (Optional; default: 1)")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("per_page")
                .in(ParameterIn.QUERY)
                .description("Number of items per page. (Optional; default: 10)")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("ids")
                .in(ParameterIn.QUERY)
                .description("Limit to only matching topic ids or slugs. (Optional; Comma separated string)")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("order_by")
                .in(ParameterIn.QUERY)
                .description("How to sort the topics. (Optional; Valid values: featured, latest, oldest, position;"
                             + " default: position)")
                .implementation(String.class)
                .required(false));
    }

    public static void buildGetTopicPhotosParam(Builder builder) {
        builder.parameter(parameterBuilder()
                .name("idOrSlug")
                .description("The topic ID or slug.")
                .in(ParameterIn.PATH)
                .required(true))
            .parameter(parameterBuilder()
                .name("page")
                .in(ParameterIn.QUERY)
                .description("Page number to retrieve. (Optional; default: 1)")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("per_page")
                .in(ParameterIn.QUERY)
                .description("Number of items per page. (Optional; default: 10)")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("order_by")
                .in(ParameterIn.QUERY)
                .description("How to sort the photos. (Optional; default: latest)."
                             + " Valid values are latest and oldest.")
                .required(false))
            .parameter(parameterBuilder()
                .name("orientation")
                .in(ParameterIn.QUERY)
                .description("Filter by photo orientation. (Optional; Valid values: landscape, portrait, squarish)")
                .required(false));
    }

    public static void buildSearchPhotosParam(Builder builder) {
        builder.parameter(parameterBuilder()
                .name("query")
                .description("Search terms")
                .in(ParameterIn.QUERY)
                .required(true))
            .parameter(parameterBuilder()
                .name("page")
                .in(ParameterIn.QUERY)
                .description("Page number to retrieve. (Optional; default: 1)")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("per_page")
                .in(ParameterIn.QUERY)
                .description("Number of items per page. (Optional; default: 10)")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("order_by")
                .in(ParameterIn.QUERY)
                .description("How to sort the photos. (Optional; default: relevant)."
                             + " Valid values are latest and relevant.")
                .required(false))
            .parameter(parameterBuilder()
                .name("color")
                .in(ParameterIn.QUERY)
                .description("Filter results by color. Optional. Valid values are: black_and_white, black, white,"
                             + " yellow, orange, red, purple, magenta, green, teal, and blue.")
                .required(false))
            .parameter(parameterBuilder()
                .name("orientation")
                .in(ParameterIn.QUERY)
                .description("Filter by photo orientation. Optional. (Valid values: landscape, portrait, squarish)")
                .required(false))
            .parameter(parameterBuilder()
                .name("content_filter")
                .in(ParameterIn.QUERY)
                .description("Limit results by content safety. (Optional; default: low)."
                             + " Valid values are low and high.")
                .required(false))
            .parameter(parameterBuilder()
                .name("collections")
                .in(ParameterIn.QUERY)
                .implementation(String.class)
                .description("Collection ID(‘s) to narrow search. Optional. If multiple, comma-separated.")
                .required(false))
            .parameter(parameterBuilder()
                .name("lang")
                .in(ParameterIn.QUERY)
                .description("Beta parameters: Supported ISO 639-1 language code of the query." +
                             " Optional, default: \"en\"")
                .required(false));
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("unsplash.halo.run", "v1alpha1");
    }
}
