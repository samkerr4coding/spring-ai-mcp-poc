# Spring AI MCP Proof of Concept

This project demonstrates a simple Proof of Concept (PoC) integrating Spring AI with the Model Context Protocol (MCP) to interact with Large Language Models (LLMs) like Qwen3 and Ollama. It provides a client-server architecture where the client interacts with the LLM through the MCP server, leveraging a set of tools exposed by the server.

## Overview

The project is structured as a Maven multi-module project, consisting of two main modules:

*   **`spring-ai-mcp-server`**: Implements the MCP server, exposing tools for file system operations, web page fetching, and command execution.
*   **`spring-ai-mcp-client`**: Implements a command-line interface (CLI) client for interacting with the MCP server and LLMs.

## Modules

### `spring-ai-mcp-server`

This module contains the implementation of the MCP server. It exposes a set of tools that can be invoked by the client to perform various tasks.

#### Key Features

*   **Tool-based Architecture**: Provides a set of tools for interacting with the file system, fetching web pages, and executing commands.
*   **Spring AI Integration**: Leverages Spring AI to interact with LLMs.
*   **MCP Server**: Implements the MCP server using `spring-ai-starter-mcp-server-webmvc`.

#### Tools

The server exposes the following tools:

*   **File System Operations**:
    *   `ReadFileService`: Reads the content of a file.
    *   `WriteFileService`: Writes content to a file.
    *   `EditFileService`: Edits a file by replacing text.
    *   `SearchFilesService`: Searches for files matching a pattern.
    *   `SearchPatternInFilesService`: Searches for a pattern within files.
    *   `ListDirectoryService`: Lists the contents of a directory.
    *   `CreateDirectoryService`: Creates a directory.
*   **Web Page Fetching**:
    *   `FetchWebpageService`: Fetches the content of a web page.
*   **Command Execution**:
    *   `BashService`: Executes Bash commands.
    *   `PowerShellService`: Executes PowerShell commands.

#### Configuration

The server is configured using `application.yml`, which defines the server name, version, and type.

### `spring-ai-mcp-client`

This module contains the implementation of the command-line interface (CLI) client. It allows users to interact with the MCP server and LLMs.

#### Key Features

*   **Spring Shell**: Provides an interactive command-line interface using Spring Shell.
*   **Ollama Integration**: Integrates with the Ollama LLM using `spring-ai-starter-model-ollama`.
*   **MCP Client**: Implements the MCP client using `spring-ai-starter-mcp-client`.

#### Commands

The client provides the following commands:

*   `mcp`: Executes MCP commands by sending prompts to the LLM via the MCP server.
*   `exit`: Exits the shell.

#### Configuration

The client is configured using `application.yml`, which defines the Ollama base URL, application name, banner location, and shell interaction settings.

## Getting Started

### Prerequisites

*   Java 21
*   Maven
*   Ollama (optional, if using Ollama LLM)
*   Qwen3 model (or any other compatible LLM)

### Building the Project

1.  Clone the repository:

    ```bash
    git clone <repository_url>
    cd spring-ai-mcp-poc
    ```

2.  Build the project using Maven:

    ```bash
    ./mvnw clean install -DskipTests
    ```

### Running the Server

1.  Navigate to the `spring-ai-mcp-server` directory:

    ```bash
    cd spring-ai-mcp-server
    ```

2.  Run the server using Spring Boot:

    ```bash
    ./mvnw spring-boot:run
    ```

### Running the Client

1.  Navigate to the `spring-ai-mcp-client` directory:

    ```bash
    cd spring-ai-mcp-client
    ```

2.  Run the client using Spring Boot:

    ```bash
    ./mvnw spring-boot:run
    ```

3.  Interact with the LLM using the `mcp` command:

    ```bash
    $> mcp What is the capital of France?
    ```

## Configuration

### Server Configuration

The server is configured using `spring-ai-mcp-server/src/main/resources/application.yml`.

```yaml
spring:
  ai:
    mcp:
      server:
        name: mcp-server-spring-workshop
        version: 0.0.1
        type: SYNC
  main:
    banner-mode: off
    web-application-type: none
logging:
  pattern:
    console:
    file: ./target/mcp-server-spring-workshop.log

```

### Client Configuration

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
  application:
    name: LLM CLI
  banner:
    location: classpath:banner.txt
  main:
    log-startup-info: false
  threads:
    virtual:
      enabled: true
  shell:
    interactive:
      enabled: true
    command:
      history.enabled: false
      script.enabled: false
      version.enabled: false
    theme:
      name: default

logging.level:
  root: ERROR
  org.jline: off

```

##  Testing
The project includes unit tests for both the client and server modules. To run the tests, use the following command:

```bash
./mvnw test
```

##  Contributing
Contributions are welcome! Please feel free to submit pull requests or open issues to suggest improvements or report bugs.

##  License
This project is licensed under the Apache License 2.0.