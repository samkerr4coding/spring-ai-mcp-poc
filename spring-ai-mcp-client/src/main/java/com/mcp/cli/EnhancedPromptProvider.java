package com.mcp.cli;

import org.jline.utils.AttributedString;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

/**
 * This class provides a custom prompt for the command line interface.
 * It implements the PromptProvider interface from the Spring Shell framework.
 */
@Component
public class EnhancedPromptProvider implements PromptProvider {

    // Define the prompt text as a constant to avoid magic strings and facilitate changes
    private static final String PROMPT_TEXT = "Enter command> ";

    /**
     * Generates the custom prompt for the CLI.
     * @return An AttributedString that represents the prompt to be displayed.
     */
    @Override
    public AttributedString getPrompt() {
        return new AttributedString(PROMPT_TEXT);
    }
}