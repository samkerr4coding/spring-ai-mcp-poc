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

@Service
public class BashService extends AbstractToolService {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final Set<String> DISALLOWED_COMMANDS = Set.of("rm", "rmdir", "mv", "del", "erase", "dd", "mkfs", "format");

    @Tool(description = """
    Run a Bash command and return its output.\s
    Caution: Avoid using commands that alter or delete system files.\s
   \s""")
    public String executeBash(@ToolParam(description = "Bash command to execute") String command,
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
            return successMessage(result);

        } catch (IOException e) {
            return errorMessage("IO error: " + e.getMessage());
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
        String commandName = command.split(" ")[0];
        return DISALLOWED_COMMANDS.contains(commandName);
    }

    private ProcessBuilder configureProcessBuilder(String command, String workingDirectory) {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
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
        result.put("exitCode", process.exitValue());
        result.put("output", String.join("\n", outputLines));
        return result;
    }
}
