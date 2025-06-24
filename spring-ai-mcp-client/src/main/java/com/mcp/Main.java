package com.mcp;

import com.mcp.cli.CommandsGroup;
import com.mcp.cli.CustomExitCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.EnableCommand;

@SpringBootApplication
@EnableCommand({ CommandsGroup.class, CustomExitCommand.class})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}