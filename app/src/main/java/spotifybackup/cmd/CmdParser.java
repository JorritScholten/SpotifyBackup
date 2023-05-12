package spotifybackup.cmd;

import org.apache.commons.text.WordUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdParser {
    private final List<Argument> arguments;
    private final String description;
    private final String epilog;
    private final String programName;
    private boolean argumentsParsed = false;

    /**
     * The CmdParser class makes it easy to write user-friendly command-line interfaces, adds a -h/--help option to the
     * parser.
     * @param arguments Array of arguments to be evaluated, name and shortName fields must be unique (shortName can be
     *                  null).
     * @throws IllegalConstructorParameterException when either an argument name or shortName is not unique.
     */
    public CmdParser(final Argument[] arguments)
            throws IllegalConstructorParameterException {
        this(arguments, null, null, null, true);
    }

    /**
     * The CmdParser class makes it easy to write user-friendly command-line interfaces, adds a -h/--help option to the
     * parser.
     * @param arguments   Array of arguments to be evaluated, name and shortName fields must be unique (shortName can be
     *                    null).
     * @param description Text to display before the argument help.
     * @throws IllegalConstructorParameterException when either an argument name or shortName is not unique.
     */
    public CmdParser(final Argument[] arguments, String description)
            throws IllegalConstructorParameterException {
        this(arguments, description, null, null, true);
    }

    /**
     * The CmdParser class makes it easy to write user-friendly command-line interfaces, adds a -h/--help option to the
     * parser.
     * @param arguments   Array of arguments to be evaluated, name and shortName fields must be unique (shortName can be
     *                    null).
     * @param description Text to display before the argument help.
     * @param programName The name of the program.
     * @throws IllegalConstructorParameterException when either an argument name or shortName is not unique.
     */
    public CmdParser(final Argument[] arguments, String description, String programName)
            throws IllegalConstructorParameterException {
        this(arguments, description, programName, null, true);
    }

    /**
     * The CmdParser class makes it easy to write user-friendly command-line interfaces, adds a -h/--help option to the
     * parser.
     * @param arguments   Array of arguments to be evaluated, name and shortName fields must be unique (shortName can be
     *                    null).
     * @param description Text to display before the argument help.
     * @param programName The name of the program.
     * @param epilog      Text to display after the argument help.
     * @throws IllegalConstructorParameterException when either an argument name or shortName is not unique.
     */
    public CmdParser(final Argument[] arguments, String description, String programName, String epilog)
            throws IllegalConstructorParameterException {
        this(arguments, description, programName, epilog, true);
    }

    /**
     * The CmdParser class makes it easy to write user-friendly command-line interfaces.
     * @param arguments   Array of arguments to be evaluated, name and shortName fields must be unique (shortName can be
     *                    null).
     * @param description Text to display before the argument help.
     * @param programName The name of the program.
     * @param epilog      Text to display after the argument help.
     * @param addHelp     Add a -h/--help option to the parser.
     * @throws IllegalConstructorParameterException when either an argument name or shortName is not unique.
     */
    public CmdParser(final Argument[] arguments, String description, String programName, String epilog, boolean addHelp)
            throws IllegalConstructorParameterException {
        this.arguments = new ArrayList<>();
        if (addHelp) {
            this.arguments.add(new FlagArgument("help", "Show this help message and exit.", 'h'));
        }
        this.arguments.addAll(List.of(arguments));
        Set<String> argumentNames = new HashSet<>();
        this.arguments.forEach(argument -> {
            if (!argumentNames.add(argument.name)) {
                throw new IllegalConstructorParameterException("Duplicated Argument name in constructor, names should be unique.");
            }
        });
        Set<Character> argumentShortNames = new HashSet<>();
        this.arguments.stream().filter(argument -> argument.shortName != null).forEach(argument -> {
            if (!argumentShortNames.add(argument.shortName)) {
                throw new IllegalConstructorParameterException("Duplicated Argument shortName in constructor, shortNames should be unique.");
            }
        });
        this.description = description;
        this.programName = programName;
        this.epilog = epilog;
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
                    .orElseThrow(() -> new ArgumentNotPresentException("Argument " + name + " not supplied in input."))
                    .getValue();
        }
    }

    /**
     * Get if value is present in input, useful for seeing if default arguments are flagged.
     * @param name Identifying name of argument as defined in constructor.
     * @return true if argument is found in input.
     * @throws ArgumentsNotParsedException when trying to get value before command-line input has been parsed.
     */
    public boolean isPresent(String name)
            throws ArgumentsNotParsedException {
        if (!argumentsParsed) {
            throw new ArgumentsNotParsedException("CmdParser.isPresent() called before parsing arguments.");
        } else {
            return arguments.stream().anyMatch(arg -> (arg.name.equals(name) && arg.isPresent));
        }

    }

    /**
     * Generate help message, assumes 80 character width terminal.
     * @return String containing help message.
     */
    public String getHelp() {
        final int DEFAULT_TERMINAL_WIDTH = 80;
        return getHelp(DEFAULT_TERMINAL_WIDTH, DEFAULT_TERMINAL_WIDTH / 4);
    }

    /**
     * Generate help message, formats name block to quarter width.
     * @param maxWidth Width of command line for text wrapping.
     * @return String containing help message.
     */
    public String getHelp(int maxWidth) {
        return getHelp(maxWidth, maxWidth / 4);
    }

    /**
     * Generate help message.
     * @param maxWidth  Width of command line for text wrapping.
     * @param nameWidth Width of argument name block.
     * @return String containing help message.
     */
    public String getHelp(int maxWidth, int nameWidth) {
        final List<Argument> mandatoryArguments = arguments.stream().filter(argument -> argument.isMandatory).toList();
        final List<Argument> optionalArguments = arguments.stream().filter(argument -> !argument.isMandatory).toList();
        StringBuilder helpText = new StringBuilder();
        Formatter formatter = new Formatter(helpText);

        // generate usage block
        formatter.format("Usage: %s", programName != null ? programName + " " : "");
        if (!mandatoryArguments.isEmpty()) {
            for (Argument argument : mandatoryArguments) {
                formatter.format("-%s %s",
                        argument.hasShortName() ? argument.shortName : "-" + argument.name,
                        argument.hasValue ? argument.getValueName() + " " : "");
            }
        }
        if (!optionalArguments.isEmpty()) {
            for (Argument argument : optionalArguments) {
                formatter.format("[-%s%s] ",
                        argument.hasShortName() ? argument.shortName : "-" + argument.name,
                        argument.hasValue ? " [" + argument.getValueName() + "]" : "");
            }
        }
        helpText.replace(0, helpText.length() - 1, WordUtils.wrap(helpText.toString().strip(), maxWidth));
        helpText.append("\n");

        if (description != null) {
            helpText.append("\n").append(WordUtils.wrap(description, maxWidth)).append("\n");
        }

        // add mandatory argument information
        if (!mandatoryArguments.isEmpty()) {
            helpText.append("\nMandatory arguments:\n");
            for (Argument argument : mandatoryArguments) {
                helpText.append(argument.getHelp(nameWidth, maxWidth)).append("\n");
            }
        }
        // add optional argument information
        if (!optionalArguments.isEmpty()) {
            helpText.append("\nOptional arguments:\n");
            for (Argument argument : optionalArguments) {
                helpText.append(argument.getHelp(nameWidth, maxWidth)).append("\n");
            }
        }

        if (epilog != null) {
            helpText.append("\n").append(WordUtils.wrap(epilog, maxWidth)).append("\n");
        }
        return helpText.toString();
    }

    private Argument identifyArgumentByShortName(String arg) {
        return identifyArgumentByShortName(arg.charAt(1));
    }

    private Argument identifyArgumentByShortName(char shortName) {
        return arguments.stream().filter(argument -> (argument.shortName != null && argument.shortName == shortName))
                .findFirst().orElse(null);
    }

    private Argument identifyArgumentByName(String arg) {
        return arguments.stream().filter(argument -> (argument.name.equals(arg.substring(2))))
                .findFirst().orElse(null);
    }

    private Argument identifyValueArgumentByShortName(String arg)
            throws MalformedInputException {
        List<Argument> shortArguments = new ArrayList<>();
        for (var c : arg.substring(1).toCharArray()) {
            var argument = identifyArgumentByShortName(c);
            if (argument == null) {
                throw new MalformedInputException("No argument defined by: " + c + " in shortened block: " + arg);
            } else {
                shortArguments.add(argument);
            }
        }
        shortArguments.stream().filter(argument -> (!argument.hasValue)).forEach(argument -> {
            argument.isPresent = true;
        });

        if (shortArguments.stream().filter(argument -> (argument.hasValue && argument.isMandatory)).count() > 1) {
            throw new MalformedInputException("Cannot have more than one mandatory value type argument in shortened block: " + arg);
        } else if (shortArguments.stream().filter(argument -> (argument.hasValue && argument.isMandatory)).count() == 1) {
            shortArguments.stream().filter(argument -> (!argument.isMandatory)).forEach(argument -> {
                argument.isPresent = true;
            });
            return shortArguments.stream().filter(argument -> (argument.hasValue && argument.isMandatory))
                    .findFirst().orElse(null);
        } else if (shortArguments.stream().noneMatch(argument -> (argument.hasValue && argument.isMandatory))) {
            shortArguments.stream().filter(argument -> (!argument.isMandatory)).forEach(argument -> {
                argument.isPresent = true;
            });
            return null;
        } else {
            throw new RuntimeException("Should not occur, all mandatory arguments should have a value.");
        }
    }

    private void parser(final LexedArgs[] inputs) throws MalformedInputException {
        for (var iter = Stream.of(inputs).collect(Collectors.toList()).listIterator(); iter.hasNext(); ) {
            var input = iter.next();
            Argument argument = switch (input.type) {
                case SHORT_ARGUMENT -> identifyArgumentByShortName(input.arg);
                case LONG_ARGUMENT -> identifyArgumentByName(input.arg);
                case SHORT_ARGUMENTS -> identifyValueArgumentByShortName(input.arg);
                case VALUE -> {
                    throw new MalformedInputException("Value: " + input.arg + " supplied without identifying argument.");
                }
            };
            if (argument == null) {
                if (input.type != ArgType.SHORT_ARGUMENTS) {
                    throw new MalformedInputException("No argument defined by: " + input.arg);
                }
            } else if (!argument.isPresent) {
                argument.isPresent = true;
                if (argument.hasValue) {
                    if (argument.isMandatory) {
                        if (!iter.hasNext()) {
                            throw new MalformedInputException("Missing value for argument.");
                        }
                        var nextInput = iter.next();
                        if (nextInput.type == ArgType.VALUE) {
                            argument.setValue(nextInput.arg);
                        } else {
                            throw new MalformedInputException("Argument " + argument.name + " supplied without value.");
                        }
                    } else {
                        if (iter.hasNext()) {
                            var nextInput = iter.next();
                            if (nextInput.type == ArgType.VALUE) {
                                argument.setValue(nextInput.arg);
                            } else {
                                // prevent ingestion of arguments
                                iter.previous();
                            }
                        }
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
        VALUE("^([^-]{1,2}[\\w]+)|([-][\\d]+)$");

        final Pattern regex;

        ArgType(String regex) {
            this.regex = Pattern.compile(regex);
        }
    }

    record LexedArgs(String arg, ArgType type) {
    }
}
