package run.halo.imagestream.client;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
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
public class PixabayEndpoint implements CustomEndpoint {

    private final WebClientFactory webClientFactory;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "PixabayV1alpha1";
        return SpringdocRouteBuilder.route()
            .GET("/photos/-/search", this::searchImages, builder -> {
                builder.operationId("SearchPixImages")
                    .description("Search images")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(ObjectNode.class));
                buildParam(builder);
            })
            .build();
    }

    private Mono<ServerResponse> searchImages(ServerRequest request) {
        var pixClient = getWebClient();
        return pixClient.get()
            .uri(uriBuilder -> uriBuilder.path("/")
                .queryParams(request.queryParams()).build())
            .exchangeToMono(ClientUtils::responseExtractor);
    }

    private WebClient getWebClient() {
        return webClientFactory.getWebClient(WebClientType.PIXABAY);
    }

    public static void buildParam(Builder builder) {
        builder.parameter(parameterBuilder()
                .name("q")
                .in(ParameterIn.QUERY)
                .description("A URL encoded search term. If omitted, all images are returned."
                             + " This value may not exceed 100 characters.\n"
                             + "Example: \"yellow+flower\"")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("page")
                .in(ParameterIn.QUERY)
                .description("Returned search results are paginated. Use this parameter to select the page number.\n"
                             + "Default: 1")
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("per_page")
                .in(ParameterIn.QUERY)
                .description("""
                    Determine the number of results per page.
                    Accepted values: 3 - 200
                    Default: 20
                    """)
                .implementation(Integer.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("image_type")
                .in(ParameterIn.QUERY)
                .description("""
                     Filter results by image type.
                     Accepted values: "all", "photo", "illustration", "vector"
                     Default: "all"
                    """)
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("category")
                .in(ParameterIn.QUERY)
                .description("""
                    Filter results by category.
                    Accepted values: backgrounds, fashion, nature, science, education, feelings, health, people,\
                     religion, places, animals, industry, computer, food, sports, transportation, travel,\
                      buildings, business, music
                    """)
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("order")
                .in(ParameterIn.QUERY)
                .description("""
                    How the results should be ordered.
                    Accepted values: "popular", "latest"
                    Default: "popular"
                    """)
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .name("safesearch")
                .in(ParameterIn.QUERY)
                .description("""
                    A flag indicating that only images suitable for all ages should be returned.
                    Accepted values: "true", "false"
                    Default: "false"
                    """)
                .implementation(String.class)
                .required(false));
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("pixabay.halo.run", "v1alpha1");
    }
}
