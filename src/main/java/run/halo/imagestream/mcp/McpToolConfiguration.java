package run.halo.imagestream.mcp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.halo.imagestream.client.WebClientFactory;

@Configuration
@ConditionalOnClass(name = "run.halo.mcpserver.api.McpToolProvider")
public class McpToolConfiguration {

    @Bean
    ImageStreamMcpToolProvider imageStreamMcpToolProvider(WebClientFactory webClientFactory) {
        return new ImageStreamMcpToolProvider(webClientFactory);
    }
}
