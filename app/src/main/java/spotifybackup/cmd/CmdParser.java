package spotifybackup.cmd;

import org.apache.commons.text.WordUtils;
import spotifybackup.cmd.argument.FlagArgument;
import spotifybackup.cmd.exception.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class CmdParser {
    private final List<Argument> arguments;
    private final String description;
    private final String epilogue;
    private final String programName;
    private boolean argumentsParsed = false;

    private CmdParser(Builder builder) throws IllegalConstructorParameterException {
        this.arguments = builder.arguments;
        this.epilogue = builder.epilogue;
        this.programName = builder.programName;
        this.description = builder.description;
    }

    /**
     * Parses command line program arguments into usable format.
     * @param args String[] parameter from main method.
     * @throws MissingArgumentException when a mandatory argument is missing from the input.
     * @throws MalformedInputException  when the input from the command line is written incorrectly.
     * @throws MissingValueException    when an optional or mandatory argument is missing its value.
     */
    public void parseArguments(final String[] args)
            throws MissingArgumentException, MalformedInputException {
        if (!argumentsParsed) {
            var lexedInput = lexer(args);
            parser(lexedInput);
            if (arguments.stream().anyMatch(argument -> argument.argMandatory && !argument.isPresent)) {
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
        final List<Argument> mandatoryArguments = arguments.stream().filter(Argument::getArgMandatory).toList();
        final List<Argument> optionalArguments = arguments.stream().filter(not(Argument::getArgMandatory)).toList();
        StringBuilder helpText = new StringBuilder();

        helpText.append(generateUsage(maxWidth)).append("\n");

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

        if (epilogue != null) {
            helpText.append("\n").append(WordUtils.wrap(epilogue, maxWidth)).append("\n");
        }
        return helpText.toString();
    }

    private String generateUsage(int maxWidth) {
        var sortedArguments = new ArrayList<>(arguments.stream().filter(Argument::getArgMandatory).toList());
        sortedArguments.addAll(arguments.stream().filter(not(Argument::getArgMandatory)).toList());
        StringBuilder usageText = new StringBuilder();
        try (Formatter formatter = new Formatter(usageText)) {
            formatter.format("Usage: %s", programName != null ? programName + " " : "");
            for (var argument : sortedArguments) {
                var valueString = argument.argMandatory || argument.valMandatory ?
                        (" " + argument.getValueName()) : (" [" + argument.getValueName() + "]");
                formatter.format(argument.argMandatory ? "-%s%s " : "[-%s%s] ",
                        argument.hasShortName() ? argument.shortName : "-" + argument.name,
                        !argument.hasValue ? "" : valueString
                );
            }
        }
        return WordUtils.wrap(usageText.toString().strip(), maxWidth);
    }

    private Optional<Argument> identifyArgumentByShortName(String arg) {
        return identifyArgumentByShortName(arg.charAt(1));
    }

    private Optional<Argument> identifyArgumentByShortName(char shortName) {
        return arguments.stream().filter(Argument::hasShortName).filter(argument -> (argument.shortName == shortName))
                .findFirst();
    }

    private Optional<Argument> identifyArgumentByName(String arg) {
        return arguments.stream().filter(argument -> (argument.name.equals(arg.substring(2))))
                .findFirst();
    }

    private List<Argument> listArgumentsFromShortNames(char[] shortNames, String arg) throws MalformedInputException {
        List<Argument> argumentList = new ArrayList<>();
        for (var c : shortNames) {
            argumentList.add(identifyArgumentByShortName(c).orElseThrow(() ->
                    new MalformedInputException("No argument defined by: " + c + " in shortened block: " + arg)));
        }
        return argumentList;
    }

    private Optional<Argument> identifyValueArgumentByShortName(String arg) throws MalformedInputException {
        List<Argument> shortArguments = listArgumentsFromShortNames(arg.substring(1).toCharArray(), arg);
        Supplier<Stream<Argument>> mandatoryValueArguments = () ->
                shortArguments.stream().filter(Argument::getArgMandatory).filter(Argument::getHasValue);
        Supplier<Stream<Argument>> optionalValueArguments = () ->
                shortArguments.stream().filter(not(Argument::getArgMandatory)).filter(Argument::getHasValue);

        // mark all identified non-value (currently only FlagArgument) arguments as present
        shortArguments.stream().filter(not(Argument::getHasValue)).forEach(Argument::confirmPresent);

        if (mandatoryValueArguments.get().count() > 1) {
            throw new MalformedInputException("Cannot have more than one mandatory value type argument in " +
                    "shortened block: " + arg);
        } else if (mandatoryValueArguments.get().count() == 1) {
            optionalValueArguments.get().forEach(Argument::confirmPresent);
            return mandatoryValueArguments.get().findFirst();
        } else {
            if (optionalValueArguments.get().count() == 1) {
                return optionalValueArguments.get().findFirst();
            } else {
                optionalValueArguments.get().forEach(Argument::confirmPresent);
                return Optional.empty();
            }
        }
    }

    private Optional<Argument> identifyArgumentFromLexedArg(final LexedArgs input) throws MalformedInputException {
        return switch (input.type) {
            case SHORT_ARGUMENT -> identifyArgumentByShortName(input.arg);
            case LONG_ARGUMENT -> identifyArgumentByName(input.arg);
            case SHORT_ARGUMENTS -> identifyValueArgumentByShortName(input.arg);
            case VALUE -> throw new MalformedInputException("Value: " +
                    input.arg + " supplied without identifying argument.");
        };
    }

    private void parser(final LexedArgs[] inputs) throws MalformedInputException {
        for (var iter = Arrays.stream(inputs).toList().listIterator(); iter.hasNext(); ) {
            var input = iter.next();
            var argument = identifyArgumentFromLexedArg(input);
            if (argument.isEmpty()) {
                if (input.type != ArgType.SHORT_ARGUMENTS) {
                    throw new MalformedInputException("No argument defined by: " + input.arg);
                }
            } else if (argument.get().isPresent) {
                throw new MalformedInputException("Argument " + argument.get().name +
                        " repeated more than once in input.");
            } else {
                argument.get().confirmPresent();
                if (argument.get().hasValue) {
                    try {
                        var nextInput = iter.next();
                        if (nextInput.type == ArgType.VALUE) {
                            argument.get().setValue(nextInput.arg);
                        } else if (argument.get().argMandatory || argument.get().valMandatory) {
                            throw new MissingValueException("Argument " + argument.get().name +
                                    " supplied without value.");
                        } else {
                            iter.previous(); // undo ingestion of next argument
                        }
                    } catch (NoSuchElementException e) {
                        if (argument.get().argMandatory) {
                            throw new MissingValueException("Missing value for mandatory argument: " +
                                    argument.get().name);
                        }
                        if (argument.get().valMandatory) {
                            throw new MissingValueException("Missing value for optional argument: " +
                                    argument.get().name);
                        }
                    }
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
                throw new MalformedInputException("Input not identified as an ArgType: [" + args[i] + "]");
            }
        }
        return retval;
    }

    private enum ArgType {
        SHORT_ARGUMENT("^[-]{1}[a-zA-Z]{1}$"),
        SHORT_ARGUMENTS("^[-]{1}[a-zA-Z]{2,}$"),
        LONG_ARGUMENT("^[-]{2}[\\w]+$"),
        VALUE("^([-]?[\\d]+([.,]{1}[\\d]+)?)|([^-]{1,2}[\\w '\\\";:()<>.,{}\\[\\]\\\\/|?-_=+!@#$%^&*]+)$");

        final Pattern regex;

        ArgType(String regex) {
            this.regex = Pattern.compile(regex);
        }
    }

    public static class Builder {
        private final List<Argument> arguments;
        private String description;
        private String epilogue;
        private String programName;

        /** The CmdParser class makes it easy to write user-friendly command-line interfaces. */
        public Builder() {
            arguments = new ArrayList<>();
        }

        /**
         * @param arguments Arguments to be evaluated, name and shortName fields must be unique (shortName can be
         *                  null).
         */
        public Builder arguments(Argument... arguments) {
            this.arguments.addAll(List.of(arguments));
            return this;
        }

        /** @param argument Argument to be evaluated, name and shortName fields must be unique (shortName can be null). */
        public Builder argument(Argument argument) {
            this.arguments.add(argument);
            return this;
        }

        /** @param description Text to display before the argument help. */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /** @param epilogue Text to display after the argument help. */
        public Builder epilogue(String epilogue) {
            this.epilogue = epilogue;
            return this;
        }

        /** @param programName The name of the program. */
        public Builder programName(String programName) {
            this.programName = programName;
            return this;
        }

        /** Add a -h/--help argument to the parser. */
        public Builder addHelp() {
            arguments.add(0, new FlagArgument.Builder()
                    .name("help")
                    .description("Show this help message and exit.")
                    .shortName('h')
                    .build()
            );
            return this;
        }

        /** @throws IllegalConstructorParameterException when either an argument name or shortName is not unique. */
        public CmdParser build() throws IllegalConstructorParameterException {
            validate();
            return new CmdParser(this);
        }

        /** @throws IllegalConstructorParameterException when either an argument name or shortName is not unique. */
        private void validate() throws IllegalConstructorParameterException {
            if (this.arguments.stream().map(a -> a.name).distinct().count() != this.arguments.size()) {
                throw new IllegalConstructorParameterException("Duplicated Argument name in constructor, " +
                        "names should be unique.");
            }
            if (this.arguments.stream().filter(Argument::hasShortName).map(a -> a.shortName).distinct().count()
                    != this.arguments.stream().filter(Argument::hasShortName).count()) {
                throw new IllegalConstructorParameterException("Duplicated Argument shortName in constructor, " +
                        "shortNames should be unique.");
            }
        }
    }

    record LexedArgs(String arg, ArgType type) {
    }
}
