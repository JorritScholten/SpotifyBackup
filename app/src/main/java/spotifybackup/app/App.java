package spotifybackup.app;

import spotifybackup.cmd.*;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello SpotifyBackup!");
        CmdParser argParser = new CmdParser(new Argument[]{
                new MandatoryIntArgument("int", "some integer"),
                new DefaultIntArgument("int2", "An interpreter for printf-style format strings. This class provides support for layout justification and alignment, common formats for numeric, string, and date/time data, and locale-specific output. Common Java types such as byte, BigDecimal, and Calendar are supported. Limited formatting customization for arbitrary user types is provided through the Formattable interface.\n",
                        'i', 23),
                new DefaultStringArgument("str", "some sort of string, dunno, not gonna use it.", 's', "string")
        });
//        argParser.parseArguments(args);
        System.out.println(argParser.getHelp());
    }
}
