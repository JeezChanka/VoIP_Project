package pl.poznan.put.voip.core.commands;

public interface CommandHandler {
    void handle(String... args);
}
