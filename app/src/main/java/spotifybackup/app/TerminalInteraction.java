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
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

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

    /**
     * Utility function to ask for a character in the terminal.
     * @param prompt Question to print to terminal.
     * @return char value.
     */
    public static char askForChar(String prompt) {
        char choice;
        String scan_out;
        Scanner scan = new Scanner(term.input());
        do {
            try {
                print(prompt);
                scan_out = scan.next();
                if (scan_out.length() > 1) {
                    print("Enter only one character please. ");
                    continue;
                }
                choice = scan_out.charAt(0);
            } catch (InputMismatchException ex) {
                print("Invalid choice entered, please try again. ");
                scan.next();
                continue;
            }
            return choice;
        } while (true);
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
        String scan_out;
        Scanner scan = new Scanner(term.input());
        do {
            print(prompt);
            scan_out = scan.nextLine();
            if (scan_out.isBlank()) {
                return defaultOption;
            } else if (scan_out.length() > 1) {
                print("Enter only one character please. ");
                continue;
            }
            if (!optionsList.contains(scan_out.charAt(0))) {
                print("Please select one of the specified characters. ");
                continue;
            } else {
                choice = scan_out.charAt(0);
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
                print("Invalid choice entered, please try again. ");
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
                print("Invalid choice entered, please try again. ");
                scan.nextLine();
                continue;
            }
        } while (true);
    }
}
