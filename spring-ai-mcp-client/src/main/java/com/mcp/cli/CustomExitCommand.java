package com.mcp.cli;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.context.InteractionMode;
import org.springframework.shell.standard.commands.Quit;

/**
 * Custom command to gracefully exit the application.
 */
@Command
public class CustomExitCommand implements Quit.Command {

    /**
     * Custom exit command that terminates the application.
     * This method overrides the default exit command to provide a farewell message.
     */
    @Command(command = "exit", interactionMode = InteractionMode.INTERACTIVE, description = "Terminate the shell session")
    public void exit() {
        System.out.println("Farewell!");
        System.exit(0);
    }
}