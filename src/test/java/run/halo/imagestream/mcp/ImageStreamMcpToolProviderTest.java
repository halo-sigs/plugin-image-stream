package run.halo.imagestream.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import run.halo.imagestream.client.WebClientFactory;
import run.halo.mcpserver.api.McpToolDefinition;
import run.halo.mcpserver.api.McpToolException;
import run.halo.mcpserver.api.McpToolInvocation;

class ImageStreamMcpToolProviderTest {

    @Test
    void shouldExposeSearchAndUnsplashPreparationTools() {
        var provider = provider("{}");

        assertThat(provider.tools().map(McpToolDefinition::name).collectList().block())
            .containsExactly("search_images", "prepare_unsplash_download");
        assertThat(tool(provider, "search_images").annotations().readOnlyHint()).isTrue();
        assertThat(tool(provider, "search_images").annotations().openWorldHint()).isTrue();
        assertThat(tool(provider, "prepare_unsplash_download").annotations().readOnlyHint())
            .isFalse();
    }

    @Test
    void shouldNormalizeUnsplashSearchResults() {
        var request = new AtomicReference<ClientRequest>();
        var provider = provider("""
            {
              "total": 1,
              "results": [{
                "id": "unsplash-1",
                "width": 2400,
                "height": 1600,
                "alt_description": "A mountain",
                "urls": {"small": "https://images.unsplash.com/preview", "full": "https://images.unsplash.com/full"},
                "links": {
                  "html": "https://unsplash.com/photos/unsplash-1",
                  "download_location": "https://api.unsplash.com/photos/unsplash-1/download?ixid=tracking"
                },
                "user": {"name": "Alice", "links": {"html": "https://unsplash.com/@alice"}}
              }]
            }
            """, request);

        var result = tool(provider, "search_images").handler().execute(new McpToolInvocation(
            "search_images", Map.of("source", "unsplash", "query", "mountain"))).block();

        assertThat(result.structuredContent())
            .containsEntry("source", "unsplash")
            .containsEntry("page", 1)
            .containsEntry("size", 20)
            .containsEntry("total", 1L);
        assertThat((List<?>) result.structuredContent().get("items"))
            .singleElement()
            .isEqualTo(Map.ofEntries(
                Map.entry("source", "unsplash"),
                Map.entry("id", "unsplash-1"),
                Map.entry("alt", "A mountain"),
                Map.entry("width", 2400),
                Map.entry("height", 1600),
                Map.entry("previewUrl", "https://images.unsplash.com/preview"),
                Map.entry("imageUrl", "https://images.unsplash.com/full"),
                Map.entry("downloadLocation",
                    "https://api.unsplash.com/photos/unsplash-1/download?ixid=tracking"),
                Map.entry("sourceUrl", "https://unsplash.com/photos/unsplash-1"),
                Map.entry("author", Map.of(
                    "name", "Alice", "url", "https://unsplash.com/@alice")),
                Map.entry("requiresPreparation", true)));
        assertThat(request.get().url().getPath()).isEqualTo("/search/photos");
        assertThat(request.get().url().getQuery())
            .contains("query=mountain", "page=1", "per_page=20");
    }

    @Test
    void shouldNormalizePexelsAndPixabayResults() {
        var pexels = provider("""
            {"total_results": 1, "photos": [{
              "id": 7, "width": 1200, "height": 800, "alt": "A lake",
              "url": "https://pexels.com/photo/7",
              "photographer": "Bob", "photographer_url": "https://pexels.com/@bob",
              "src": {"medium": "https://images.pexels.com/preview", "original": "https://images.pexels.com/original"}
            }]}
            """);
        var pixabay = provider("""
            {"totalHits": 1, "hits": [{
              "id": 8, "imageWidth": 900, "imageHeight": 600, "tags": "forest, trees",
              "previewURL": "https://cdn.pixabay.com/preview", "largeImageURL": "https://pixabay.com/large",
              "pageURL": "https://pixabay.com/photos/8", "user": "Carol"
            }]}
            """);

        assertThat(firstItem(search(pexels, "pexels")))
            .containsEntry("id", "7")
            .containsEntry("imageUrl", "https://images.pexels.com/original")
            .containsEntry("requiresPreparation", false);
        assertThat(firstItem(search(pixabay, "pixabay")))
            .containsEntry("id", "8")
            .containsEntry("imageUrl", "https://pixabay.com/large")
            .containsEntry("requiresPreparation", false);
    }

    @Test
    void shouldPrepareUnsplashDownload() {
        var request = new AtomicReference<ClientRequest>();
        var provider = provider("{\"url\":\"https://images.unsplash.com/download\"}", request);

        var result = tool(provider, "prepare_unsplash_download").handler()
            .execute(new McpToolInvocation(
                "prepare_unsplash_download", Map.of(
                    "id", "unsplash-1",
                    "downloadLocation",
                    "https://api.unsplash.com/photos/unsplash-1/download?ixid=tracking")))
            .block();

        assertThat(result.structuredContent()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "source", "unsplash",
            "id", "unsplash-1",
            "url", "https://images.unsplash.com/download"));
        assertThat(request.get().url().toString())
            .isEqualTo("https://api.unsplash.com/photos/unsplash-1/download?ixid=tracking");
    }

    @Test
    void shouldRejectUnsupportedSizeAndUnsplashDownloadLocation() {
        var provider = provider("{}");

        assertThatThrownBy(() -> tool(provider, "search_images").handler()
            .execute(new McpToolInvocation("search_images", Map.of(
                "source", "pixabay", "query", "nature", "size", 2)))
            .block())
            .isInstanceOf(McpToolException.class)
            .hasMessageContaining("size is outside the supported range");
        assertThatThrownBy(() -> tool(provider, "prepare_unsplash_download").handler()
            .execute(new McpToolInvocation("prepare_unsplash_download", Map.of(
                "id", "unsplash-1",
                "downloadLocation", "https://example.com/photos/unsplash-1/download")))
            .block())
            .isInstanceOf(McpToolException.class)
            .hasMessageContaining("downloadLocation must match the selected Unsplash photo");
    }

    @Test
    void shouldNotLoadProviderWithoutMcpServerApi() {
        new ApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader("run.halo.mcpserver.api"))
            .withUserConfiguration(McpToolConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean("imageStreamMcpToolProvider"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstItem(Map<String, Object> result) {
        return (Map<String, Object>) ((List<?>) result.get("items")).getFirst();
    }

    private static Map<String, Object> search(ImageStreamMcpToolProvider provider, String source) {
        return tool(provider, "search_images").handler()
            .execute(new McpToolInvocation(
                "search_images", Map.of("source", source, "query", "nature")))
            .block()
            .structuredContent();
    }

    private static McpToolDefinition tool(ImageStreamMcpToolProvider provider, String name) {
        return provider.tools().filter(tool -> tool.name().equals(name)).blockFirst();
    }

    private static ImageStreamMcpToolProvider provider(String body) {
        return provider(body, new AtomicReference<>());
    }

    private static ImageStreamMcpToolProvider provider(
        String body, AtomicReference<ClientRequest> request) {
        ExchangeFunction exchange = clientRequest -> {
            request.set(clientRequest);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build());
        };
        var factory = mock(WebClientFactory.class);
        when(factory.getWebClient(any())).thenReturn(WebClient.builder()
            .exchangeFunction(exchange)
            .build());
        return new ImageStreamMcpToolProvider(factory);
    }
}
