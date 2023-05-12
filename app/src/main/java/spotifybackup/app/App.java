package spotifybackup.app;

import spotifybackup.cmd.*;
import spotifybackup.cmd.argument.file.MandatoryFilePathArgument;
import spotifybackup.cmd.argument.integer.DefaultIntArgument;
import spotifybackup.cmd.argument.integer.MandatoryIntArgument;
import spotifybackup.cmd.argument.string.DefaultStringArgument;

public class App {
    public static void main(String[] args) {
//        System.out.println("Hello SpotifyBackup!");
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryIntArgument("int", "some integer"),
                new DefaultIntArgument("int2", "Lorem ipsum dolor sit amet, consectetur adipiscing " +
                        "elit, sed do eiusmod tempor incididunt ut", 'i', 23),
                new DefaultStringArgument("str", "some sort of string, dunno, not gonna use it.",
                        's', "string"),
                new MandatoryFilePathArgument("txt", "Lorem ipsum dolor sit amet, consectetur " +
                        "adipiscing elit, sed do eiusmod tempor incididunt ut", false)
        }, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut",
                "testName.jar", "labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
                "nostrud exercitation");
//        argParser.parseArguments(args);
        System.out.println(argParser.getHelp(60));
    }
}
