package run.halo.imagestream.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.imagestream.client.ClientUtils;
import run.halo.imagestream.client.WebClientFactory;
import run.halo.imagestream.client.WebClientType;
import run.halo.mcpserver.api.McpToolAnnotations;
import run.halo.mcpserver.api.McpToolDefinition;
import run.halo.mcpserver.api.McpToolException;
import run.halo.mcpserver.api.McpToolInvocation;
import run.halo.mcpserver.api.McpToolProvider;
import run.halo.mcpserver.api.McpToolResult;

public class ImageStreamMcpToolProvider implements McpToolProvider {

    private static final int DEFAULT_SIZE = 20;
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 30;

    private final WebClientFactory webClientFactory;

    public ImageStreamMcpToolProvider(WebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }

    @Override
    public Flux<McpToolDefinition> tools() {
        return Flux.just(searchImagesTool(), prepareUnsplashDownloadTool());
    }

    private McpToolDefinition searchImagesTool() {
        return McpToolDefinition.builder()
            .name("search_images")
            .title("Search stock images")
            .description("Search Unsplash, Pexels, or Pixabay and return normalized image metadata. "
                + "Unsplash results must be passed to prepare_unsplash_download before transfer.")
            .displayTitle("搜索图库图片")
            .displayDescription("搜索 Unsplash、Pexels 或 Pixabay，返回适合 AI Agent 使用的统一图片信息。")
            .inputSchema(objectSchema(Map.of(
                "source", Map.of(
                    "type", "string", "enum", List.of("unsplash", "pexels", "pixabay")),
                "query", Map.of("type", "string", "minLength", 1, "maxLength", 100),
                "page", Map.of("type", "integer", "minimum", 1, "default", 1),
                "size", Map.of(
                    "type", "integer", "minimum", MIN_SIZE, "maximum", MAX_SIZE,
                    "default", DEFAULT_SIZE)), List.of("source", "query")))
            .outputSchema(searchOutputSchema())
            .annotations(new McpToolAnnotations(true, false, true, true, "Search stock images"))
            .permission(ignored -> Mono.just(true))
            .handler(this::searchImages)
            .build();
    }

    private McpToolDefinition prepareUnsplashDownloadTool() {
        return McpToolDefinition.builder()
            .name("prepare_unsplash_download")
            .title("Prepare an Unsplash download")
            .description("Register an Unsplash download for a selected search result and return "
                + "the URL that can be passed to Halo's attachment URL transfer tool.")
            .displayTitle("准备 Unsplash 下载")
            .displayDescription("为选中的 Unsplash 图片登记下载，并返回可用于附件转存的链接。")
            .inputSchema(objectSchema(Map.of(
                    "id", Map.of("type", "string", "minLength", 1, "maxLength", 100),
                    "downloadLocation", Map.of("type", "string", "format", "uri")),
                List.of("id", "downloadLocation")))
            .outputSchema(objectSchema(Map.of(
                "source", Map.of("type", "string", "const", "unsplash"),
                "id", Map.of("type", "string"),
                "url", Map.of("type", "string", "format", "uri")),
                List.of("source", "id", "url")))
            .annotations(new McpToolAnnotations(
                false, false, false, true, "Prepare an Unsplash download"))
            .permission(ignored -> Mono.just(true))
            .handler(this::prepareUnsplashDownload)
            .build();
    }

    private Mono<McpToolResult> searchImages(McpToolInvocation invocation) {
        return Mono.defer(() -> {
            var arguments = invocation.arguments();
            var source = source(arguments.get("source"));
            var query = requiredString(arguments, "query");
            var page = integer(arguments, "page", 1);
            var size = integer(arguments, "size", DEFAULT_SIZE);
            return search(source, query, page, size)
                .map(body -> McpToolResult.success(searchPayload(source, body, page, size)));
        });
    }

    private Mono<McpToolResult> prepareUnsplashDownload(McpToolInvocation invocation) {
        return Mono.defer(() -> {
            var id = requiredString(invocation.arguments(), "id");
            var downloadLocation = unsplashDownloadLocation(
                requiredString(invocation.arguments(), "downloadLocation"), id);
            return client(WebClientType.UNSPLASH).get()
                .uri(downloadLocation)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(ClientUtils::parseJsonNode)
                .switchIfEmpty(Mono.error(unavailable("Unsplash returned an empty response", null)))
                .map(body -> {
                    var url = text(body, "url");
                    if (url == null) {
                        throw unavailable("Unsplash did not return a download URL", null);
                    }
                    return McpToolResult.success(Map.of(
                        "source", "unsplash", "id", id, "url", url));
                })
                .onErrorMap(error -> !(error instanceof McpToolException),
                    error -> unavailable("Unable to prepare the Unsplash download", error));
        });
    }

