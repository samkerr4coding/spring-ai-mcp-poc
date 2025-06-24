package com.mcp.tools.web;

import com.mcp.tools.AbstractToolService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FetchWebpageService extends AbstractToolService {

    private static final int DEFAULT_TIMEOUT_MS = 10000;

    @Tool(description = """
        Retrieve HTML content from a specified URL using jsoup. 
        Options include setting a connection timeout and extracting webpage text content.
        """)
    public String fetchWebpage(
            @ToolParam(description = "Webpage URL to retrieve") String url,
            @ToolParam(description = "Connection timeout in milliseconds (default: 10000)", required = false) Integer timeoutMs) {

        int timeout = (timeoutMs != null) ? timeoutMs : DEFAULT_TIMEOUT_MS;

        try {
            Document doc = Jsoup.connect(url)
                    .timeout(timeout)
                    .get();

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("content", doc.text());
            result.put("title", doc.title());

            return successMessage(result);

        } catch (Exception e) {
            return errorMessage("Failed to access URL: " + url + ". Error: " + e.getMessage());
        }
    }
}
