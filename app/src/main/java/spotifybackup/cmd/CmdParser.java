package spotifybackup.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CmdParser {
    private final List<Argument> arguments;
    private boolean argumentsParsed = false;

    public CmdParser() {
        arguments = new ArrayList<>();
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public void parseArguments(String[] args)
            throws MissingArgumentException, ArgumentNotPresentException, MalformedInputException {
        if (!argumentsParsed) {
            Argument lookingForArgument = null;
            for (String arg : args) {
                if (lookingForArgument == null) {
                    if (arg.startsWith("--")) {
                        lookingForArgument = arguments.stream().filter(argument -> (
                                argument.name.equals(arg.substring(2)) && !argument.isPresent)
                        ).findFirst().orElseThrow(() -> new ArgumentNotPresentException(
                                "Malformed command line argument supplied to program: " + arg + " not an argument.")
                        );
                        lookingForArgument.isPresent = true;
                    } else if (arg.startsWith("-")) {
                        for (char c : arg.substring(1).toCharArray()) {
                            lookingForArgument = arguments.stream().filter(argument -> (argument.shortName == c && !argument.isPresent))
                                    .findFirst().orElse(null);
                            if (lookingForArgument != null) {
                                lookingForArgument.isPresent = true;
                            }
                            // TODO: implement multiple flag arguments followed by value
                        }
                    } else {
                        throw new RuntimeException("Malformed command line argument supplied to program.");
                    }
                    if (lookingForArgument != null && !lookingForArgument.hasValue) {
                        lookingForArgument = null;
                    }
                } else {
                    if (!lookingForArgument.hasValue) {
                        throw new RuntimeException("Malformed command line argument supplied to program: "
                                + lookingForArgument.name + " shouldn't be supplied a value.");
                    } else {
                        lookingForArgument.setValue(arg);
                        lookingForArgument = null;
                    }
                }
            }
            if (lookingForArgument != null) {
                throw new MalformedInputException("Missing value for argument.");
            }
            if (arguments.stream().anyMatch(argument -> argument.isMandatory && !argument.isPresent)) {
                throw new MissingArgumentException("Mandatory arguments missing from input.");
            }
            argumentsParsed = true;
        }
    }

    public Object getValue(String name)
            throws ArgumentNotPresentException, ArgumentsNotParsedException {
        if (!argumentsParsed) {
            throw new ArgumentsNotParsedException("CmdParser.getValue() called before parsing arguments.");
        } else {
            return arguments.stream().filter(arg -> arg.name.equals(name)).findFirst()
                    .orElseThrow(() -> new ArgumentNotPresentException("Argument " + name + " not supplied."))
                    .getValue();
        }
    }
}