    private Mono<JsonNode> search(Source source, String query, int page, int size) {
        var request = client(source.clientType()).get();
        Mono<JsonNode> response = (switch (source) {
            case UNSPLASH -> request.uri(uriBuilder -> uriBuilder.path("/search/photos")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("per_page", size)
                .build());
            case PEXELS -> request.uri(uriBuilder -> uriBuilder.path("/search")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("per_page", size)
                .build());
            case PIXABAY -> request.uri(uriBuilder -> uriBuilder.path("/")
                .queryParam("q", query)
                .queryParam("page", page)
                .queryParam("per_page", size)
                .queryParam("safesearch", true)
                .build());
        }).retrieve().bodyToMono(String.class).flatMap(ClientUtils::parseJsonNode);
        return response
            .switchIfEmpty(Mono.error(unavailable("The image source returned an empty response", null)))
            .onErrorMap(error -> !(error instanceof McpToolException),
                error -> unavailable("Unable to search " + source.id(), error));
    }

    private WebClient client(WebClientType type) {
        var client = webClientFactory.getWebClient(type);
        if (client == null) {
            throw unavailable("The image source is not configured", null);
        }
        return client;
    }

    private static Map<String, Object> searchPayload(
        Source source, JsonNode body, int page, int size) {
        var itemsNode = body.path(source.itemsField());
        var items = new ArrayList<Map<String, Object>>();
        if (itemsNode.isArray()) {
            itemsNode.forEach(node -> {
                var item = item(source, node);
                if (item != null) {
                    items.add(item);
                }
            });
        }
        var payload = new LinkedHashMap<String, Object>();
        payload.put("source", source.id());
        payload.put("page", page);
        payload.put("size", size);
        payload.put("total", body.path(source.totalField()).asLong(items.size()));
        payload.put("items", items);
        return payload;
    }

    private static Map<String, Object> item(Source source, JsonNode node) {
        var id = text(node, "id");
        var previewUrl = switch (source) {
            case UNSPLASH -> text(node, "urls", "small");
            case PEXELS -> text(node, "src", "medium");
            case PIXABAY -> text(node, "previewURL");
        };
        var imageUrl = switch (source) {
            case UNSPLASH -> text(node, "urls", "full");
            case PEXELS -> text(node, "src", "original");
            case PIXABAY -> text(node, "largeImageURL");
        };
        if (id == null || previewUrl == null || imageUrl == null) {
            return null;
        }

        var item = new LinkedHashMap<String, Object>();
        item.put("source", source.id());
        item.put("id", id);
        put(item, "alt", switch (source) {
            case UNSPLASH -> first(text(node, "alt_description"), text(node, "description"));
            case PEXELS -> text(node, "alt");
            case PIXABAY -> text(node, "tags");
        });
        put(item, "width", integer(node, source == Source.PIXABAY ? "imageWidth" : "width"));
        put(item, "height", integer(node, source == Source.PIXABAY ? "imageHeight" : "height"));
        item.put("previewUrl", previewUrl);
        item.put("imageUrl", imageUrl);
        if (source == Source.UNSPLASH) {
            put(item, "downloadLocation", text(node, "links", "download_location"));
        }
        put(item, "sourceUrl", switch (source) {
            case UNSPLASH -> text(node, "links", "html");
            case PEXELS -> text(node, "url");
            case PIXABAY -> text(node, "pageURL");
        });
        var author = author(source, node);
        if (!author.isEmpty()) {
            item.put("author", author);
        }
        item.put("requiresPreparation", source == Source.UNSPLASH);
        return item;
    }

    private static Map<String, Object> author(Source source, JsonNode node) {
        var author = new LinkedHashMap<String, Object>();
        switch (source) {
            case UNSPLASH -> {
                put(author, "name", text(node, "user", "name"));
                put(author, "url", text(node, "user", "links", "html"));
            }
            case PEXELS -> {
                put(author, "name", text(node, "photographer"));
                put(author, "url", text(node, "photographer_url"));
            }
            case PIXABAY -> put(author, "name", text(node, "user"));
        }
        return author;
    }

