package com.mcp.tools.files;

import com.mcp.tools.AbstractToolService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchPatternInFilesService extends AbstractToolService {

    public static final int DEFAULT_MAX_RESULTS = 100;

    @Tool(description = """
        Search files for text patterns, returning matches with line numbers and context.
        Functions like 'grep' with output tailored for LLM processing.
        """)
    public String grepFiles(
            @ToolParam(description = "The base directory to search in") String directory,
            @ToolParam(description = "The pattern to search for in file contents") String pattern,
            @ToolParam(description = "Optional file extension filter (e.g., '.java', '.txt')", required = false) String fileExtension,
            @ToolParam(description = "Whether to use regex for pattern matching", required = false) Boolean useRegex,
            @ToolParam(description = "Number of context lines to include before/after matches", required = false) Integer contextLines,
            @ToolParam(description = "Maximum number of results to return", required = false) Integer maxResults
    ) {
        Map<String, Object> result = new HashMap<>();
        List<String> matchingSummaries = new ArrayList<>();
        AtomicInteger totalMatches = new AtomicInteger(0);
        int filesWithMatches = 0;

        // Default values
        boolean useRegexValue = useRegex != null && useRegex;
        int contextLinesValue = contextLines != null ? contextLines : 0;
        int maxResultsValue = maxResults != null ? maxResults : DEFAULT_MAX_RESULTS;

        try {
            Path basePath = Paths.get(directory);

            if (!validateDirectory(basePath, result)) {
                return mapper.writeValueAsString(result);
            }

            Pattern searchPattern = compilePattern(pattern, useRegexValue);
            List<Path> filesToSearch = findFiles(basePath, fileExtension);

            for (Path filePath : filesToSearch) {
                if (totalMatches.get() >= maxResultsValue) break;
                processFile(filePath, searchPattern, matchingSummaries, contextLinesValue, maxResultsValue, totalMatches);
                filesWithMatches++;
            }

            finalizeResults(result, matchingSummaries, totalMatches, filesWithMatches, maxResultsValue);
            return successMessage(result);

        } catch (Exception e) {
            return errorMessage("Unexpected error: " + e.getMessage());
        }
    }

    private boolean validateDirectory(Path basePath, Map<String, Object> result) {
        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            result.put(SUCCESS, false);
            result.put(ERROR, "Directory does not exist or is not a directory: " + basePath);
            return false;
        }
        return true;
    }

    private Pattern compilePattern(String pattern, boolean useRegex) {
        String escapedPattern = useRegex ? pattern : Pattern.quote(pattern);
        return Pattern.compile(escapedPattern, Pattern.CASE_INSENSITIVE);
    }

    private List<Path> findFiles(Path basePath, String fileExtension) throws IOException {
        try (Stream<Path> paths = Files.walk(basePath)) {
            return paths.filter(path -> Files.isRegularFile(path) &&
                            (fileExtension == null || fileExtension.isEmpty() || path.toString().endsWith(fileExtension)))
                    .toList();
        }
    }

    private void processFile(Path filePath, Pattern searchPattern, List<String> matchingSummaries, int contextLinesValue, int maxResultsValue, AtomicInteger totalMatches) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            List<String> fileMatches = new ArrayList<>();

            for (int i = 0; i < lines.size(); i++) {
                if (searchPattern.matcher(lines.get(i)).find()) {
                    totalMatches.incrementAndGet();
                    fileMatches.add(formatMatch(filePath, lines, i, contextLinesValue));
                    if (totalMatches.get() >= maxResultsValue) break;
                }
            }

            if (!fileMatches.isEmpty()) {
                addFileMatchesToSummary(filePath, matchingSummaries, fileMatches);
            }
        } catch (IOException e) {
            // Skip files that can't be read
        }
    }

    private String formatMatch(Path filePath, List<String> lines, int matchLineIndex, int contextLinesValue) {
        StringBuilder matchInfo = new StringBuilder();
        matchInfo.append(filePath.getFileName()).append(":").append(matchLineIndex + 1).append(": ");

        if (contextLinesValue > 0) {
            matchInfo.append(formatContext(lines, matchLineIndex, contextLinesValue));
        } else {
            matchInfo.append(lines.get(matchLineIndex));
        }
        return matchInfo.toString();
    }

    private String formatContext(List<String> lines, int matchLineIndex, int contextLinesValue) {
        StringBuilder context = new StringBuilder();
        int startLine = Math.max(0, matchLineIndex - contextLinesValue);
        int endLine = Math.min(lines.size(), matchLineIndex + 1 + contextLinesValue);

        for (int j = startLine; j < endLine; j++) {
            if (j == matchLineIndex) {
                context.append("→ ");
            } else {
                context.append("  ");
            }
            context.append(lines.get(j)).append("\n");
        }
        if (endLine < lines.size()) {
            context.append("...");
        }
        return context.toString();
    }

    private void addFileMatchesToSummary(Path filePath, List<String> matchingSummaries, List<String> fileMatches) {
        matchingSummaries.add(String.format("%s (%d matches)", filePath.toString(), fileMatches.size()));
        matchingSummaries.addAll(fileMatches.subList(0, Math.min(fileMatches.size(), 5)));
        if (fileMatches.size() > 5) {
            matchingSummaries.add(String.format("... and %d more matches in this file", fileMatches.size() - 5));
        }
        matchingSummaries.add("---");
    }

    private void finalizeResults(Map<String, Object> result, List<String> matchingSummaries, AtomicInteger totalMatches, int filesWithMatches, int maxResultsValue) {
        if (!matchingSummaries.isEmpty() && matchingSummaries.get(matchingSummaries.size() - 1).equals("---")) {
            matchingSummaries.remove(matchingSummaries.size() - 1);
        }
        result.put("summary", String.format("Found %d matches in %d files", totalMatches.get(), filesWithMatches));
        result.put("results", matchingSummaries);
        result.put("limitReached", totalMatches.get() >= maxResultsValue);
    }
}
