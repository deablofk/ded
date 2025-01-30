package dev.cwby.exceptions;

public class NoConfigFoundException extends RuntimeException {
    public NoConfigFoundException() {
        super("No toml config found");
    }
}
