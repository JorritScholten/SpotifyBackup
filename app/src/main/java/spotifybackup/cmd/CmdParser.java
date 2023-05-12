package spotifybackup.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CmdParser {
    private final List<Argument> arguments;
    private boolean argumentsParsed = false;

    public CmdParser(final Argument[] arguments) {
        this.arguments = new ArrayList<>();
        this.arguments.addAll(List.of(arguments));
    }

    public void parseArguments(final String[] args)
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

    private Argument identifyArgumentByShortName(char shortName) {
        return arguments.stream().filter(argument -> (argument.shortName == shortName))
                .findFirst().orElse(null);
    }

    private Argument identifyArgumentByName(String name) {
        return arguments.stream().filter(arg -> (arg.name.equals(name)))
                .findFirst().orElse(null);
    }

    private void parser(final LexedArgs[] inputs) throws MalformedInputException {
        for (var iter = Stream.of(inputs).iterator(); iter.hasNext(); ) {
            var input = iter.next();
            switch (input.type) {
                case SHORT_ARGUMENT -> {
                    char c = input.arg.charAt(1);
                    var argument = identifyArgumentByShortName(c);
                    if (argument == null) {
                        throw new MalformedInputException("No argument defined by: " + c);
                    } else if (!argument.isPresent) {
                        argument.isPresent = true;
                        if (argument.hasValue) {
                            var nextInput = iter.next();
                            if (nextInput.type == ArgType.VALUE) {
                                argument.setValue(nextInput.arg);
                            } else {
                                throw new MalformedInputException("Argument " + argument.name + " supplied without value.");
                            }
                        }
                    } else {
                        throw new MalformedInputException("Argument " + argument.name + " repeated more than once in input.");
                    }
                }
                case LONG_ARGUMENT -> {
                    String s = input.arg.substring(2);
                    var argument = identifyArgumentByName(s);
                    if (argument =)
                }
                case VALUE -> {
                    throw new MalformedInputException("Value: " + input.arg + " supplied without identifying argument.");
                }
            }
        }
    }

    private LexedArgs[] lexer(final String[] args) throws MalformedInputException {
        var retval = new LexedArgs[args.length];
        for (int i = 0; i < args.length; i++) {
            for (ArgType type : ArgType.values()) {
                if (type.regex.matcher(args[i]).find()) {
                    retval[i] = new LexedArgs(args[i], type);
                    break;
                }
            }
            if (retval[i] == null) {
                throw new MalformedInputException("Argument not identified as an ArgType.");
            }
        }
        return retval;
    }

    private enum ArgType {
        SHORT_ARGUMENT("^[-]{1}[\\w]{1}$"),
        SHORT_ARGUMENTS("^[-]{1}[\\w]{2,}$"),
        LONG_ARGUMENT("^[-]{2}[\\w]+$"),
        VALUE("^[^-]{1,2}[\\w]+$");

        final Pattern regex;

        ArgType(String regex) {
            this.regex = Pattern.compile(regex);
        }
    }

    record LexedArgs(String arg, ArgType type) {
    }
}
