package spotifybackup.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CmdParser {
    private final List<Argument> arguments;
    private boolean argumentsParsed = false;

    /**
     * The CmdParser class makes it easy to write user-friendly command-line interfaces.
     * @param arguments Array of arguments to be evaluated.
     */
    public CmdParser(final Argument[] arguments) {
        this.arguments = new ArrayList<>();
        this.arguments.addAll(List.of(arguments));
    }

    /**
     * Parses command line program arguments into usable format.
     * @param args String[] parameter from main method.
     * @throws MissingArgumentException when a mandatory argument is missing from the input.
     * @throws MalformedInputException  when the input from the command line is written incorrectly.
     */
    public void parseArguments(final String[] args)
            throws MissingArgumentException, MalformedInputException {
        if (!argumentsParsed) {
            var lexedInput = lexer(args);
            parser(lexedInput);
            if (arguments.stream().anyMatch(argument -> argument.isMandatory && !argument.isPresent)) {
                throw new MissingArgumentException("Mandatory arguments missing from input.");
            }
            argumentsParsed = true;
        }
    }

    /**
     * Get the value of an argument.
     * @param name Identifying name of argument as defined in constructor.
     * @return Parsed value of argument.
     * @throws ArgumentNotPresentException when trying to get the value of an undefined argument.
     * @throws ArgumentsNotParsedException when trying to get value before command-line input has been parsed.
     */
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
        return arguments.stream().filter(argument -> (argument.shortName != null && argument.shortName == shortName))
                .findFirst().orElse(null);
    }

    private Argument identifyArgumentByName(String name) {
        return arguments.stream().filter(arg -> (arg.name.equals(name)))
                .findFirst().orElse(null);
    }

    private void parser(final LexedArgs[] inputs) throws MalformedInputException {
        for (var iter = Stream.of(inputs).iterator(); iter.hasNext(); ) {
            var input = iter.next();
            Argument argument = null;
            switch (input.type) {
                case SHORT_ARGUMENT -> argument = identifyArgumentByShortName(input.arg.charAt(1));
                case LONG_ARGUMENT -> argument = identifyArgumentByName(input.arg.substring(2));
                case SHORT_ARGUMENTS -> {
                    List<Argument> shortArguments = new ArrayList<>();
                    for (var c : input.arg.substring(1).toCharArray()) {
                        shortArguments.add(identifyArgumentByShortName(c));
                    }
                    if (shortArguments.stream().filter(arg -> (arg.hasValue)).count() > 1) {
                        throw new MalformedInputException("Cannot have more than one value type argument in shortened block: " + input.arg);
                    } else {
                        argument = shortArguments.stream().filter(arg -> (arg.hasValue)).findFirst().orElse(null);
                        shortArguments.stream().filter(arg -> (!arg.hasValue)).forEach(arg -> {
                            arg.isPresent = true;
                        });
                    }
                }
                case VALUE ->
                        throw new MalformedInputException("Value: " + input.arg + " supplied without identifying argument.");
            }
            if (argument == null) {
                throw new MalformedInputException("No argument defined by: " + input.arg);
            } else if (!argument.isPresent) {
                argument.isPresent = true;
                if (argument.hasValue) {
                    if (!iter.hasNext()) {
                        throw new MalformedInputException("Missing value for argument.");
                    }
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
        SHORT_ARGUMENT("^[-]{1}[a-zA-Z]{1}$"),
        SHORT_ARGUMENTS("^[-]{1}[a-zA-Z]{2,}$"),
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
