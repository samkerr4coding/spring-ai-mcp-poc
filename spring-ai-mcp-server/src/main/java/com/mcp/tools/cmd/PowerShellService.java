package com.mcp.tools.cmd;

import com.mcp.tools.AbstractToolService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.springframework.ai.model.ModelOptionsUtils.OBJECT_MAPPER;

@Service
public class PowerShellService extends AbstractToolService {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final Set<String> DISALLOWED_COMMANDS = Set.of(
            "Remove-Item", "Move-Item", "Format-Volume", "Stop-Process", "Stop-Service", "Clear-Content",
            "rm", "del", "erase", "rd", "ri", "mv", "move", "clc",
            "rmdir", "dd", "mkfs", "format"
    );

    @Tool(description = """
    Run a PowerShell command and return its output.
    Note: Avoid using commands that alter or delete system files.
    """)
    public String executePowerShell(@ToolParam(description = "PowerShell command to execute") String command,
                                    @ToolParam(description = "Working directory (optional)", required = false) String workingDirectory,
                                    @ToolParam(description = "Command timeout in seconds (default: 30)", required = false) Integer timeoutSeconds) {

        if (isInvalidCommand(command)) {
            return errorMessage("Invalid or disallowed command.");
        }

        try {
            ProcessBuilder processBuilder = configureProcessBuilder(command, workingDirectory);
            Process process = startProcess(processBuilder, timeoutSeconds);

            if (process == null) {
                return errorMessage("Command execution timed out.");
            }

            Map<String, Object> result = captureProcessOutput(command, process);
            return evaluateResult(result, process.exitValue());

        } catch (IOException e) {
            return handleIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorMessage("Execution interrupted: " + e.getMessage());
        } catch (Exception e) {
            return errorMessage("Unexpected error: " + e.getMessage());
        }
    }

    private boolean isInvalidCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return true;
        }
        String firstToken = Arrays.stream(command.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse("");
        return DISALLOWED_COMMANDS.stream().anyMatch(s -> s.equalsIgnoreCase(firstToken));
    }

    private ProcessBuilder configureProcessBuilder(String command, String workingDirectory) {
        ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-NonInteractive", "-NoProfile", "-Command", command);
        if (workingDirectory != null && !workingDirectory.trim().isEmpty()) {
            processBuilder.directory(new java.io.File(workingDirectory));
        }
        processBuilder.redirectErrorStream(true);
        return processBuilder;
    }

    private Process startProcess(ProcessBuilder processBuilder, Integer timeoutSeconds) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        int timeout = Optional.ofNullable(timeoutSeconds).filter(t -> t > 0).orElse(DEFAULT_TIMEOUT_SECONDS);
        boolean completed = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            return null;
        }
        return process;
    }

    private Map<String, Object> captureProcessOutput(String command, Process process) throws IOException {
        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("command", command);
        result.put("output", String.join("\n", outputLines));
        return result;
    }

    private String evaluateResult(Map<String, Object> result, int exitCode) {
        if (exitCode == 0) {
            return successMessage(result);
        } else {
            return errorMessage("Command failed with exit code " + exitCode, result);
        }
    }

    private String handleIOException(IOException e) {
        if (e.getMessage().contains("Cannot run program \"powershell.exe\"")) {
            return errorMessage("IO error: 'powershell.exe' not found. Ensure PowerShell is installed and in the system's PATH.");
        }
        return errorMessage("IO error: " + e.getMessage());
    }

    private String errorMessage(String message, Map<String, Object> results) {
        results.put("success", false);
        results.put("error", message);
        try {
            return OBJECT_MAPPER.writeValueAsString(results);
        } catch (Exception e) {
            return "{\"success\":false, \"error\":\"Failed to serialize error message: " + e.getMessage() + "\"}";
        }
    }
}
