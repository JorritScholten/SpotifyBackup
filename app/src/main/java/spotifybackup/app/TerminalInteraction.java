package spotifybackup.app;

import lombok.AccessLevel;
import lombok.Getter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

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
}
