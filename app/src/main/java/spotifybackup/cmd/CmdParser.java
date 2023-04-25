package spotifybackup.cmd;

import java.util.ArrayList;
import java.util.List;

public class CmdParser {
    private List<Argument> arguments;

    public CmdParser() {
        arguments = new ArrayList<>();
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public void parseArguments(String[] args) {
        for (String arg : args) {
            System.out.println("arg: " + arg);
        }
    }
}
