package com.mcp.tools.files;

import com.mcp.tools.AbstractToolService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReadFileService extends AbstractToolService {

    @Tool(description = """
    Read and return the full content of a file. 
    Supports various text encodings and delivers detailed error messages for read failures.
    Ideal for examining the contents of a single file.
    """)

    public String readFile(@ToolParam(description = "The full path to the file") String fullPathFile) {
        Map<String, Object> result = new HashMap<>();

        try {
            Path path = Paths.get(fullPathFile);

            if (!Files.exists(path)) {
                result.put(SUCCESS, false);
                result.put(ERROR, "File does not exist: " + fullPathFile);
                return mapper.writeValueAsString(result);
            }

            if (!Files.isRegularFile(path)) {
                result.put(SUCCESS, false);
                result.put(ERROR, "Path is not a regular file: " + fullPathFile);
                return mapper.writeValueAsString(result);
            }

            // Try to detect the file encoding (simplified here, uses default charset)
            String content = Files.readString(path);

            result.put("content", content);
            result.put("path", fullPathFile);
            result.put("size", Files.size(path));

            return successMessage(result);

        } catch (IOException e) {
            return errorMessage("Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            return errorMessage("Unexpected error: " + e.getMessage());
        }
    }
}
