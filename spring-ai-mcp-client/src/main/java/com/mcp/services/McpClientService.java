package com.mcp.services;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class McpClientService {

    private final ChatClient chatClient;

    public McpClientService(ChatClient.Builder chatClientBuilder) {
        McpSyncClient mcpClient = stdioClient();
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClient))
                .build();
    }

    public Stream<String> getResponse(String question) {
        OllamaOptions options = OllamaOptions.builder()
                .model("qwen3:1.7b")
                .temperature(0.1)
                .build();

        System.out.println("Preparing the answer...");

        String response = chatClient.prompt()
                .options(options)
                .user(question)
                .call()
                .content();

        System.out.println("reponse : " + response);

        return Stream.of(response);
    }

    public McpSyncClient stdioClient() {
        var stdioParams = ServerParameters.builder("java")
                .args("-Dspring.ai.mcp.server.stdio=true", "-Dspring.main.web-application-type=none",
                        "-Dlogging.pattern.console=", "-jar",
                        "C://Users//B661LP//git//mcp-server-spring-workshop//spring-ai-mcp-server//target//spring-ai-mcp-server-0.0.1.jar")
                .build();

        var transport = new StdioClientTransport(stdioParams);
        var client = McpClient.sync(transport).build();

        client.initialize();

        // List and demonstrate tools
        McpSchema.ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);
        return client;
    }
}
