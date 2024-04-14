package spotifybackup.app;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public abstract class TerminalInteraction {
    @Getter(AccessLevel.PACKAGE)
    private static final Terminal term;

    static {
        try {
            term = TerminalBuilder.terminal();
        } catch (IOException e) {
            throw new RuntimeException("Can't create terminal. " + e);
        }
    }

    TerminalInteraction() {}

    public static void println(String message) {
        term.writer().println(message);
        term.flush();
    }

    /** @param spaces Amount of spaces to prepend to message. */
    public static void println(int spaces, String message) {
        println(" ".repeat(spaces) + message);
    }

    public static void verbosePrintln(String message) {
        if (App.verboseArg.isPresent()) println(message);
    }

    /** @param spaces Amount of spaces to prepend to message. */
    public static void verbosePrintln(int spaces, String message) {
        if (App.verboseArg.isPresent()) println(spaces, message);
    }

    public static void print(String message) {
        term.writer().print(message);
        term.flush();
    }

    /** @param spaces Amount of spaces to prepend to message. */
    public static void print(int spaces, String message) {
        print(" ".repeat(spaces) + message);
    }

    public static void verbosePrint(String message) {
        if (App.verboseArg.isPresent()) print(message);
    }

    /** @param spaces Amount of spaces to prepend to message. */
    public static void verbosePrint(int spaces, String message) {
        if (App.verboseArg.isPresent()) print(spaces, message);
    }

    public static void printf(String format, Object... args) {
        term.writer().printf(format, args);
        term.flush();
    }

    /**
     * Print numbered list of Enum.Values() to console.
     * @param enumValues Enum.values() of specified enum.
     * @param <T>        An enum with at least one type.
     * @throws IllegalArgumentException when enum has no types.
     */
    private static <T extends Enum<T>> void printIndexedEnum(T[] enumValues) throws IllegalArgumentException {
        if (enumValues.length < 1) throw new IllegalArgumentException("Enum should have at least one type.");
        int i = 1;
        for (T value : enumValues) {
            printf("\t%2d\t-\t%s%n", i, value.name());
            i++;
        }
    }

    /**
     * Utility function to ask for a character in the terminal.
     * @param prompt Question to print to terminal.
     * @return char value.
     */
    public static char askForChar(String prompt) {
        char choice;
        String scanOut;
        Scanner scan = new Scanner(term.input());
        do {
            try {
                print(prompt);
                scanOut = scan.next();
                if (scanOut.length() > 1) {
                    print("Enter only one character please. ");
                    continue;
                }
                choice = scanOut.charAt(0);
            } catch (InputMismatchException ex) {
                printInvalidChoice();
                scan.next();
                continue;
            }
            return choice;
        } while (true);
    }

    private static void printInvalidChoice() {
        print("Invalid choice entered, please try again. ");
    }

    /**
     * Utility function to ask for confirmation in terminal using a single character input.
     * @param prompt        Question to print to terminal.
     * @param defaultOption default choice to return when input is left blank, should be in {@code options}.
     * @param options       array of options, will re-prompt when input is not in list.
     * @return char value.
     */
    public static char confirmUsingChar(String prompt, char defaultOption, @NonNull Character... options) {
        List<Character> optionsList = List.of(options);
        if (!optionsList.contains(defaultOption))
            throw new IllegalArgumentException("defaultOption[" + defaultOption + "] not in options" +
                    Arrays.toString(options));
        char choice;
        String scanOut;
        Scanner scan = new Scanner(term.input());
        do {
            print(prompt);
            scanOut = scan.nextLine();
            if (scanOut.isBlank()) {
                return defaultOption;
            } else if (scanOut.length() > 1) {
                print("Enter only one character please. ");
                continue;
            }
            if (!optionsList.contains(scanOut.charAt(0))) {
                print("Please select one of the specified characters. ");
                continue;
            } else {
                choice = scanOut.charAt(0);
            }
            return choice;
        } while (true);
    }

    /**
     * Utility function to ask for a string in the terminal.
     * @param prompt Question to print to terminal.
     * @return reply to prompt.
     */
    public static String askForString(String prompt) {
        String choice;
        Scanner scan = new Scanner(term.input());
        do {
            try {
                print(prompt);
                choice = scan.nextLine();
            } catch (InputMismatchException ex) {
                printInvalidChoice();
                scan.next();
                continue;
            }
            return choice;
        } while (true);
    }

    /**
     * Utility function to ask for a string that is not blank in the terminal.
     * @param prompt Question to print to terminal.
     * @return reply to prompt.
     */
    public static String askForNonBlankString(String prompt) {
        String choice;
        Scanner scan = new Scanner(term.input());
        do {
            print(prompt);
            choice = scan.nextLine();
            if (choice.isBlank()) {
                print("Input can not be blank. ");
                continue;
            }
            return choice;
        } while (true);
    }

    /**
     * Utility function to ask for a URI in the terminal, will re-prompt if uri is blank or not valid.
     * @param prompt Question to print to terminal.
     * @return reply to prompt.
     */
    public static URI askForURI(String prompt) {
        String choice;
        Scanner scan = new Scanner(term.input());
        do {
            try {
                print(prompt);
                choice = scan.next();
                if (choice.isBlank()) {
                    print("Input can not be blank. ");
                    continue;
                }
                return URI.create(choice).toURL().toURI();
            } catch (MalformedURLException | URISyntaxException | InputMismatchException ex) {
                printInvalidChoice();
                scan.nextLine();
            }
        } while (true);
    }

    /**
     * Utility function to ask for an int in the terminal.
     * @param prompt Question to print to terminal.
     * @return Chosen int value.
     */
    public static int askForInt(String prompt) {
        int choice;
        Scanner scan = new Scanner(term.input());
        do {
            try {
                print(prompt);
                choice = scan.nextInt();
            } catch (InputMismatchException ex) {
                printInvalidChoice();
                scan.next();
                continue;
            }
            return choice;
        } while (true);
    }

    /**
     * Utility function to ask for an array of int values in the terminal.
     * @param prompt    Question to print to terminal.
     * @param separator regex for separating input.
     * @return Array of int values.
     */
    public static int[] askForInts(String prompt, String separator) {
        return askForInts(prompt, separator, -1);
    }

    /**
     * Utility function to ask for a specified amount of int values in the terminal.
     * @param prompt    Question to print to terminal.
     * @param separator regex for separating input.
     * @param amount    Amount of values user should enter, ignored if negative or zero.
     * @return Array of int values.
     */
    public static int[] askForInts(String prompt, String separator, int amount) {
        List<Integer> integerList = new ArrayList<>();
        String choice;
        Scanner scan = new Scanner(term.input());
        do {
            try {
                print(prompt);
                choice = scan.nextLine();
                integerList.clear();
                for (String number : choice.split(separator)) {
                    integerList.add(Integer.decode(number));
                }
            } catch (InputMismatchException ex) {
                printInvalidChoice();
                continue;
            } catch (NumberFormatException e) {
                print("Input is not formatted correctly, please try again. ");
                continue;
            }
            if (amount > 0 && integerList.size() != amount) {
                print("Amount of int values is wrong, please try again. ");
                continue;
            }
            return integerList.stream().mapToInt(Integer::intValue).toArray();
        } while (true);
    }


    /**
     * Utility function to ask for an int in range min to max in the terminal.
     * @param min Minimum value of returned int.
     * @param max Maximum value of returned int.
     * @return Chosen int value which is in range.
     */
    public static int chooseIntInRange(int min, int max) {
        return chooseIntInRange("Please choose by typing the relevant number", min, max);
    }

    /**
     * Utility function to ask for an int in range min to max in the terminal.
     * @param prompt Question to print to terminal, appended with the range as '(min to max):'.
     * @param min    Minimum value of returned int.
     * @param max    Maximum value of returned int.
     * @return Chosen int value which is in range.
     */
    public static int chooseIntInRange(String prompt, int min, int max) {
        int choice = Integer.MIN_VALUE;
        Scanner scan = new Scanner(term.input());
        do {
            try {
                printf("%s (%d to %d):", prompt, min, max);
                choice = scan.nextInt();
            } catch (InputMismatchException ex) {
                printInvalidChoice();
                scan.next();
            }
        } while (choice < min || choice > max);
        return choice;
    }

    /**
     * Utility function to present a choice of options from a String array to the terminal.
     * @param prompt  Question to print to terminal.
     * @param options Array of strings describing the options.
     * @return index value of selected option.
     * @throws IllegalArgumentException when options array or prompt is empty.
     */
    public static int chooseFromArray(String prompt, String[] options) throws IllegalArgumentException {
        if (options.length == 0) throw new IllegalArgumentException("List of options shouldn't be empty.");
        if (prompt.isEmpty()) throw new IllegalArgumentException("message shouldn't be empty.");
        println(prompt);
        int i = 1;
        for (String option : options) {
            printf("\t%2d\t-\t%s%n", i, option);
            i++;
        }
        return chooseIntInRange(1, options.length) - 1;
    }

    /**
     * Utility function to present a choice of options from all possible enum values to the terminal.
     * @param prompt   Question to print to terminal.
     * @param instance enum of options to present in terminal.
     * @param <T>      An enum with at least one type.
     * @return Selected enum option.
     * @throws IllegalArgumentException when enum has no types or prompt is empty.
     */
    public static <T extends Enum<T>> T chooseFromEnum(String prompt, T instance) {
        return chooseFromEnum(prompt, instance.getDeclaringClass().getEnumConstants());
    }

    /**
     * Utility function to present a choice of options from an enum to the terminal.
     * @param prompt     Question to print to terminal.
     * @param enumValues array of subset enum values.
     * @param <T>        An enum with at least one type.
     * @return Selected enum option.
     * @throws IllegalArgumentException when enum has no types or prompt is empty.
     */
    public static <T extends Enum<T>> T chooseFromEnum(String prompt, T[] enumValues) {
        if (prompt.isEmpty()) throw new IllegalArgumentException("message shouldn't be empty.");
        println(prompt);
        printIndexedEnum(enumValues);
        int choice = chooseIntInRange(1, enumValues.length);
        return enumValues[choice - 1];
    }
}
