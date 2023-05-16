package spotifybackup.app;

import spotifybackup.cmd.*;
import spotifybackup.cmd.argument.file.MandatoryFilePathArgument;
import spotifybackup.cmd.argument.integer.DefaultIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryIntArgument;
import spotifybackup.cmd.argument.string.DefaultStringArgument;
import spotifybackup.cmd.exception.MalformedInputException;
import spotifybackup.cmd.exception.MissingArgumentException;

public class App {
    public static void main(String[] args) {
        addition(args);
//        System.out.println("Hello SpotifyBackup!");
//        CmdParser argParser = new CmdParser(new Argument[]{
//                new MandatoryIntArgument("int", "some integer"),
//                new DefaultIntArgument("int2", "Lorem ipsum dolor sit amet, consectetur adipiscing " +
//                        "elit, sed do eiusmod tempor incididunt ut", 'i', 23),
//                new DefaultStringArgument("str", "some sort of string, dunno, not gonna use it.",
//                        's', "string"),
//                new MandatoryFilePathArgument("txt", "Lorem ipsum dolor sit amet, consectetur " +
//                        "adipiscing elit, sed do eiusmod tempor incididunt ut", false)
//        }, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut",
//                "testName.jar", "labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
//                "nostrud exercitation");
////        argParser.parseArguments(args);
//        System.out.println(argParser.getHelp(60));
    }

    public static void addition(String[] args) {
        var argParser = new CmdParser(new Argument[]{
                new MandatoryIntArgument("value1", "First value.", 'a'),
                new MandatoryIntArgument("value2", "Second value.", 'b')
        }, "Program description", "Add.jar", "Help footer");
        try {
            argParser.parseArguments(args);
            if (argParser.isPresent("help")) {
                System.out.println(argParser.getHelp());
                System.exit(1);
            } else {
                int value1 = (int) argParser.getValue("value1");
                int value2 = (int) argParser.getValue("value2");
                System.out.printf("%d + %d = %d\n", value1, value2, value1 + value2);
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("Error with input: " + e.getMessage());
            System.out.println(argParser.getHelp());
            System.exit(-1);
        }
    }
}