    private static Map<String, Object> searchOutputSchema() {
        var author = objectSchema(Map.of(
            "name", Map.of("type", "string"),
            "url", Map.of("type", "string", "format", "uri")), List.of());
        var itemProperties = new LinkedHashMap<String, Object>();
        itemProperties.put("source", Map.of(
            "type", "string", "enum", List.of("unsplash", "pexels", "pixabay")));
        itemProperties.put("id", Map.of("type", "string"));
        itemProperties.put("alt", Map.of("type", "string"));
        itemProperties.put("width", Map.of("type", "integer", "minimum", 0));
        itemProperties.put("height", Map.of("type", "integer", "minimum", 0));
        itemProperties.put("previewUrl", Map.of("type", "string", "format", "uri"));
        itemProperties.put("imageUrl", Map.of("type", "string", "format", "uri"));
        itemProperties.put("downloadLocation", Map.of("type", "string", "format", "uri"));
        itemProperties.put("sourceUrl", Map.of("type", "string", "format", "uri"));
        itemProperties.put("author", author);
        itemProperties.put("requiresPreparation", Map.of("type", "boolean"));
        var item = objectSchema(itemProperties,
            List.of("source", "id", "previewUrl", "imageUrl", "requiresPreparation"));
        return objectSchema(Map.of(
            "source", Map.of(
                "type", "string", "enum", List.of("unsplash", "pexels", "pixabay")),
            "page", Map.of("type", "integer", "minimum", 1),
            "size", Map.of("type", "integer", "minimum", 1, "maximum", MAX_SIZE),
            "total", Map.of("type", "integer", "minimum", 0),
            "items", Map.of("type", "array", "items", item)),
            List.of("source", "page", "size", "total", "items"));
    }

    private static Map<String, Object> objectSchema(
        Map<String, Object> properties, List<String> required) {
        var schema = new LinkedHashMap<String, Object>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }

    private static Source source(Object value) {
        if (!(value instanceof String text)) {
            throw invalid("source must be a string");
        }
        try {
            return Source.valueOf(text.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw invalid("source must be unsplash, pexels, or pixabay");
        }
    }

    private static String requiredString(Map<String, Object> arguments, String name) {
        var value = arguments.get(name);
        if (!(value instanceof String text) || text.isBlank()) {
            throw invalid(name + " must be a non-empty string");
        }
        return text.strip();
    }

    private static int integer(Map<String, Object> arguments, String name, int defaultValue) {
        var value = arguments.get(name);
        if (value == null) {
            return defaultValue;
        }
        if (!(value instanceof Number number)
            || number.doubleValue() != number.intValue()
            || number.intValue() < 1
            || ("size".equals(name) && number.intValue() < MIN_SIZE)
            || ("size".equals(name) && number.intValue() > MAX_SIZE)) {
            throw invalid(name + " is outside the supported range");
        }
        return number.intValue();
    }

    private static URI unsplashDownloadLocation(String value, String id) {
        try {
            var uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                || !"api.unsplash.com".equalsIgnoreCase(uri.getHost())
                || uri.getPort() != -1
                || uri.getUserInfo() != null
                || uri.getFragment() != null
                || !("/photos/" + id + "/download").equals(uri.getPath())) {
                throw invalid("downloadLocation must match the selected Unsplash photo");
            }
            return uri;
        } catch (IllegalArgumentException error) {
            throw invalid("downloadLocation must be a valid Unsplash download URL");
        }
    }

    private static Integer integer(JsonNode node, String field) {
        var value = node.path(field);
        return value.isIntegralNumber() ? value.intValue() : null;
    }

    private static String text(JsonNode node, String... path) {
        var value = node;
        for (var segment : path) {
            value = value.path(segment);
        }
        if (value.isTextual()) {
            var text = value.textValue();
            return text == null || text.isBlank() ? null : text;
        }
        return value.isNumber() ? value.asText() : null;
    }

    private static String first(String first, String second) {
        return first == null ? second : first;
    }

    private static void put(Map<String, Object> values, String key, Object value) {
        if (value != null) {
            values.put(key, value);
        }
    }

    private static McpToolException invalid(String message) {
        return new McpToolException("INVALID_ARGUMENT", message);
    }

    private static McpToolException unavailable(String message, Throwable cause) {
        return cause == null
            ? new McpToolException("IMAGE_SOURCE_UNAVAILABLE", message)
            : new McpToolException("IMAGE_SOURCE_UNAVAILABLE", message, cause);
    }

    private enum Source {
        UNSPLASH(WebClientType.UNSPLASH, "unsplash", "results", "total"),
        PEXELS(WebClientType.PEXELS, "pexels", "photos", "total_results"),
        PIXABAY(WebClientType.PIXABAY, "pixabay", "hits", "totalHits");

        private final WebClientType clientType;
        private final String id;
        private final String itemsField;
        private final String totalField;

        Source(WebClientType clientType, String id, String itemsField, String totalField) {
            this.clientType = clientType;
            this.id = id;
            this.itemsField = itemsField;
            this.totalField = totalField;
        }

        WebClientType clientType() {
            return clientType;
        }

        String id() {
            return id;
        }

        String itemsField() {
            return itemsField;
        }

        String totalField() {
            return totalField;
        }
    }
}
