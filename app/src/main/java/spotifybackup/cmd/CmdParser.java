package spotifybackup.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

    private Argument identifyArgumentByShortName(char c) {
        return arguments.stream().filter(argument -> (argument.shortName == c))
                .findFirst().orElse(null);
    }

    private void parser(final LexedArgs[] args) throws MalformedInputException {
        List.of(args).stream().iterator()
        for (int i = 0; i < args.length; i++) {
            switch (args[i].type) {
                case SHORT_ARGUMENT -> {
                    char c = args[i].arg.charAt(1);
                    var argument = identifyArgumentByShortName(c);
                    if (argument == null) {
                        throw new MalformedInputException("No argument defined by: " + c);
                    } else if (!argument.isPresent) {
                        argument.isPresent = true;
                        if (argument.hasValue) {
                            if (args[i + 1].type == ArgType.VALUE) {
                                argument.setValue(args[i + 1].arg);
                                i++; // skip next argument because it will be a VALUE type
                            } else {
                                throw new MalformedInputException("Argument " + argument.name + " supplied without value.");
                            }
                        }
                    } else {
                        throw new MalformedInputException("Argument " + argument.name + " repeated more than once in input.");
                    }
                }

                case VALUE -> {
                    throw new MalformedInputException("Value: " + args[i].arg + " supplied without identifying argument.");
                }
            }
        }
    }

    private LexedArgs[] lexer(final String[] args) throws MalformedInputException {
        var retval = new LexedArgs[args.length];
        for (int i = 0; i < args.length; i++) {
            for (ArgType a : ArgType.values()) {
                if (a.regex.matcher(args[i]).find()) {
                    retval[i] = new LexedArgs(args[i], a);
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
