package pl.poznan.put.voip.core.commands;

public class CommandDto {
    private final String command;
    private final String[] args;

    public CommandDto(String command, String... args) {
        this.command = command;
        this.args = args;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

}
