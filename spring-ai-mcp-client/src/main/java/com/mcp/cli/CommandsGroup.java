package com.mcp.cli;

import java.util.Optional;
import java.util.stream.Stream;

import com.mcp.services.McpClientService;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.context.InteractionMode;

@Command
public class CommandsGroup {

    private final McpClientService mcpClientService;

    public CommandsGroup(com.mcp.services.McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    @Command(command = "mcp", interactionMode = InteractionMode.INTERACTIVE, description = "Execute MCP commands")
    public void mcp(@Option(arity = CommandRegistration.OptionArity.ZERO_OR_MORE) final String args) {
        final String question = argsToPrompt(args);

        mcpClientService.getResponse(question);
    }

    /**
     * Replace commas with spaces (for hands-on fluency).
     *
     * @param argsCommaSeparated args separated with commas
     * @return prompt
     */
    private String argsToPrompt(final String argsCommaSeparated) {
        return Optional.ofNullable(argsCommaSeparated)
                .map(args -> args.replace(',', ' '))
                .orElse("");
    }

}
