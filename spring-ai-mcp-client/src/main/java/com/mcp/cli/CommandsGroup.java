package com.mcp.cli;

import java.util.Optional;

import com.mcp.services.McpClientService;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.context.InteractionMode;

/**
 * This class defines a group of commands under the 'mcp' namespace.
 * It handles the interaction with the McpClientService to process commands.
 */
@Command
public class CommandsGroup {

    // Service to handle MCP client operations
    private final McpClientService mcpClientService;

    /**
     * Constructor to inject the McpClientService dependency.
     * @param mcpClientService the service to handle MCP commands
     */
    public CommandsGroup(McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    /**
     * Command method to execute MCP commands interactively.
     * It converts the input arguments into a space-separated string and fetches the response.
     * @param args the input arguments for the command, expected to be comma-separated
     */
    @Command(command = "mcp", interactionMode = InteractionMode.INTERACTIVE, description = "Execute MCP commands")
    public void executeMcpCommand(@Option(arity = CommandRegistration.OptionArity.ZERO_OR_MORE) final String args) {
        final String formattedInput = formatInputArgs(args);
        mcpClientService.getResponse(formattedInput);
    }

    /**
     * Formats the input arguments by replacing commas with spaces for better readability.
     * This helps in processing the command as a single string.
     * @param argsCommaSeparated the comma-separated arguments
     * @return a space-separated string of arguments
     */
    private String formatInputArgs(final String argsCommaSeparated) {
        return Optional.ofNullable(argsCommaSeparated)
                .map(args -> args.replace(',', ' '))
                .orElse("");
    }

}