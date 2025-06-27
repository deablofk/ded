# Deditor - A Modern Text Editor

## Overview

Deditor is a modern, feature-rich text editor built with Java. It provides a sleek interface with advanced code editing capabilities, including syntax highlighting, code intelligence through Language Server Protocol (LSP) integration, and a customizable environment.

## Features

- **Modern UI**: Clean, customizable interface with a focus on code readability
- **Split Views**: Support for both horizontal and vertical split views for multi-file editing
- **Syntax Highlighting**: Advanced syntax highlighting using TreeSitter for multiple programming languages
- **Code Intelligence**: Integration with Language Server Protocol (LSP) for features like:
  - Code completion
  - Error checking
  - Go to definition
  - Find references
- **File Management**:
  - Built-in file explorer
  - Fuzzy finder (FZF) for quick file navigation
- **Package Management**: Integrated package manager for language servers and other tools
- **Customization**: Extensive configuration options via TOML files
  - Custom themes
  - Font settings
  - Cursor behavior
  - Syntax highlighting colors

## Dependencies

Deditor relies on the following libraries:

- **LWJGL (Lightweight Java Game Library)**: For graphics and input handling
- **Skija**: For 2D graphics rendering
- **JTreeSitter**: For syntax highlighting
- **TOML4J**: For configuration parsing
- **LSP4J**: For Language Server Protocol support

## Installation and Setup

### Prerequisites

- Java 21 or higher
- Gradle 7.0 or higher

### Building from Source

1. Clone the repository:
   ```
   git clone https://github.com/deablofk/ded.git
   cd ded
   ```

2. Build the project using Gradle:
   ```
   ./gradlew build
   ```

### Running Deditor

Run the editor using the Gradle application plugin:
```
./gradlew run
```

Or, after building, run the JAR file directly:
```
java -jar build/libs/deditor-1.0-SNAPSHOT.jar
```

## Configuration

Deditor is configured using TOML files located in the `config` directory:

- `config/config.toml`: Main configuration file for editor settings
- `config/packages/*.toml`: Configuration files for language servers and other packages

### Example Configuration

```toml
[cursor]
blink = 500 # cursor blink interval
color = "#77FFFFFF"
select = "#8066B9FF"

[font]
family = "Iosevka Nerd Font Mono"
size = 16

[theme]
background = "#FF1B1B1B"
numberColor = "#FF666666"
```

## Commands

Deditor supports various commands that can be executed from the command mode:

- `quit` or `q`: Exit the editor
- `edit` or `e`: Edit files
- `save` or `w`: Save files
- `vs`: Create a vertical split
- `s`: Create a horizontal split
- `fzf`: Toggle fuzzy finder
- `explorer`: Open file explorer
- `pkgman`: Open package manager

## Supported Languages

Deditor provides syntax highlighting and LSP support for multiple languages including:

- Java
- Kotlin
- Python
- JavaScript/TypeScript
- HTML/CSS
- C/C++
- Go
- Rust
- And many more

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
