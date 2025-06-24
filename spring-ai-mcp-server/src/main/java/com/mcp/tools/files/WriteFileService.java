package com.mcp.tools.files;

import com.mcp.tools.AbstractToolService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class WriteFileService extends AbstractToolService {

    @Tool(description = """
    Create or overwrite a file with new text content. 
    Caution: Existing files will be overwritten without notice.
    Supports proper text encoding.
    """)

    public String writeFile(
            @ToolParam(description = "The path to the file to create or overwrite") String path,
            @ToolParam(description = "The content to write to the file") String content
    ) {
        Map<String, Object> result = new HashMap<>();

        try {
            Path filePath = Paths.get(path);

            // Create parent directories if they don't exist
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
                result.put("createdDirectories", parent.toString());
            }

            // Write the content to the file
            boolean fileExisted = Files.exists(filePath);
            Files.writeString(filePath, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            result.put("path", path);
            result.put("bytesWritten", content.getBytes(StandardCharsets.UTF_8).length);
            result.put("action", fileExisted ? "overwritten" : "created");

            return successMessage(result);

        } catch (IOException e) {
            return errorMessage("Failed to write file: " + e.getMessage());
        } catch (Exception e) {
            return errorMessage("Failed to serialize error result");
        }
    }
}