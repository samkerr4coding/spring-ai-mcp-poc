package com.mcp.filesystem;

import com.mcp.tools.cmd.BashService;
import com.mcp.tools.files.*;
import com.mcp.tools.folder.CreateDirectoryService;
import com.mcp.tools.folder.ListDirectoryService;
import com.mcp.tools.web.FetchWebpageService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider mcpServices(SearchFilesService searchFilesService,
											ReadFileService readFileService,
											EditFileService editFileService,
											ListDirectoryService listDirectoryService,
											WriteFileService writeFileService,
											CreateDirectoryService createDirectoryService,
											SearchPatternInFilesService searchPatternInFilesService,
											FetchWebpageService fetchWebpageService,
											BashService bashService) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(searchFilesService,
						listDirectoryService,
						editFileService,
						readFileService,
						writeFileService,
						createDirectoryService,
						searchPatternInFilesService,
						fetchWebpageService,
						bashService)
				.build();
	}
}
